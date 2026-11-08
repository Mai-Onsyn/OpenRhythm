package mai_onsyn.compose_gl.internal.desktop.native

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.GLCanvasHandle
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.error.GLLog
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.scope.FrameStats
import mai_onsyn.compose_gl.scope.GLInfo
import mai_onsyn.compose_gl.scope.GLRenderScope
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.roundToInt

/**
 * 重量级原生画布：GL 直接渲染到 AWT 组件上，**不回读、不经 Skia**。
 *
 * 与 [mai_onsyn.compose_gl.internal.desktop.DesktopRenderSurface] 的区别：
 * - 没有独立渲染线程：`paintGL` 在 **EDT** 上被调用，用户回调都跑在 EDT；
 * - 没有 FBO / 回读 / Image：画面直接进窗口，零每帧分配、零 GC 压力；
 * - 帧驱动靠 `javax.swing.Timer` 触发 `repaint()`。
 *
 * ## 已知问题（尚未解决）
 *
 * 在 Compose Desktop 的 `SwingPanel` 互操作层里，这个 AWT 重量级画布**渲染不出来**：
 * - 不加 `-Dcompose.interop.blending=true`：画布被 Skia 层完全盖住，一片白；
 * - 加了 blending：画布变透明，GL 输出依然不可见。
 *
 * 已排除的原因：GL 上下文能建（4.5 core）、`initGL`/`paintGL` 都在跑（帧号递增）、
 * 组件 `isShowing=true` 且尺寸正确、给画布设红色背景也看不到。
 * 剩下的怀疑点在 Compose 的 `SwingInteropViewGroup` 对 heavyweight 子组件的混排处理。
 *
 * 因此这个模式目前**不可用**，默认仍是 `DesktopSurfaceMode.ComposeImage`。
 * 要真正的零拷贝 + 可叠加，需要走 GL 纹理与 Skia 共享（见 DESIGN 17.5）。
 */
internal class NativeGLCanvas private constructor(
    data: GLData,
    private val config: GLConfig,
) : AWTGLCanvas(data) {

    constructor(config: GLConfig) : this(buildGlData(config), config)

    var onSurfaceCreated: (GLRenderScope) -> Unit = {}
    var onSurfaceResized: (GLRenderScope, IntSize) -> Unit = { _, _ -> }
    var onDraw: (GLRenderScope) -> Unit = {}
    var onSurfaceDestroyed: (GLRenderScope) -> Unit = {}
    var onFailure: (GLException) -> Unit = { e -> GLLog.warn("ComposeGL", e.message ?: "GL 失败", e) }

    /** 显示器刷新率，`Continuous(maxFps = 0)` 时用它定周期。 */
    @Volatile
    var refreshRateHz: Float = 60f

    var density: Float = 1f
        set(value) {
            field = value
            scope.densityValue = value
        }

    private val scope = MutableGLRenderScope()
    private var lastWidth = -1
    private var lastHeight = -1
    private var timer: Timer? = null
    private var initialized = false
    private var disposed = false

    init {
        // AWTGLCanvas 没有重写 paint()：AWT 的绘制流程不会触发 GL 渲染，
        // 必须由我们显式调 render()（它内部完成 beforeRender -> paintGL -> swapBuffers）。
        // 关闭 AWT 自己的重绘，避免和我们的渲染互相打架。
        setIgnoreRepaint(true)
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = requestRender()
            override fun componentShown(e: ComponentEvent) = requestRender()
        })
    }

    override fun addNotify() {
        super.addNotify()
        SwingUtilities.invokeLater { doRender() }
    }

    /** 真正的渲染入口：必须在 EDT 上执行。 */
    private fun doRender() {
        if (disposed) return
        try {
            render()
        } catch (t: Throwable) {
            onFailure(t as? GLException ?: GLException("原生画布渲染异常：${t.message}", t))
        }
    }

    override fun initGL() {
        GL.createCapabilities()
        scope.infoValue = GLInfo(
            major = GL11.glGetInteger(GL30.GL_MAJOR_VERSION),
            minor = GL11.glGetInteger(GL30.GL_MINOR_VERSION),
            versionString = GL11.glGetString(GL11.GL_VERSION) ?: "unknown",
            rendererString = GL11.glGetString(GL11.GL_RENDERER) ?: "unknown",
            vendorString = GL11.glGetString(GL11.GL_VENDOR) ?: "unknown",
        )
        scope.requestHandler = { requestRender() }
        scope.densityValue = density
        scope.refreshRateValue = refreshRateHz
        initialized = true
        // 每次上下文创建调用一次（驱动复位后会重新走这里）
        onSurfaceCreated(scope)
    }

    override fun paintGL() {
        if (disposed) return
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        if (w != lastWidth || h != lastHeight) {
            lastWidth = w
            lastHeight = h
            scope.w = w
            scope.h = h
            onSurfaceResized(scope, IntSize(w, h))
        }

        scope.frame += 1
        val start = System.nanoTime()
        scope.frameTime = start
        try {
            GL11.glViewport(0, 0, w, h)
            onDraw(scope)
        } catch (t: Throwable) {
            onFailure(t as? GLException ?: GLException("原生画布绘制异常：${t.message}", t))
        }
        val end = System.nanoTime()
        scope.statsValue = FrameStats(
            frameIndex = scope.frame,
            drawNanos = end - start,
            readbackNanos = 0,
            presentNanos = 0,
            droppedFrames = 0,
        )
    }

    /** 帧策略可在运行中切换。 */
    fun updatePolicy(policy: FramePolicy) {
        timer?.stop()
        timer = null
        if (disposed) return
        if (policy is FramePolicy.Continuous) {
            val hz = if (policy.maxFps > 0) policy.maxFps else refreshRateHz.roundToInt().coerceAtLeast(1)
            timer = Timer((1000 / hz).coerceAtLeast(1)) { doRender() }.apply {
                isCoalesce = true
                start()
            }
        }
    }

    fun requestRender() {
        if (disposed) return
        if (SwingUtilities.isEventDispatchThread()) doRender() else SwingUtilities.invokeLater { doRender() }
    }

    /** 释放：先在上下文仍有效时让用户清理资源，再销毁画布。 */
    fun disposeSafely() {
        if (disposed) return
        disposed = true
        timer?.stop()
        timer = null
        if (initialized) {
            try {
                runInContext { onSurfaceDestroyed(scope) }
            } catch (t: Throwable) {
                // 上下文已经没了就跳过清理，别把异常抛到 EDT
            }
        }
        try {
            disposeCanvas()
        } catch (_: Throwable) {
        }
    }

    private companion object {
        fun buildGlData(config: GLConfig): GLData = GLData().apply {
            // lwjgl3-awt 的 GLUtil.validVersionGL 只认到 4.5，传 4.6 会抛
            // IllegalArgumentException: Invalid OpenGL version。
            // 这里夹到 4.5 —— 驱动仍会给到可用的最高 core 上下文。
            val (reqMajor, reqMinor) = config.desktopGlVersion
            val major = reqMajor.coerceIn(1, 4)
            val minor = if (major == 4) reqMinor.coerceIn(0, 5) else reqMinor.coerceAtLeast(0)
            if (major != reqMajor || minor != reqMinor) {
                GLLog.warn(
                    "ComposeGL",
                    "NativeSurface 模式最高支持 GL 4.5，已把请求的 $reqMajor.$reqMinor 降到 $major.$minor",
                )
            }
            majorVersion = major
            minorVersion = minor
            profile = GLData.Profile.CORE
            doubleBuffer = true
            redSize = 8
            greenSize = 8
            blueSize = 8
            alphaSize = config.alphaBits.coerceAtLeast(8)
            depthSize = config.depthBits
            stencilSize = config.stencilBits
            if (config.msaa > 1) {
                sampleBuffers = 1
                samples = config.msaa
            }
        }
    }
}

/**
 * 用 `SwingPanel` 承载原生画布的 Compose 入口。
 *
 * 注意：这是**重量级组件**，Compose 画在它上面的内容会被它盖住。
 */
@Composable
internal fun NativeSurfaceGLCanvas(
    modifier: Modifier,
    config: GLConfig,
    framePolicy: FramePolicy,
    refreshRateHz: Float,
    density: Float,
    handle: GLCanvasHandle?,
    onSurfaceCreated: (GLRenderScope) -> Unit,
    onSurfaceResized: (GLRenderScope, IntSize) -> Unit,
    onDraw: (GLRenderScope) -> Unit,
    onSurfaceDestroyed: (GLRenderScope) -> Unit,
    onFailure: (GLException) -> Unit,
) {
    // AWT 组件在 EDT 上创建：Compose 的组合线程不是 EDT，直接 new 出来的 Canvas
    // 在 JAWT 侧可能拿不到 drawing surface
    val canvas = remember(config) {
        var created: NativeGLCanvas? = null
        if (SwingUtilities.isEventDispatchThread()) {
            created = NativeGLCanvas(config)
        } else {
            SwingUtilities.invokeAndWait { created = NativeGLCanvas(config) }
        }
        created!!
    }

    SideEffect {
        canvas.refreshRateHz = refreshRateHz
        canvas.density = density
        canvas.onSurfaceCreated = onSurfaceCreated
        canvas.onSurfaceResized = onSurfaceResized
        canvas.onDraw = onDraw
        canvas.onSurfaceDestroyed = onSurfaceDestroyed
        canvas.onFailure = onFailure
        canvas.updatePolicy(framePolicy)
    }

    DisposableEffect(canvas, handle) {
        // 这个模式没有独立的 paused 概念（Continuous 靠 Swing Timer 驱动），只接驱动入口。
        // 何况这条路径本身已确认不可用，不再为它补暂停语义。
        handle?.attach(request = { canvas.requestRender() }, applyPause = {})
        onDispose { handle?.detach() }
    }

    DisposableEffect(canvas) {
        onDispose { canvas.disposeSafely() }
    }

    SwingPanel(
        factory = { canvas },
        modifier = modifier,
        background = Color.Transparent,
    )
}
