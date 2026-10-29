package mai_onsyn.compose_gl.internal.android

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import mai_onsyn.compose_gl.config.AndroidSurfaceMode
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.android.compose.AndroidImageHost
import mai_onsyn.compose_gl.internal.android.thread.FrameLoopThread
import mai_onsyn.compose_gl.internal.android.thread.OffscreenRenderThread
import mai_onsyn.compose_gl.internal.android.thread.WindowRenderThread
import mai_onsyn.compose_gl.internal.android.view.GLTextureView

/**
 * 安卓端编排：按 [AndroidSurfaceMode] 起对应的渲染线程，并转发尺寸与帧请求。
 *
 * 不直接调用任何 GLES 函数。
 */
internal class AndroidRenderSurface(
    private val config: GLConfig,
    private val sessionKey: String,
    framePolicy: FramePolicy,
    private val mode: AndroidSurfaceMode,
    val callbacks: AndroidCallbacks,
    private val host: AndroidImageHost,
) {

    private val scope = MutableGLRenderScope()
    private var thread: FrameLoopThread? = null
    private var lastWidth = 0
    private var lastHeight = 0

    @Volatile
    var framePolicy: FramePolicy = framePolicy
        set(value) {
            field = value
            thread?.let {
                it.framePolicy = value
                // 线程可能正阻塞在 wait() 上，必须唤醒它重新判断策略
                it.requestRender()
            }
        }

    @Volatile
    var paused: Boolean = false
        set(value) {
            field = value
            thread?.paused = value
        }

    @Volatile
    var refreshRateHz: Float = 60f
        set(value) {
            field = value
            scope.refreshRateValue = value
            thread?.refreshRateHz = value
        }

    @Volatile
    var density: Float = 1f
        set(value) {
            field = value
            scope.densityValue = value
            thread?.density = value
        }

    /** 离屏模式在这里起线程；零拷贝模式要等 Surface 就绪，什么也不做。 */
    fun start() {
        if (mode != AndroidSurfaceMode.ComposeImage) return
        if (thread != null) return
        val created = OffscreenRenderThread(
            config = config,
            sessionKey = sessionKey,
            framePolicy = framePolicy,
            callbacks = callbacks,
            scope = scope,
            host = host,
            initialWidth = lastWidth,
            initialHeight = lastHeight,
        )
        created.density = density
        created.refreshRateHz = refreshRateHz
        created.paused = paused
        scope.refreshRateValue = refreshRateHz
        thread = created
        created.start()
    }

    /** 已接管的 View，用来让 [attach] 幂等。 */
    private var attachedView: GLTextureView? = null

    /**
     * 只有零拷贝模式需要：接上 TextureView 的 SurfaceTexture 生命周期。
     *
     * **必须幂等**：`config` 变化会重建 surface，而 `AndroidView` 的 factory 只跑一次，
     * 新 surface 只能通过 `update` 重新接管已有 View。若此时 SurfaceTexture 已就绪，
     * 不会再有 `onSurfaceTextureAvailable` 回调，所以要主动起线程。
     */
    fun attach(view: GLTextureView) {
        if (mode != AndroidSurfaceMode.TextureView) return
        if (attachedView === view && thread != null) return
        attachedView = view

        view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                startWindowThread(Surface(surfaceTexture), width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                resize(width, height)
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                stopThread()
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
        }

        // View 已经在显示中：直接接管现有 SurfaceTexture
        val existing = view.surfaceTexture
        if (existing != null && view.width > 0 && view.height > 0) {
            startWindowThread(Surface(existing), view.width, view.height)
        }
    }

    fun resize(width: Int, height: Int) {
        lastWidth = width
        lastHeight = height
        thread?.resize(width, height)
    }

    fun requestRender() {
        thread?.requestRender()
    }

    fun dispose() {
        stopThread()
    }

    private fun startWindowThread(surface: Surface, width: Int, height: Int) {
        stopThread()
        lastWidth = width
        lastHeight = height
        val created = WindowRenderThread(
            config = config,
            sessionKey = sessionKey,
            framePolicy = framePolicy,
            callbacks = callbacks,
            scope = scope,
            nativeSurface = surface,
            initialWidth = width,
            initialHeight = height,
        )
        created.density = density
        created.refreshRateHz = refreshRateHz
        created.paused = paused
        scope.refreshRateValue = refreshRateHz
        thread = created
        created.start()
    }

    private fun stopThread() {
        attachedView = null
        val running = thread ?: return
        thread = null
        running.stopGracefully()
    }
}
