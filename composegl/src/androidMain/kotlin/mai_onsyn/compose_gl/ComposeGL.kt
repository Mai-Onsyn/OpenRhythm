package mai_onsyn.compose_gl

import android.opengl.EGL14
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.internal.android.egl.EglSession

internal actual fun invalidateContextPlatform(sessionKey: String) {
    // 安卓端两种承载模式各有一个会话，都清掉
    val config = GLConfig()
    EglSession.invalidate(EglSession.sessionKey(sessionKey, config, EGL14.EGL_WINDOW_BIT))
    EglSession.invalidate(EglSession.sessionKey(sessionKey, config, EGL14.EGL_PBUFFER_BIT))
}
