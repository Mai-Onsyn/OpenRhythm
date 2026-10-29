package mai_onsyn.compose_gl.internal.android.egl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import mai_onsyn.compose_gl.config.GLConfig

/**
 * EGL display / config / context 的访问入口。
 *
 * **只管上下文，不碰 surface**——具体 surface（pbuffer 或 window）由
 * [EglPbufferSurface] / [EglWindowSurface] 负责，这样两种承载模式可以共用同一套上下文代码。
 *
 * 上下文本身由 [EglSession] 持有（活过画布的挂载周期），
 * 本类只负责"借用"：用完交还，但不销毁。
 */
internal class EglCore(private val session: EglSession) {

    val display: EGLDisplay = session.display
    val eglConfig: EGLConfig = session.eglConfig
    val context: EGLContext

    /** 上下文是不是这次才创建的。false 表示复用了常驻上下文，用户的 GL 资源依然有效。 */
    val isFirstContextUse: Boolean

    /** 上下文销毁时回调（由客户端注册）。 */
    var onContextDestroy: (() -> Unit)?
        get() = session.onContextDestroy
        set(value) {
            session.onContextDestroy = value
        }

    init {
        val lease = session.acquire()
        context = lease.context
        isFirstContextUse = lease.isFirstContextUse
    }

    /** 解除当前绑定。销毁 surface / context 之前必须先调用。 */
    fun releaseCurrent() {
        EGL14.eglMakeCurrent(
            display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT,
        )
    }

    /** 交还上下文；常驻会话本身不会因此销毁。 */
    fun release() {
        releaseCurrent()
        session.release(context)
    }

    companion object {
        /**
         * 借一个上下文来用。
         *
         * @param sessionKey 会话标识。相同的 key 复用同一个常驻上下文。
         * @param surfaceType [EGL14.EGL_PBUFFER_BIT]（离屏）或 [EGL14.EGL_WINDOW_BIT]（TextureView）
         */
        fun acquire(sessionKey: String, config: GLConfig, surfaceType: Int): EglCore {
            val key = EglSession.sessionKey(sessionKey, config, surfaceType)
            val session = EglSession.obtain(key, config, surfaceType)
            return EglCore(session)
        }
    }
}
