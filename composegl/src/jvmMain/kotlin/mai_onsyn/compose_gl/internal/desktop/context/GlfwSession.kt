package mai_onsyn.compose_gl.internal.desktop.context

import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.error.GLLog
import mai_onsyn.compose_gl.gl.GLThreadGuard
import mai_onsyn.compose_gl.internal.desktop.skiko.SkikoGlShare
import mai_onsyn.compose_gl.scope.GLInfo
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL43
import org.lwjgl.opengl.GLDebugMessageCallback
import org.lwjgl.opengl.GLDebugMessageCallbackI
import org.lwjgl.opengl.WGL
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL

/**
 * 进程级共享的 GL 上下文会话。
 *
 * 同一个 [key] 的所有 GLCanvas 共用**同一个常驻上下文**：GLCanvas 卸载时不销毁它，
 * 下次挂载直接复用 —— 着色器、纹理这些昂贵资源不用重新编译/创建。
 *
 * ## 为什么会话自己持有一条线程
 *
 * GL 上下文只能在**一个线程**上 current。而 WGL 还有一个更隐蔽的坑：
 * 上下文在 A 线程创建后，拿到 B 线程重新绑定会因为 **DC 的像素格式不匹配**而失败
 * （GLFW 报 `GLFW_FORMAT_UNAVAILABLE` / "WGL: Failed to make context current"）。
 * 隔离测试证实：只要 DC 是同一个，上下文就能跨线程迁移；而 GLFW 每个线程重新 `GetDC`
 * 拿到的 DC 像素格式不同，迁移就会失败。
 *
 * 所以这里让**会话线程持有上下文，所有渲染都在这条线程上跑**，上下文永远不跨线程。
 */
internal class GlfwSession private constructor(
    val key: String,
    private val config: GLConfig,
    private val useSharedContext: Boolean,
) {

    /** 会话线程持有的窗口与上下文。 */
    private var window = 0L
    private var info: GLInfo? = null
    private var debugCallback: GLDebugMessageCallback? = null

    // wait/notifyAll 是 java.lang.Object 的方法，kotlin.Any 上没有
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock = Object()
    private var pendingSurface: SessionClient? = null
    private var closed = false

    /** 上下文创建后的第一个客户 —— 它需要初始化资源。 */
    private var firstClient = true

    /** 上下文信息。会话线程建好上下文之后才有值。 */
    val glInfo: GLInfo?
        get() = info

    private val thread = Thread({ sessionLoop() }, "ComposeGL-Session").apply {
        isDaemon = true
    }

    /** 由会话线程调用：在本线程上创建窗口与上下文。 */
    private fun createContextOnSessionThread() {
        if (useSharedContext) {
            createSharedContextOnSessionThread()
            return
        }
        val requested = config.desktopGlVersion
        val attempts = buildList {
            add(requested)
            add(4 to 3)
            add(3 to 3)
        }.distinct()

        var lastError = ""
        for ((major, minor) in attempts) {
            GLFW.glfwDefaultWindowHints()
            GLFW.glfwWindowHint(GLFW_VISIBLE, GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major)
            GLFW.glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor)
            GLFW.glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
            if (config.debugContext) {
                GLFW.glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE)
            }

            val created = GLFW.glfwCreateWindow(1, 1, "ComposeGL", NULL, NULL)
            if (created == NULL) {
                lastError = "无法以 GL $major.$minor core 创建上下文"
                continue
            }
            GLFW.glfwMakeContextCurrent(created)
            GL.createCapabilities()
            window = created
            info = GLInfo(
                major = major,
                minor = minor,
                versionString = GL11.glGetString(GL11.GL_VERSION) ?: "unknown",
                rendererString = GL11.glGetString(GL11.GL_RENDERER) ?: "unknown",
                vendorString = GL11.glGetString(GL11.GL_VENDOR) ?: "unknown",
            )
            installDebugCallback()
            return
        }
        throw GLException("无法创建 OpenGL 上下文：$lastError")
    }

    /**
     * 零拷贝模式：直接复用影子类建的 bootstrap 上下文。
     *
     * 它与 Skiko 的 GL 上下文**从创建起就在同一个 WGL Share Group**，所以这里建的纹理
     * 可以被 Skia 直接采样。GLFW 的 `glfwCreateWindow(..., share)` 只接受另一个 GLFW
     * window，传不进原生 HGLRC，因此这条路径必须自己 `wglMakeCurrent`。
     */
    private fun createSharedContextOnSessionThread() {
        val hglrc = SkikoGlShare.ensureBootstrap()
        val dc = SkikoGlShare.bootstrapDc
        if (hglrc == 0L || dc == 0L) {
            throw GLException(
                "SharedTexture bootstrap context unavailable; " +
                    "start the JVM with -Dskiko.renderApi=OPENGL",
            )
        }
        val stack = MemoryStack.stackPush()
        val ok = try {
            WGL.wglMakeCurrent(stack.ints(1), dc, hglrc)
        } finally {
            stack.pop()
        }
        if (!ok) throw GLException("wglMakeCurrent failed for shared bootstrap context")
        GL.createCapabilities()
        val version = GL11.glGetString(GL11.GL_VERSION) ?: "unknown"
        info = GLInfo(
            major = version.trim().substringBefore('.').toIntOrNull() ?: 4,
            minor = version.trim().substringAfter('.').substringBefore(' ').toIntOrNull() ?: 5,
            versionString = version,
            rendererString = GL11.glGetString(GL11.GL_RENDERER) ?: "unknown",
            vendorString = GL11.glGetString(GL11.GL_VENDOR) ?: "unknown",
        )
        installDebugCallback()
    }

    private fun installDebugCallback() {
        if (!config.debugContext) return
        if (!GL.getCapabilities().OpenGL43) return
        val emitted = java.util.concurrent.atomic.AtomicInteger()
        val impl = GLDebugMessageCallbackI { source, type, _, severity, length, message, _ ->
            // 出错时每帧都会回调，必须限流，否则日志刷爆
            if (message != NULL && emitted.getAndIncrement() < DEBUG_MESSAGE_LIMIT) {
                val text = GLDebugMessageCallback.getMessage(length, message)
                GLLog.warn(
                    "GL",
                    "source=0x${source.toString(16)} type=0x${type.toString(16)} " +
                        "severity=0x${severity.toString(16)}: $text",
                )
            }
        }
        val callback = GLDebugMessageCallback.create(impl)
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT)
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS)
        GL43.glDebugMessageCallback(callback, NULL)
        debugCallback = callback
    }

    /** 会话是否已被 [invalidate]。画布循环靠它退出。 */
    val isClosed: Boolean
        get() = synchronized(lock) { closed }

    /** 上下文销毁时回调（由客户端注册，在会话线程收尾时执行，此时上下文仍有效）。 */
    @Volatile
    var onContextDestroy: (() -> Unit)? = null

    /** 挂上一个画布。会话线程空闲时会接手它。 */
    fun attach(client: SessionClient) {
        synchronized(lock) {
            pendingSurface = client
            lock.notifyAll()
        }
    }

    /** 关闭会话（进程退出时用）。 */
    fun close() {
        synchronized(lock) {
            closed = true
            lock.notifyAll()
        }
    }

    private fun sessionLoop() {
        try {
            createContextOnSessionThread()
        } catch (t: Throwable) {
            GLLog.warn("GL", "Failed to create resident GL context: ${t.message}")
            synchronized(lock) {
                pendingSurface?.onSessionFailure(t)
                pendingSurface = null
            }
            return
        }
        try {
            while (true) {
                val client = synchronized(lock) {
                    while (pendingSurface == null && !closed) {
                        lock.wait()
                    }
                    if (closed) return
                    val c = pendingSurface
                    pendingSurface = null
                    c!!
                }
                val firstUse = firstClient
                firstClient = false
                client.runOnSessionThread(this, firstUse)
            }
        } finally {
            // 先把上下文级销毁回调跑掉：此刻上下文仍然 current，用户可以安全删 program/texture
            GLThreadGuard.enter()
            try {
                runCatching { onContextDestroy?.invoke() }
                    .onFailure { GLLog.warn("GL", "onContextDestroyed 抛出异常: ${it.message}") }
            } finally {
                GLThreadGuard.exit()
            }
            onContextDestroy = null

            debugCallback?.free()
            debugCallback = null
            if (window != 0L) {
                GLFW.glfwDestroyWindow(window)
                window = 0L
            }
            // GLFW 的终止必须发生在会话线程收尾之后，否则会话线程还在用就已经被 terminate
            releaseGlfw()
        }
    }

    companion object {
        private const val DEBUG_MESSAGE_LIMIT = 50
        private var initCount = 0
        private val sessions = HashMap<String, GlfwSession>()

        /** 取（或建）会话。相同 [key] 拿到同一个常驻上下文。 */
        @Synchronized
        fun obtain(key: String, config: GLConfig, useSharedContext: Boolean = false): GlfwSession {
            sessions[key]?.let { return it }
            if (initCount == 0 && !GLFW.glfwInit()) {
                throw GLException("glfwInit 失败")
            }
            initCount++
            val created = GlfwSession(key, config, useSharedContext)
            sessions[key] = created
            created.thread.start()
            return created
        }

        /**
         * 丢弃会话：下次挂载会重新建上下文（着色器需要重新编译）。
         *
         * 这里只标记关闭。真正的收尾（触发 onContextDestroyed、销毁窗口、归还 GLFW）
         * 由会话线程在退出时完成 —— 因为那时上下文才 current，GL 调用才安全。
         */
        @Synchronized
        fun invalidate(key: String) {
            sessions.remove(key)?.close()
        }

        /** 会话线程退出后调用：归还 GLFW 引用，归零时终止。 */
        @Synchronized
        internal fun releaseGlfw() {
            initCount--
            if (initCount <= 0) {
                initCount = 0
                GLFW.glfwTerminate()
            }
        }
    }
}

/**
 * 由会话线程驱动的客户端（一块画布）。
 *
 * 生命周期：
 * 1. [runOnSessionThread] 在**会话线程**上被调用，画布的全部 GL 工作都在这条线程上；
 * 2. 画布卸载时它自己会让循环退出，方法返回；
 * 3. 会话随后可以接手下一块画布。
 */
internal interface SessionClient {
    /**
     * @param isFirstContextUse true 表示上下文是**刚刚创建**的（会话的第一个客户）；
     *   false 表示复用了常驻上下文，客户端之前的 GL 资源依然有效。
     */
    fun runOnSessionThread(session: GlfwSession, isFirstContextUse: Boolean)

    /** 会话线程建上下文失败时回调；默认空实现，客户端可用来触发 onFailure。 */
    fun onSessionFailure(error: Throwable) = Unit
}
