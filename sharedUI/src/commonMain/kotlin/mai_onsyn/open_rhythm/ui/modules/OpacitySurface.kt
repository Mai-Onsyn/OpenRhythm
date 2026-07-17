package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun OpacitySurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    color: Color = MaterialTheme.colorScheme.primary,
    opacity: Float = 0.2f,
    contentPadding: Dp = 8.dp,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable (() -> Unit)
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color.copy(alpha = opacity),
        contentColor = color,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(contentPadding)
                .minAspectRatioOne()
        ) {
            content()
        }
    }
}

fun Modifier.minAspectRatioOne(): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val contentWidth = placeable.width
    val contentHeight = placeable.height

    val targetWidth = max(contentWidth, contentHeight)

    layout(targetWidth, contentHeight) {
        val x = (targetWidth - contentWidth) / 2
        val y = 0
        placeable.placeRelative(x, y)
    }
}