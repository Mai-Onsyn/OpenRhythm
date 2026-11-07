package mai_onsyn.compose_gl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.error.GLLog
import mai_onsyn.compose_gl.internal.PlatformGLCanvas
import mai_onsyn.compose_gl.scope.GLRenderScope

/**
 * 在 Compose 界面里嵌一块 OpenGL 渲染区域。
 *
 * 尺寸完全由 [modifier] 决定；库只保证回调执行时上下文已在当前线程 current，不提供绘制辅助。
 *
 * ## 两层生命周期
 *
 * GL 上下文是**常驻**的（按 [sessionKey] 分组，活过画布的挂载周期），而画布会被反复挂载/卸载。
 * 两者分开，互不干扰：
 *
 * | 回调 | 触发时机 | 放什么 |
 * |---|---|---|
 * | [ComposeGL.onContextCreated] | 每个 GL 上下文**一次** | 着色器程序、字体图集等「重建很贵」的东西 |
 * | [onCanvasCreated] | **每次挂载** | VBO / VAO / FBO 等跟这块画布绑定的东西 |
 * | [onCanvasResized] | 尺寸变化 | 跟尺寸相关的 render target |
 * | [onDraw] | 每帧 | 画一帧（**库不会帮你 glClear**） |
 * | [onCanvasDisposed] | **每次卸载**，与 [onCanvasCreated] 严格配对 | 释放上面那些资源 |
 *
 * ## 三条不变量
 *
 * 1. 所有回调都在**同一条渲染线程**上执行（桌面＝该会话的线程，安卓＝该画布的线程），
 *    执行期间上下文已 current。**不要在其他线程调用 gl\***，那是 `0xC0000005` 的常见来源。
 * 2. [onCanvasCreated] 与 [onCanvasDisposed] **次数相等**，中间至少有一次 [onDraw]。
 * 3. 同一个 [sessionKey] 的多个画布**不能同时渲染**：会话线程是串行的，后挂载的画布会排队
 *    直到前一个卸载。需要同时显示多块 GL 区域时给不同的 [sessionKey]。
 *
 * 示例：
 *
 * ```kotlin
 * // 程序启动时注册一次：贵的资源跟着上下文走
 * ComposeGL.onContextCreated = { renderer.compileShaders() }
 * ComposeGL.onContextDestroyed = { renderer.releasePrograms() }
 *
 * GLCanvas(
 *     onCanvasCreated = { renderer.uploadMesh() },   // 每次挂载
 *     onCanvasDisposed = { renderer.releaseMesh() }, // 每次卸载
 * ) {
 *     renderer.draw(width, height)                   // onDraw：尾随 lambda
 * }
 * ```
 *
 * @param onDraw 每帧的绘制回调。语义是「这一帧如何重画整个画面」，不是增量补画。
 *   放在参数表末尾，便于用尾随 lambda。
 * @param enabled 为 false 时**彻底销毁**上下文与所有资源，区域留空；重新置 true 会重新初始化。
 *   只是"暂时不画"请用 `FramePolicy` 或 `paused`，不要用它。
 * @param config 渲染配置。**改动会导致上下文 / render target 重建**，别在每帧改它。
 * @param framePolicy 帧驱动策略，可在运行中切换（不会重建 surface）。
 * @param sessionKey GL 会话标识。**同一个 key 的画布共用一个常驻 GL 上下文**：
 *   画布卸载时上下文不销毁，下次挂载直接复用。不同 key 各自独立。
 * @param fallbackContent GL 不可用时渲染的内容。
 */
@Composable
fun GLCanvas(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: GLConfig = GLConfig(),
    framePolicy: FramePolicy = FramePolicy.OnRequest,
    sessionKey: String = DEFAULT_SESSION_KEY,
    onCanvasCreated: GLRenderScope.() -> Unit = {},
    onCanvasResized: GLRenderScope.(IntSize) -> Unit = { _, _ -> },
    onCanvasDisposed: GLRenderScope.() -> Unit = {},
    onError: (GLException) -> Unit = { e ->
        GLLog.warn("ComposeGL", e.message ?: "GL 初始化失败", e)
    },
    fallbackContent: @Composable () -> Unit = {},
    onDraw: GLRenderScope.() -> Unit,
) {
    var failure by remember(config) { mutableStateOf<GLException?>(null) }

    Box(modifier) {
        when {
            failure != null -> fallbackContent()
            enabled -> PlatformGLCanvas(
                modifier = Modifier.fillMaxSize(),
                config = config,
                framePolicy = framePolicy,
                onCanvasCreated = onCanvasCreated,
                onCanvasResized = onCanvasResized,
                onDraw = onDraw,
                onCanvasDisposed = onCanvasDisposed,
                onFailure = { e ->
                    failure = e
                    onError(e)
                },
                sessionKey = sessionKey,
            )
        }
    }
}

/** 默认会话标识。所有不显式指定 sessionKey 的 GLCanvas 共用同一个上下文。 */
const val DEFAULT_SESSION_KEY: String = "default"
