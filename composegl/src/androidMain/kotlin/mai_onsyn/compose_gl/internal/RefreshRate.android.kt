package mai_onsyn.compose_gl.internal

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 安卓端的刷新率。
 *
 * 取**面板支持的最高刷新率**，而不是 `display.refreshRate` —— 后者返回当前模式，
 * 120Hz 屏在自适应/省电策略下空闲态常常仍报 60，会导致连续渲染被钉在 60fps。
 * 我们本来就是"能跑多快跑多快"，按最高档定周期更符合语义；
 * 实际出帧节奏仍会被 SurfaceFlinger 的缓冲队列自然限流到屏幕真实刷新率。
 */
@Composable
internal actual fun rememberDisplayRefreshRateHz(): Float {
    val context = LocalContext.current
    return remember(context) {
        sanitizeRefreshRate(
            runCatching { maxSupportedRefreshRate(context) }.getOrNull() ?: 60f,
        )
    }
}

private fun maxSupportedRefreshRate(context: Context): Float {
    val display = context.findDisplay() ?: return 60f
    val modes = runCatching { display.supportedModes }.getOrNull()
    if (!modes.isNullOrEmpty()) {
        return modes.maxOf { it.refreshRate }
    }
    @Suppress("DEPRECATION")
    return display.refreshRate
}

@Suppress("UNNECESSARY_SAFE_CALL") // 类型上非空，但 Application context 运行时确实可能给 null
private fun Context.findDisplay(): Display? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.let { return it }
    }
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    @Suppress("DEPRECATION")
    return manager.defaultDisplay
}
