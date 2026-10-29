package mai_onsyn.compose_gl.internal

import androidx.compose.runtime.Composable

/**
 * 当前显示器的刷新率（Hz）。
 *
 * `FramePolicy.Continuous(maxFps = 0)` 用它来定帧周期，这样 120Hz 屏上就不会被钉死在 60fps。
 * 读不到时回退 60。
 */
@Composable
internal expect fun rememberDisplayRefreshRateHz(): Float

/** 把刷新率收敛到合理区间，防止驱动报出 0 或离谱值。 */
internal fun sanitizeRefreshRate(hz: Float): Float =
    if (hz.isFinite() && hz in 24f..500f) hz else 60f

/** 按目标帧率算出一帧的周期（纳秒）。 */
internal fun framePeriodNanos(fps: Float): Long =
    (1_000_000_000.0 / sanitizeRefreshRate(fps)).toLong()
