package mai_onsyn.compose_gl

import mai_onsyn.compose_gl.config.AndroidSurfaceMode
import mai_onsyn.compose_gl.config.DesktopSurfaceMode
import mai_onsyn.compose_gl.scope.GLRenderScope

/**
 * 全局启动配置与**上下文级**生命周期回调。
 *
 * 这里的设置都是启动期一次性设置，进程运行中修改不应期待立即生效。
 */
object ComposeGL {

    /**
     * GL 上下文创建后调用一次（每个上下文一次）。
     *
     * 执行时机：该上下文的第一个画布挂载时，**在任何画布的 [GLCanvas.onCanvasCreated] 之前**。
     * 线程与上下文：在该会话的渲染线程上，上下文已 current。
     *
     * 适合放「重建很贵、且与具体画布无关」的东西：着色器程序、字体图集、全局 UBO。
     * 不要在这里创建跟某块画布绑定的资源（VBO/VAO/FBO）——那些属于 [GLCanvas.onCanvasCreated]。
     *
     * 在 `main` 里赋值即可，早于任何界面显示：
     *
     * ```kotlin
     * ComposeGL.onContextCreated = { renderer.compileShaders() }
     * ```
     */
    var onContextCreated: GLRenderScope.() -> Unit = {}

    /**
     * GL 上下文销毁前调用一次（与 [onContextCreated] 配对）。
     *
     * 触发条件只有 [invalidateContext]（正常切页面不会销毁上下文）。
     * 在该会话的渲染线程上执行，**此时上下文仍然有效**，可以安全地删 program / texture。
     *
     * 上下文被销毁后，下一次挂载会重新触发 [onContextCreated]。
     */
    var onContextDestroyed: GLRenderScope.() -> Unit = {}

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
     * 桌面端的承载方式，默认 [DesktopSurfaceMode.ComposeImage]（能叠加 Compose 内容）。
     *
     * 想要零拷贝时在 `setContent` 之前设置：
     *
     * ```
     * ComposeGL.desktopSurfaceMode = DesktopSurfaceMode.SharedTexture
     * ```
     */
    var desktopSurfaceMode: DesktopSurfaceMode = DesktopSurfaceMode.ComposeImage

    /**
     * 丢弃某个会话的常驻 GL 上下文。
     *
     * 上下文是常驻的（活过画布的挂载周期），所以平时**不需要**调用它。只有这些情况才需要：
     * - 你改了着色器 / 资源，想让它们重新编译
     * - 驱动复位（context lost）之后要重建
     *
     * 调用后：[onContextDestroyed] 在该会话的渲染线程上触发一次（上下文仍然有效），
     * 然后上下文被销毁；下一次挂载该 [sessionKey] 的画布会新建上下文并重新触发 [onContextCreated]。
     */
    fun invalidateContext(sessionKey: String = DEFAULT_SESSION_KEY) {
        invalidateContextPlatform(sessionKey)
    }
}

internal expect fun invalidateContextPlatform(sessionKey: String)
