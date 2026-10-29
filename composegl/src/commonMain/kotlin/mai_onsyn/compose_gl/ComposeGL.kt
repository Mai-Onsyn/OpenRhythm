package mai_onsyn.compose_gl

import mai_onsyn.compose_gl.config.AndroidSurfaceMode

/**
 * 全局启动配置。
 *
 * 这些都是**启动期一次性**的设置，进程运行中修改不应期待立即生效。
 */
object ComposeGL {

    /**
     * 安卓端的承载方式，默认 [AndroidSurfaceMode.ComposeImage]（正确优先）。
     *
     * 想用零拷贝的 `TextureView` 快速路径时，在 `Application.onCreate` 或 `setContent` 之前设置：
     *
     * ```
     * ComposeGL.androidSurfaceMode = AndroidSurfaceMode.TextureView
     * ```
     *
     * 注意切到 `TextureView` 后，GL 层下面的 Compose 组件将无法接收触摸。
     */
    var androidSurfaceMode: AndroidSurfaceMode = AndroidSurfaceMode.TextureView

    /**
     * 丢弃某个会话的常驻 GL 上下文。
     *
     * 上下文是常驻的（活过画布的挂载周期），所以平时**不需要**调用它。
     * 只有这些情况才需要：
     * - 你改了着色器/资源，想让它们重新编译
     * - 驱动复位（context lost）之后要重建
     *
     * 调用后，下一次挂载该 [sessionKey] 的 GLCanvas 会新建上下文，
     * 并重新触发 `onSurfaceCreated`（此时可以重建资源）。
     */
    fun invalidateContext(sessionKey: String = DEFAULT_SESSION_KEY) {
        invalidateContextPlatform(sessionKey)
    }
}

internal expect fun invalidateContextPlatform(sessionKey: String)
