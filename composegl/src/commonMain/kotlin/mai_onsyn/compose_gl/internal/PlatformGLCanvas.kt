package mai_onsyn.compose_gl.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.scope.GLRenderScope

/** 平台实现：接上真正的 GL 上下文并把画面合成进 Compose。 */
@Composable
internal expect fun PlatformGLCanvas(
    modifier: Modifier,
    config: GLConfig,
    framePolicy: FramePolicy,
    onSurfaceCreated: (GLRenderScope) -> Unit,
    onSurfaceResized: (GLRenderScope, IntSize) -> Unit,
    onDraw: (GLRenderScope) -> Unit,
    onSurfaceDestroyed: (GLRenderScope) -> Unit,
    onFailure: (GLException) -> Unit,
    sessionKey: String,
)
