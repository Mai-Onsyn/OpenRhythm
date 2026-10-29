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
 * 尺寸完全由 [modifier] 决定；库只保证在回调里上下文已 current，
 * 不提供任何绘制辅助。
 *
 * 所有回调都在渲染线程执行：
 * - [onSurfaceCreated] 建 shader/VBO 等资源。**每次挂载都会调用一次**
 *   （每次挂载都是全新的 GL 上下文，对象 id 不能跨挂载复用）
 * - [onSurfaceResized] 重建与尺寸相关的资源
 * - [onDraw] 画一帧（**库不会帮你 glClear**）
 * - [onSurfaceDestroyed] 释放资源
 *
 * @param onDraw 每帧的绘制回调。语义是「这一帧如何重画整个画面」，不是增量补画。
 * @param enabled 为 false 时**彻底销毁**上下文与所有资源，区域留空；重新置 true 会重新初始化。
 *   只是"暂时不画"请用 `FramePolicy` 或 `paused`，不要用它。
 * @param config 渲染配置。**改动会导致 render target 重建**，别在每帧改它。
 * @param framePolicy 帧驱动策略，可在运行中切换（不会重建 surface）。
 * @param fallbackContent GL 不可用时渲染的内容。
 * @param sessionKey GL 会话标识。**同一个 key 的 GLCanvas 共用一个常驻 GL 上下文**：
 *   画布卸载时上下文不销毁，下次挂载直接复用，着色器不用重新编译。
 *   不同 key 各自独立（用不同 GL 资源的画布要给不同 key）。
 */
@Composable
fun GLCanvas(
    onDraw: (GLRenderScope) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: GLConfig = GLConfig(),
    framePolicy: FramePolicy = FramePolicy.OnRequest,
    onSurfaceCreated: (GLRenderScope) -> Unit = {},
    onSurfaceResized: (GLRenderScope, IntSize) -> Unit = { _, _ -> },
    onSurfaceDestroyed: (GLRenderScope) -> Unit = {},
    onError: (GLException) -> Unit = { e ->
        GLLog.warn("ComposeGL", e.message ?: "GL 初始化失败", e)
    },
    fallbackContent: @Composable () -> Unit = {},
    sessionKey: String = DEFAULT_SESSION_KEY,
) {
    var failure by remember(config) { mutableStateOf<GLException?>(null) }

    Box(modifier) {
        when {
            failure != null -> fallbackContent()
            enabled -> PlatformGLCanvas(
                modifier = Modifier.fillMaxSize(),
                config = config,
                framePolicy = framePolicy,
                onSurfaceCreated = onSurfaceCreated,
                onSurfaceResized = onSurfaceResized,
                onDraw = onDraw,
                onSurfaceDestroyed = onSurfaceDestroyed,
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
