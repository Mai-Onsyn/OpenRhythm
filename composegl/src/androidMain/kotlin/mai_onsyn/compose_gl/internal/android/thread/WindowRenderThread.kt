package mai_onsyn.compose_gl.internal.android.thread

import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLES30
import android.view.Surface
import mai_onsyn.compose_gl.ComposeGL
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.android.AndroidCallbacks
import mai_onsyn.compose_gl.internal.android.egl.EglCore
import mai_onsyn.compose_gl.internal.android.egl.EglWindowSurface
import mai_onsyn.compose_gl.internal.android.framebuffer.GlesFramebuffer
import mai_onsyn.compose_gl.scope.GLInfo

/**
 * 零拷贝模式：直接渲染到 TextureView 的 SurfaceTexture 并 swap，全程不碰 CPU 内存。
 *
 * 对应 [mai_onsyn.compose_gl.config.AndroidSurfaceMode.TextureView]。
 *
 * 注意：该模式下 GL 层会吞掉其范围内的所有触摸（`AndroidView` 的固有行为），
 * 见 docs/DESIGN.md 7.5。
 */
internal class WindowRenderThread(
    private val config: GLConfig,
    private val sessionKey: String,
    framePolicy: FramePolicy,
    callbacks: AndroidCallbacks,
    scope: MutableGLRenderScope,
    private val nativeSurface: Surface,
    initialWidth: Int,
    initialHeight: Int,
) : FrameLoopThread(framePolicy, callbacks, scope, initialWidth, initialHeight) {

    private var core: EglCore? = null
    private var window: EglWindowSurface? = null

    /**
     * `msaa > 1` 时先渲染到多重采样 FBO，再 blit 到窗口表面。
     * 窗口表面本身不支持多重采样，只能走这条中转路径。
     */
    private val framebuffer = GlesFramebuffer()
    private var useMsaa = false

    override fun onStart() {
        val created = EglCore.acquire(sessionKey, config, EGL14.EGL_WINDOW_BIT)
        core = created
        val surface = EglWindowSurface(created)
        window = surface
        surface.create(nativeSurface)

        useMsaa = config.msaa > 1

        scope.infoValue = readInfo()
        scope.requestHandler = { requestRender() }
        scope.densityValue = density
        // 由基类统一触发上下文级 / 画布级回调，这里只把「是不是首次」传上去
        contextIsFirstUse = created.isFirstContextUse
        created.onContextDestroy = { ComposeGL.onContextDestroyed(scope) }
    }

    override fun onResize(width: Int, height: Int) {
        if (useMsaa) framebuffer.resize(config, width, height)
    }

    override fun beforeDraw(width: Int, height: Int) {
        if (useMsaa) {
            framebuffer.bind()
        } else {
            // surface 尺寸变化（旋转、分屏、insets 变化）时，默认帧缓冲会自动跟随，
            // 但 viewport 是 GL 状态位，不会自己变——旋转后会一直停在 eglMakeCurrent
            // 时的旧尺寸上，画面按旧画布尺寸拉伸。与桌面路径保持一致：每帧显式
            // 绑回默认帧缓冲并重设视口。
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
            GLES20.glViewport(0, 0, width, height)
        }
    }

    override fun present(width: Int, height: Int) {
        reportReadback(0)
        if (useMsaa) {
            // 解析到单采样纹理，再把结果贴到默认帧缓冲
            framebuffer.resolve()
            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0)
            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, framebuffer.presentedFbo)
            GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0)
            GLES30.glBlitFramebuffer(
                0, 0, width, height,
                0, 0, width, height,
                GLES30.GL_COLOR_BUFFER_BIT, GLES20.GL_NEAREST,
            )
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        }
        window?.swapBuffers()
    }

    override fun onStop() {
        framebuffer.dispose()
        window?.release()
        core?.release()
        nativeSurface.release()
    }

    private fun readInfo(): GLInfo {
        val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "unknown"
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "unknown"
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "unknown"
        val (major, minor) = parseVersion(version)
        return GLInfo(major, minor, version, renderer, vendor)
    }

    private fun parseVersion(version: String): Pair<Int, Int> {
        val raw = version.substringAfter("OpenGL ES ", version)
        val head = raw.substringBefore(' ').substringBefore('-')
        val parts = head.split('.')
        return (parts.getOrNull(0)?.toIntOrNull() ?: 3) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }
}
