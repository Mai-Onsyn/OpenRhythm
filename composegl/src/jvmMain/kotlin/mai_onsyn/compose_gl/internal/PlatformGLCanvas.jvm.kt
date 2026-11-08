package mai_onsyn.compose_gl.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Rect
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.ComposeGL
import mai_onsyn.compose_gl.GLCanvasHandle
import mai_onsyn.compose_gl.config.DesktopSurfaceMode
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.desktop.DesktopCallbacks
import mai_onsyn.compose_gl.internal.desktop.DesktopRenderSurface
import mai_onsyn.compose_gl.internal.desktop.compose.ComposeImageHost
import mai_onsyn.compose_gl.internal.desktop.compose.FrameHost
import mai_onsyn.compose_gl.internal.desktop.compose.SharedTextureHost
import mai_onsyn.compose_gl.internal.desktop.native.NativeSurfaceGLCanvas
import mai_onsyn.compose_gl.scope.GLRenderScope

@Composable
internal actual fun PlatformGLCanvas(
    modifier: Modifier,
    config: GLConfig,
    framePolicy: FramePolicy,
    handle: GLCanvasHandle?,
    onCanvasCreated: GLRenderScope.() -> Unit,
    onCanvasResized: GLRenderScope.(IntSize) -> Unit,
    onDraw: GLRenderScope.() -> Unit,
    onCanvasDisposed: GLRenderScope.() -> Unit,
    onFailure: (GLException) -> Unit,
    sessionKey: String,
) {
    val mode = ComposeGL.desktopSurfaceMode

    when (mode) {
        DesktopSurfaceMode.NativeSurface -> {
            NativeSurfaceGLCanvas(
                modifier = modifier,
                config = config,
                framePolicy = framePolicy,
                refreshRateHz = rememberDisplayRefreshRateHz(),
                density = LocalDensity.current.density,
                handle = handle,
                // 该模式已确认不可用（互操作层不合成 heavyweight 画布），仅为保留代码路径而适配。
                onSurfaceCreated = { scope -> onCanvasCreated(scope) },
                onSurfaceResized = { scope, size -> onCanvasResized(scope, size) },
                onDraw = { scope -> onDraw(scope) },
                onSurfaceDestroyed = { scope -> onCanvasDisposed(scope) },
                onFailure = onFailure,
            )
            return
        }

        DesktopSurfaceMode.ComposeImage, DesktopSurfaceMode.SharedTexture -> Unit
    }

    val sharedTexture = mode == DesktopSurfaceMode.SharedTexture
    val density = LocalDensity.current.density
    val refreshRateHz = rememberDisplayRefreshRateHz()
    val host: FrameHost = remember(sessionKey, sharedTexture) {
        if (sharedTexture) SharedTextureHost.obtain(sessionKey) else ComposeImageHost()
    }
    val surface = remember(config, sharedTexture) {
        val callbacks = DesktopCallbacks().apply {
            canvasCreated = { scope -> onCanvasCreated(scope) }
            resized = { scope, size -> onCanvasResized(scope, size) }
            draw = { scope -> onDraw(scope) }
            canvasDisposed = { scope -> onCanvasDisposed(scope) }
            failure = onFailure
        }
        DesktopRenderSurface(
            config = config,
            sessionKey = sessionKey,
            framePolicy = framePolicy,
            host = host,
            callbacks = callbacks,
            refreshRateHz = refreshRateHz,
            sharedTexture = sharedTexture,
        ).also { it.density = density }
    }

    SideEffect {
        surface.callbacks.canvasCreated = { scope -> onCanvasCreated(scope) }
        surface.callbacks.resized = { scope, size -> onCanvasResized(scope, size) }
        surface.callbacks.draw = { scope -> onDraw(scope) }
        surface.callbacks.canvasDisposed = { scope -> onCanvasDisposed(scope) }
        surface.callbacks.failure = onFailure
        surface.density = density
        surface.framePolicy = framePolicy
        surface.refreshRateHz = refreshRateHz
        // 后端/影子类没生效时让界面直接显示 fallback，而不是白屏 + 静默涨内存。
        (host as? SharedTextureHost)?.onError = { message ->
            onFailure(GLException(message))
        }
    }

    DisposableEffect(surface) {
        surface.start()
        onDispose {
            // host 按 sessionKey 常驻，这里只让渲染线程退出；重新挂载直接复用它。
            surface.dispose()
        }
    }

    // 句柄的挂载周期跟着 surface 走，但 handle 本身活在调用方的 composition 里。
    // 单独一个 effect：handle 变化不该导致 surface 重建。
    DisposableEffect(surface, handle) {
        handle?.attach(
            request = { surface.requestRender() },
            applyPause = { surface.paused = it },
        )
        onDispose { handle?.detach() }
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
                val frame = host.frame.value ?: return@drawBehind
                if (sharedTexture) {
                    val image = frame as? SkiaImage ?: return@drawBehind
                    // 必须绕开 ImageBitmap：Compose 1.11 的 toComposeImageBitmap() 会走
                    // Image.toBitmap() -> Canvas(bitmap).drawImage(...)，把 GPU 纹理整帧
                    // 光栅化到 CPU 位图。直接把 SkiaImage 交给底层 Skia Canvas 才是零拷贝。
                    val src = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
                    val dst = Rect.makeWH(size.width, size.height)
                    drawIntoCanvas { canvas ->
                        canvas.skiaCanvas.drawImageRect(image, src, dst, null)
                    }
                } else {
                    val image = frame as? ImageBitmap ?: return@drawBehind
                    val dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    // GL 原点在左下、Skia 在左上，翻转放在合成阶段（回读侧翻转会破坏零拷贝）
                    withTransform({ scale(1f, -1f, center) }) {
                        drawImage(image = image, dstOffset = IntOffset.Zero, dstSize = dstSize)
                    }
                }
            },
    )
}
