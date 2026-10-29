package mai_onsyn.compose_gl.internal.android.egl

import android.opengl.EGL14
import android.opengl.EGLSurface
import mai_onsyn.compose_gl.error.GLException

/**
 * 1x1 的 pbuffer 表面。
 *
 * EGL 要求必须有一个 current 的 surface 才能执行 GL 调用；离屏模式下渲染目标是 FBO，
 * 所以这个 surface 只是占位，不承载任何像素。
 */
internal class EglPbufferSurface(private val core: EglCore) {

    private var surface: EGLSurface = EGL14.EGL_NO_SURFACE

    fun create() {
        val attributes = intArrayOf(
            EGL14.EGL_WIDTH, 1,
            EGL14.EGL_HEIGHT, 1,
            EGL14.EGL_NONE,
        )
        surface = EGL14.eglCreatePbufferSurface(core.display, core.eglConfig, attributes, 0)
        if (surface == EGL14.EGL_NO_SURFACE) {
            throw GLException("eglCreatePbufferSurface 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
        }
        makeCurrent()
    }

    fun makeCurrent() {
        if (!EGL14.eglMakeCurrent(core.display, surface, surface, core.context)) {
            throw GLException("eglMakeCurrent 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
        }
    }

    fun release() {
        if (surface != EGL14.EGL_NO_SURFACE) {
            core.releaseCurrent()
            EGL14.eglDestroySurface(core.display, surface)
            surface = EGL14.EGL_NO_SURFACE
        }
    }
}
