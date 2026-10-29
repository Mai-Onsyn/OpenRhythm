package mai_onsyn.compose_gl.internal

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import mai_onsyn.compose_gl.ComposeGL
import mai_onsyn.compose_gl.config.AndroidSurfaceMode
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.android.AndroidCallbacks
import mai_onsyn.compose_gl.internal.android.AndroidRenderSurface
import mai_onsyn.compose_gl.internal.android.compose.AndroidImageHost
import mai_onsyn.compose_gl.internal.android.view.GLTextureView
import mai_onsyn.compose_gl.scope.GLRenderScope

/**
 * 应用是否处于 STARTED 之后（前台可见）。
 *
 * 退到后台时停止出帧，避免后台空转耗电；恢复时自动继续。
 * 从 `LocalContext` 往上找 `LifecycleOwner`，不额外引入 lifecycle-compose 依赖。
 */
@Composable
private fun rememberLifecycleStarted(): Boolean {
    val context = LocalContext.current
    val owner = remember(context) { context.findLifecycleOwner() }
    var started by remember(owner) {
        mutableStateOf(owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) ?: true)
    }
    DisposableEffect(owner) {
        val lifecycle = owner?.lifecycle
        if (lifecycle == null) return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, _ ->
            started = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycle.addObserver(observer)
        started = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return started
}

private fun Context.findLifecycleOwner(): LifecycleOwner? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is LifecycleOwner) return current
        current = current.baseContext
    }
    return current as? LifecycleOwner
}

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
    // 启动期一次性开关：只在首次进入组合时读取，运行中改动不会生效
    val mode = remember { ComposeGL.androidSurfaceMode }
    val lifecycleStarted = rememberLifecycleStarted()
    val density = LocalDensity.current.density
    val refreshRateHz = rememberDisplayRefreshRateHz()
    val host = remember { AndroidImageHost() }
    val surface = remember(config, mode) {
        val callbacks = AndroidCallbacks().apply {
            created = onSurfaceCreated
            resized = onSurfaceResized
            draw = onDraw
            destroyed = onSurfaceDestroyed
            failure = onFailure
        }
        AndroidRenderSurface(config, sessionKey, framePolicy, mode, callbacks, host)
            .also { it.density = density; it.refreshRateHz = refreshRateHz }
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
        surface.paused = !lifecycleStarted
    }

    DisposableEffect(surface) {
        surface.start()
        onDispose { surface.dispose() }
    }

    when (mode) {
        AndroidSurfaceMode.TextureView -> AndroidView(
            factory = { context -> GLTextureView(context).also { surface.attach(it) } },
            // surface 实例被重建时（如改了 config）factory 不会再跑，必须在这里重新接管
            update = { view -> surface.attach(view) },
            modifier = modifier,
        )

        AndroidSurfaceMode.ComposeImage -> Box(
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
                    // GL 原点在左下、Bitmap 在左上，在这里翻转（回读侧翻转会破坏零拷贝）
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
}
