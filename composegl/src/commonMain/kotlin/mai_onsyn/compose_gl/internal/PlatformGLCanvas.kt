package mai_onsyn.compose_gl.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.scope.GLRenderScope

/**
 * 平台实现：接上真正的 GL 上下文并把画面合成进 Compose。
 *
 * 回调语义见 [mai_onsyn.compose_gl.GLCanvas]：`onCanvasCreated` / `onCanvasDisposed`
 * 是**每次挂载/卸载**，上下文级回调（`ComposeGL.onContextCreated`）由实现内部触发。
 */
@Composable
internal expect fun PlatformGLCanvas(
    modifier: Modifier,
    config: GLConfig,
    framePolicy: FramePolicy,
    onCanvasCreated: GLRenderScope.() -> Unit,
    onCanvasResized: GLRenderScope.(IntSize) -> Unit,
    onDraw: GLRenderScope.() -> Unit,
    onCanvasDisposed: GLRenderScope.() -> Unit,
    onFailure: (GLException) -> Unit,
    sessionKey: String,
)
