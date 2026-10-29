package mai_onsyn.compose_gl.internal.android.egl

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.view.Surface
import mai_onsyn.compose_gl.error.GLException

/** 包住 TextureView 的 SurfaceTexture 的 EGL 窗口表面。 */
internal class EglWindowSurface(private val core: EglCore) {

    private var surface: EGLSurface = EGL14.EGL_NO_SURFACE

    fun create(nativeSurface: Surface) {
        val attributes = intArrayOf(EGL14.EGL_NONE)
        surface = EGL14.eglCreateWindowSurface(core.display, core.eglConfig, nativeSurface, attributes, 0)
        if (surface == EGL14.EGL_NO_SURFACE) {
            throw GLException("eglCreateWindowSurface 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
        }
        makeCurrent()
    }

    fun makeCurrent() {
        if (!EGL14.eglMakeCurrent(core.display, surface, surface, core.context)) {
            throw GLException("eglMakeCurrent 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
        }
    }

    fun swapBuffers() {
        if (!EGL14.eglSwapBuffers(core.display, surface)) {
            throw GLException("eglSwapBuffers 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
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
