package mai_onsyn.compose_gl.internal.android.thread

import android.opengl.EGL14
import android.opengl.GLES20
import androidx.compose.ui.graphics.asImageBitmap
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.android.AndroidCallbacks
import mai_onsyn.compose_gl.internal.android.compose.AndroidImageHost
import mai_onsyn.compose_gl.internal.android.egl.EglCore
import mai_onsyn.compose_gl.internal.android.egl.EglPbufferSurface
import mai_onsyn.compose_gl.internal.android.framebuffer.GlesFramebuffer
import mai_onsyn.compose_gl.internal.android.readback.AndroidFrameReader
import mai_onsyn.compose_gl.scope.GLInfo

/**
 * 离屏模式：1x1 pbuffer 持有上下文，渲染进 FBO，再回读成 Bitmap 交给 Compose。
 *
 * 对应 [mai_onsyn.compose_gl.config.AndroidSurfaceMode.ComposeImage]。
 */
internal class OffscreenRenderThread(
    private val config: GLConfig,
    private val sessionKey: String,
    framePolicy: FramePolicy,
    callbacks: AndroidCallbacks,
    scope: MutableGLRenderScope,
    private val host: AndroidImageHost,
    initialWidth: Int,
    initialHeight: Int,
) : FrameLoopThread(framePolicy, callbacks, scope, initialWidth, initialHeight) {

    private var core: EglCore? = null
    private var pbuffer: EglPbufferSurface? = null
    private val framebuffer = GlesFramebuffer()
    private val reader = AndroidFrameReader()

    override fun onStart() {
        val created = EglCore.acquire(sessionKey, config, EGL14.EGL_PBUFFER_BIT)
        core = created
        val surface = EglPbufferSurface(created)
        pbuffer = surface
        surface.create()

        scope.infoValue = readInfo()
        scope.requestHandler = { requestRender() }
        scope.densityValue = density
        // 上下文是常驻的：只有真正创建上下文的那一次才触发，
        // 之后重新挂载时用户的着色器等资源依然有效，不用重新编译
        if (created.isFirstContextUse) {
            callbacks.created(scope)
            created.onContextDestroy = { callbacks.destroyed(scope) }
        }
    }

    override fun onResize(width: Int, height: Int) {
        framebuffer.resize(config, width, height)
    }

    override fun beforeDraw(width: Int, height: Int) {
        framebuffer.bind()
    }

    override fun present(width: Int, height: Int) {
        // MSAA 时先把多重采样结果解析到单采样纹理，回读才拿得到像素
        framebuffer.resolve()
        GLES20.glFinish()
        val readStart = System.nanoTime()
        val bitmap = reader.read(width, height)
        reportReadback(System.nanoTime() - readStart)
        host.publish(bitmap.asImageBitmap())
    }

    override fun onStop() {
        reader.dispose()
        framebuffer.dispose()
        pbuffer?.release()
        core?.release()
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
