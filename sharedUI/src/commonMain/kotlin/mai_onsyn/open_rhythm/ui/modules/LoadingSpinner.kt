package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier.size(48.dp),
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 6.dp,
    durationMillis: Int = 1000
) {
    val transition = rememberInfiniteTransition(label = "loading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawCircle(
            color = colorScheme.surfaceContainerHigh,
            style = Stroke(width = strokeWidth.toPx())
        )

        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}