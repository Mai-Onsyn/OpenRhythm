package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FlatSlider(
    modifier: Modifier = Modifier,
    progress: Float,
    onChanged: (Float) -> Unit,
    trackColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    conerRadius: Int = 50,
    scalePadding: Dp = 4.dp,
    border: BorderStroke? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
//            .background(Color.Yellow)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        for (change in event.changes) {
                            if (change.pressed) {
                                onChanged(change.position.x / size.width)
                            }
                        }
                    }
                }
                detectDragGestures(
                    onDrag = { change, _ ->
                        onChanged(change.position.x / size.width)
                    },
                    onDragStart = {
                        onChanged(it.x / size.width)
                    }
                )
            }
    ) {
        val padding by animateDpAsState(targetValue = if (hovered) 0.dp else scalePadding)
        Canvas(
            modifier = Modifier
                .padding(padding)
                .clip(RoundedCornerShape(percent = conerRadius))
                .fillMaxSize()
                .background(trackColor)
        ) {
            val coner = conerRadius / 100f * size.height
            drawRoundRect(
                color = contentColor,
                size = size.copy(width = size.width * progress),
                cornerRadius = CornerRadius(coner, coner),
            )
        }
    }
}