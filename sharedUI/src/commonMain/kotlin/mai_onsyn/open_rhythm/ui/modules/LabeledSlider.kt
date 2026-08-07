package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.ui.utility.drawTextCentered
import mai_onsyn.open_rhythm.ui.utility.rememberTextLayoutResult
import kotlin.math.roundToInt

@Composable
fun LabeledSlider(
    value: Int,
    range: IntRange,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
    onSlidStart: (Int) -> Unit = {},
    onSlidStop: () -> Unit = {},
    valueMapping: (Int) -> Int = { it },
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalTicks = (range.last - range.first).toFloat()

    var progress by remember { mutableStateOf(value / totalTicks) }
    val animatedProgress by animateFloatAsState(progress)

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val hoveredScale by animateFloatAsState(if (hovered) 1.14f else 1f)

    var isDragging by remember { mutableStateOf(false) }
    val interactActive by derivedStateOf { hovered || isDragging }
    val interactingAlpha by animateFloatAsState(if (interactActive) 1f else 0f)

    val innerValue by rememberUpdatedState(value)
    Box(
        modifier = modifier
            .height(20.dp)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val percent = coerceBound(it.x, size.width)
                        val nearest = range.closestTick(steps, percent)
                        progress = (nearest - range.first) / totalTicks
                        onSlidStart(nearest)
                        onValueChanged(nearest)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        val percent = coerceBound(it.x, size.width)
                        val nearest = range.closestTick(steps, percent)
                        progress = (nearest - range.first) / totalTicks
                        onSlidStart(nearest)
                        onValueChanged(nearest)
                        isDragging = true
                    },
                    onDragEnd = {
                        onSlidStop()
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        progress = coerceBound(change.position.x, size.width)
                        val nearest = range.closestTick(steps, progress)
                        progress = (nearest - range.first) / totalTicks
//                        Logger.v { "Nearest ticks: $nearest, value: $innerValue" }
                        if (innerValue != nearest) onValueChanged(nearest)
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.Center)
        ) {
            drawRoundRect(
                color = colorScheme.surfaceContainerHighest,
                size = size,
                cornerRadius = CornerRadius(size.height / 2),
            )
            drawRoundRect(
                color = colorScheme.primary,
                size = size.copy(width = size.width * animatedProgress),
                cornerRadius = CornerRadius(size.height / 2),
            )
        }
        val density = LocalDensity.current
        val textLayoutResult = rememberTextLayoutResult(
            text = valueMapping(value).toString(),
            fontSize = (5.5 * density.density).sp,
            color = colorScheme.onPrimaryContainer,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val center = Offset(size.width * animatedProgress, size.height / 2)
                    val shadow = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        center = center,
                        radius = size.height / 1.6f * hoveredScale
                    )
                    onDrawBehind {
                        drawCircle(
                            brush = shadow,
                            radius = size.height / 1.5f * hoveredScale,
                            center = center
                        )
                        drawCircle(
                            color = colorScheme.primary,
                            radius = size.height / 2 * hoveredScale,
                            center = center
                        )

                    }
                }
                .then(
                    if (interactingAlpha > 0f) Modifier.drawWithCache {
                        val tooltipIconPath = PathParser().parsePathString(
                            "M12,22 L9.33,18 H4 Q3.18,18 2.59,17.41 T2,16 V4 Q2,3.17 2.59,2.59 T4,2 H20 Q20.83,2 21.41,2.59 T22,4 V16 Q22,16.82 21.41,17.41 T20,18 H14.68 L12,22 Z"
                        ).toPath().apply {
                            translate(Offset(-12f, -12f))
                        }
                        val scaleFactor = 2f * density.density
                        val offsetY = -(12.dp).toPx()
                        onDrawBehind {
                            withTransform({
                                val matrix = Matrix()
                                matrix.scale(scaleFactor, scaleFactor)
                                matrix.translate(size.width * animatedProgress / scaleFactor, offsetY)
                                transform(matrix)
                            }) {
                                drawPath(
                                    path = tooltipIconPath,
                                    color = colorScheme.primaryContainer,
                                    alpha = interactingAlpha
                                )
                                drawTextCentered(
                                    textLayoutResult,
                                    center = Offset(0f, offsetY * 0.15f),
                                    alpha = interactingAlpha
                                )
                            }
                        }
                    } else Modifier
                )
        )
    }
}

@Composable
fun SliderWithSuffix(
    value: Int,
    range: IntRange,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
    onSlidStart: (Int) -> Unit = {},
    onSlidStop: () -> Unit = {},
    extraSuffix: String? = null,
    valueMapping: (Int) -> Int = { it },
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LabeledSlider(
            value, range, steps, Modifier.weight(1f), onValueChanged, onSlidStart, onSlidStop, valueMapping
        )
        Spacer(Modifier.width(16.dp))
        Row(Modifier.width(44.dp), horizontalArrangement = Arrangement.Center) {
            Text(
                text = valueMapping(value).toString(),
                style = MaterialTheme.typography.labelMedium
            )
            extraSuffix?.let {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun coerceBound(pos: Float, width: Int): Float {
    if (pos < 1e-4f) return 0f
    if (pos > width - 1e-4f) return 1f
    return pos / width
}

private fun IntRange.closestTick(step: Int, percent: Float): Int {
    require(first <= last) { "Range must not be empty" }
    require(step > 0) { "Step must be positive" }

    val p = percent.coerceIn(0f, 1f)
    val maxIndex = (last - first) / step

    // 连续空间中的理想索引
    val target = first + (last - first) * p
    val idealIndex = (target - first) / step

    val roundIdx = idealIndex.roundToInt().coerceIn(0, maxIndex)

    return first + roundIdx * step
}