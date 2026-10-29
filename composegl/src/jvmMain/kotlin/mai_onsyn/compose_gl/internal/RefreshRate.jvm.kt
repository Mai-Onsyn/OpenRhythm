package mai_onsyn.compose_gl.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.GraphicsEnvironment

@Composable
internal actual fun rememberDisplayRefreshRateHz(): Float = remember {
    sanitizeRefreshRate(
        runCatching {
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .defaultScreenDevice
                .displayMode
                .refreshRate
                .toFloat()
        }.getOrDefault(60f),
    )
}
