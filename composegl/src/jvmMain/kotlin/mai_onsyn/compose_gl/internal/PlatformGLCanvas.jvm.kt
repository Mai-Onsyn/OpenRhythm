package mai_onsyn.compose_gl.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.desktop.DesktopCallbacks
import mai_onsyn.compose_gl.internal.desktop.DesktopRenderSurface
import mai_onsyn.compose_gl.internal.desktop.compose.ComposeImageHost
import mai_onsyn.compose_gl.scope.GLRenderScope

@Composable
internal actual fun PlatformGLCanvas(
    modifier: Modifier,
    config: GLConfig,
    framePolicy: FramePolicy,
    onSurfaceCreated: (GLRenderScope) -> Unit,
    onSurfaceResized: (GLRenderScope, IntSize) -> Unit,
    onDraw: (GLRenderScope) -> Unit,
    onSurfaceDestroyed: (GLRenderScope) -> Unit,
    onFailure: (GLException) -> Unit,
    sessionKey: String,
) {
    val density = LocalDensity.current.density
    val refreshRateHz = rememberDisplayRefreshRateHz()
    val host = remember { ComposeImageHost() }
    val surface = remember(config) {
        val callbacks = DesktopCallbacks().apply {
            created = onSurfaceCreated
            resized = onSurfaceResized
            draw = onDraw
            destroyed = onSurfaceDestroyed
            failure = onFailure
        }
        DesktopRenderSurface(config, sessionKey, framePolicy, host, callbacks, refreshRateHz)
            .also { it.density = density }
    }

    SideEffect {
        surface.callbacks.created = onSurfaceCreated
        surface.callbacks.resized = onSurfaceResized
        surface.callbacks.draw = onDraw
        surface.callbacks.destroyed = onSurfaceDestroyed
        surface.callbacks.failure = onFailure
        surface.density = density
        surface.framePolicy = framePolicy
        surface.refreshRateHz = refreshRateHz
    }

    DisposableEffect(surface) {
        surface.start()
        onDispose { surface.dispose() }
    }

    Box(
        modifier
            .onSizeChanged {
                surface.resize(
                    scaledTargetSize(it.width, config.resolutionScale),
                    scaledTargetSize(it.height, config.resolutionScale),
                )
            }
            .clipToBounds()
            .drawBehind {
                val image = host.frame.value ?: return@drawBehind
                // GL 原点在左下、Skia 在左上，在这里翻转（回读侧翻转会破坏零拷贝）
                withTransform({ scale(1f, -1f, center) }) {
                    drawImage(
                        image = image,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    )
                }
            },
    )
}
