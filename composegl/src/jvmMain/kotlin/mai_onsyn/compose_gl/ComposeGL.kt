package mai_onsyn.compose_gl

import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.config.contextSignature
import mai_onsyn.compose_gl.internal.desktop.context.GlfwSession

internal actual fun invalidateContextPlatform(sessionKey: String) {
    // 桌面端的会话 key 里还带着配置特征，这里用默认特征去找（配置变了就是另一个会话）
    val key = "$sessionKey|${GLConfig().contextSignature()}"
    GlfwSession.invalidate(key)
}
