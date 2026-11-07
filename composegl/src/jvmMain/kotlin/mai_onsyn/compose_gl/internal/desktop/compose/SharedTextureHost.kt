package mai_onsyn.compose_gl.internal.desktop.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import mai_onsyn.compose_gl.error.GLLog
import mai_onsyn.compose_gl.internal.desktop.skiko.SkikoGlShare
import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.SurfaceOrigin
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.WGL
import org.lwjgl.system.MemoryStack
import java.awt.Component
import java.awt.Container
import java.awt.Window
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

/** GL_TIMEOUT_IGNORED as a GLuint64 (all bits set). */
private const val GL_TIMEOUT_IGNORED = -1L

/** 保留最近几帧不释放，避免 Compose 的 display list 还引用着刚被替换的 Image。 */
private const val RETAIN_FRAMES = 3

/** 启动初期 SkiaLayer 可能还没建好，给这么多次重试再判定后端不可用。 */
private const val RESOLVE_RETRY_LIMIT = 30

private class SkikoHandles(val dc: Long, val hglrc: Long, val directContext: DirectContext)

/**
 * 零拷贝路径的帧交接点。
 *
 * 渲染线程把「纹理 id + fence」交给这里，真正的 adopt 在 **EDT、Skiko 自己的 HGLRC
 * current 时**执行：
 *
 * 1. Compose 的 OpenGL 渲染循环跑在 EDT（`MainUIDispatcher = SwingDispatcher`），
 *    `WindowsOpenGLRedrawer` 每帧末尾 `makeCurrent()` 后不释放上下文 —— 在 EDT 上重新
 *    `wglMakeCurrent` 到同一个 HGLRC 是幂等且安全的。
 * 2. 跨上下文改纹理必须 fence 同步（OpenGL D.3.3）：写入方 `glFenceSync`+`glFlush`，
 *    这里 `glWaitSync`+`glDeleteSync`，让 Skia 的采样命令排在写入之后。
 * 3. **决定性**：Compose 的 display list 按底层 `org.jetbrains.skia.Image` 缓存内容，
 *    所以每帧都要 adopt 出一个全新的 Image。
 * 4. 交给 Compose 的是 **SkiaImage 本身**，不是 `ImageBitmap`。`toComposeImageBitmap()`
 *    在 Compose 1.11 里会走 `Image.toBitmap()` → `Canvas(bitmap).drawImage(...)`，
 *    把 GPU 纹理整帧光栅化到 CPU 位图，那就不是零拷贝了。
 *
 * 资源生命周期：`adoptTextureFrom` 让 Skia 接管纹理所有权，因此这里保留最近
 * [RETAIN_FRAMES] 帧，更早的 Image / BackendTexture 在 **EDT 且 Skiko 上下文 current**
 * 时显式 `close()` —— `Managed.close()` 会同步注销 Cleaner，不会在后台线程删 GL 纹理。
 */
internal class SharedTextureHost : FrameHost {

    private val state = mutableStateOf<Any?>(null)
    override val frame: State<Any?> get() = state

    @Volatile private var pendingTexture = 0
    @Volatile private var pendingFence = 0L
    @Volatile private var pendingWidth = 0
    @Volatile private var pendingHeight = 0

    /** 配置前提不满足时置位，渲染线程据此丢弃纹理而不是继续堆积。 */
    @Volatile var unusable = false
        private set

    /** 由 PlatformGLCanvas 用 SideEffect 设置，让后端不对时 UI 能显示 fallback。 */
    @Volatile var onError: ((String) -> Unit)? = null

    private val edtTaskPending = AtomicBoolean(false)
    private var handles: SkikoHandles? = null
    private var edtCapabilitiesReady = false
    private var warned = false
    private var resolveFailures = 0

    private val live = ArrayDeque<Pair<BackendTexture, SkiaImage>>()

    /**
     * 从渲染线程调用。EDT 还没消费上一帧时返回 false，调用方应立刻删掉这张纹理
     * （Skia 从未接管它），以此形成背压，避免任务堆积和无人认领的纹理泄漏。
     */
    fun tryPublish(textureId: Int, fence: Long, width: Int, height: Int): Boolean {
        if (unusable) return false
        if (!edtTaskPending.compareAndSet(false, true)) return false
        pendingTexture = textureId
        pendingFence = fence
        pendingWidth = width
        pendingHeight = height
        try {
            SwingUtilities.invokeLater {
                try {
                    publishOnEdt()
                } finally {
                    edtTaskPending.set(false)
                }
            }
        } catch (t: Throwable) {
            edtTaskPending.set(false)
            GLLog.warn("SharedTexture", "invokeLater failed: ${t.message}")
            return false
        }
        return true
    }

    private fun publishOnEdt() {
        val h = handles ?: resolveSkiko()?.also {
            handles = it
            resolveFailures = 0
        } ?: run {
            // Without this guard a backend/classpath mismatch would leave the render thread
            // allocating textures that nobody ever adopts -- a silent linear memory leak.
            if (++resolveFailures >= RESOLVE_RETRY_LIMIT) {
                markUnusable(
                    "Cannot resolve Skiko GL handles: Skiko must use the OPENGL backend and " +
                        "the composegl shadow class must precede skiko-awt on the classpath.",
                )
            }
            return
        }
        if (!SkikoGlShare.skikoContextShared) {
            markUnusable("Skiko GL context is not in our share group; OPENGL backend + shadow class required")
            return
        }
        if (!withStack { st -> WGL.wglMakeCurrent(st.ints(1), h.dc, h.hglrc) }) {
            // 窗口关闭时 HGLRC 随 Skiko 销毁，属于正常收尾，不报成错误。
            // 清掉缓存，下次建新窗口时会重新 resolve 到新的 HDC/HGLRC。
            handles = null
            return
        }
        if (!edtCapabilitiesReady) {
            GL.createCapabilities()
            edtCapabilitiesReady = true
        }

        val fence = pendingFence
        if (fence != 0L) {
            GL32.glWaitSync(fence, 0, GL_TIMEOUT_IGNORED)
            GL32.glDeleteSync(fence)
            pendingFence = 0L
        }

        val textureId = pendingTexture
        val width = pendingWidth
        val height = pendingHeight
        if (textureId == 0 || width <= 0 || height <= 0) return

        // format must be a sized internal format (GL_RGBA8 = 0x8058); target/format are GL enums.
        val backend = BackendTexture.makeGL(
            width, height, false, textureId, GL11.GL_TEXTURE_2D, GL11.GL_RGBA8,
        )
        val image = SkiaImage.adoptTextureFrom(
            h.directContext, backend, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888,
        )
        live.addLast(backend to image)
        while (live.size > RETAIN_FRAMES) {
            val (oldBackend, oldImage) = live.removeFirst()
            runCatching { oldImage.close() }
                .onFailure { GLLog.warn("SharedTexture", "close image failed: ${it.message}") }
            runCatching { oldBackend.close() }
        }
        state.value = image
        Snapshot.sendApplyNotifications()
    }

    private fun markUnusable(message: String) {
        unusable = true
        warnOnce(message)
        onError?.invoke(message)
    }

    private fun warnOnce(message: String) {
        if (warned) return
        warned = true
        GLLog.warn("SharedTexture", message)
    }

    /** SkiaLayer -> redrawer -> device(HDC) / context(HGLRC) / contextHandler.directContext. */
    private fun resolveSkiko(): SkikoHandles? {
        val window = Window.getWindows().firstOrNull { it.isVisible } ?: return null
        val layer = findSkiaLayer(window) ?: return null
        val redrawer = layer.javaClass.methods.firstOrNull {
            it.parameterCount == 0 && it.name.contains("getRedrawer")
        }?.also { it.isAccessible = true }?.invoke(layer) ?: return null

        fun longField(name: String): Long {
            var cls: Class<*>? = redrawer.javaClass
            while (cls != null) {
                for (f in cls.declaredFields) {
                    if (f.name == name && f.type == Long::class.javaPrimitiveType) {
                        f.isAccessible = true
                        return f.get(redrawer) as? Long ?: 0L
                    }
                }
                cls = cls.superclass
            }
            return 0L
        }

        val dc = longField("device")
        val hglrc = longField("context")
        if (dc == 0L || hglrc == 0L) return null

        val handler = redrawer.javaClass.declaredFields.firstOrNull {
            it.name.contains("contextHandler")
        }?.also { it.isAccessible = true }?.get(redrawer) ?: return null
        var directContext: DirectContext? = null
        var cls: Class<*>? = handler.javaClass
        while (cls != null && directContext == null) {
            for (f in cls.declaredFields) {
                if (DirectContext::class.java.isAssignableFrom(f.type)) {
                    runCatching {
                        f.isAccessible = true
                        directContext = f.get(handler) as? DirectContext
                    }
                }
            }
            cls = cls.superclass
        }
        val ctx = directContext ?: return null
        return SkikoHandles(dc, hglrc, ctx)
    }

    private fun findSkiaLayer(root: Container): Component? {
        for (child in root.components) {
            if (child.javaClass.name.contains("SkiaLayer")) return child
            if (child is Container) findSkiaLayer(child)?.let { return it }
        }
        return null
    }

    companion object {
        private val hosts = HashMap<String, SharedTextureHost>()

        /**
         * 与常驻 GL 会话一致：同一 [key] 的画布跨挂载/卸载复用同一个 host。
         * 页面切换时不会重建 Skia 资源、不会留下「已 publish 但没人 adopt」的纹理，
         * 重新挂载还能直接显示上一帧。
         */
        @Synchronized
        fun obtain(key: String): SharedTextureHost = hosts.getOrPut(key) { SharedTextureHost() }
    }
}

private inline fun <T> withStack(block: (MemoryStack) -> T): T {
    val s = MemoryStack.stackPush()
    return try {
        block(s)
    } finally {
        s.pop()
    }
}
