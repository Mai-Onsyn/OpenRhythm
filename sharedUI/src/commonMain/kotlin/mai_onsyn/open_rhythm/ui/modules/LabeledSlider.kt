package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mai_onsyn.open_rhythm.ui.utility.drawTextCentered
import mai_onsyn.open_rhythm.ui.utility.rememberTextLayoutResult
import kotlin.math.abs
import kotlin.math.roundToInt

private val tooltipIconPath = PathParser().parsePathString(
    "M12,22 L9.33,18 H4 Q3.18,18 2.59,17.41 T2,16 V4 Q2,3.17 2.59,2.59 T4,2 H20 Q20.83,2 21.41,2.59 T22,4 V16 Q22,16.82 21.41,17.41 T20,18 H14.68 L12,22 Z"
).toPath().apply { translate(Offset(-12f, -12f)) }
private val labelFontSize = 10.sp

@Composable
fun LabeledSlider(
    value: Int,
    range: IntRange,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
    onSlidStart: (Int) -> Unit = {},
    onSlidStop: () -> Unit = {},
    valueMapping: (Int) -> String = { it.toString() },
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalTicks = (range.last - range.first).toFloat()

    var progress by remember { mutableStateOf(((value - range.first) / totalTicks).coerceIn(0f, 1f) ) }
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
        val textLayoutResult = rememberTextLayoutResult(
            text = valueMapping(value),
            fontSize = labelFontSize,
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
                .then(drawLabelModifier(
                    interactingAlpha,
                    animatedProgress,
                    colorScheme.primaryContainer,
                    textLayoutResult
                ))
        )
    }
}

@Composable
fun SliderWithSuffix(
    value: Int,
    range: IntRange,
    steps: Int = 1,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
    onSlidStart: (Int) -> Unit = {},
    onSlidStop: () -> Unit = {},
    extraSuffix: String? = null,
    valueMapping: (Int) -> String = { it.toString() },
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabeledSlider(
            value, range, steps, Modifier.weight(1f), onValueChanged, onSlidStart, onSlidStop, valueMapping
        )
        Spacer(Modifier.width(10.dp))
        LabelRow(valueMapping(value), extraSuffix)
    }
}

@Composable
fun LabeledRangeSlider(
    lValue: Int,
    rValue: Int,
    range: IntRange,
    steps: Int = 1,     // TODO("未重组时改变该值无效")
    minLength: Int = 0, // TODO("未重组时改变该值无效")
    modifier: Modifier = Modifier,
    onLValueChanged: (Int) -> Unit = {},
    onRValueChanged: (Int) -> Unit = {},
    valueMapping: (Int) -> String = { it.toString() }
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalTicks = (range.last - range.first).toFloat()
    val currentRange by rememberUpdatedState(lValue..rValue)

    var lProgress by remember { mutableStateOf(
        ((lValue - range.first) / totalTicks).coerceIn(0f, 1f)
    ) }
    var rProgress by remember { mutableStateOf(
        ((rValue - range.first) / totalTicks).coerceIn(0f, 1f)
    ) }
    val animatedLProgress by animateFloatAsState(lProgress)
    val animatedRProgress by animateFloatAsState(rProgress)

    var lActive by remember { mutableStateOf(0) }   // 0=false 1=true 2=transparent
    var rActive by remember { mutableStateOf(0) }

    fun mappingValue(active: Int): Float = when (active) {
        1 -> 1f
        2 -> 0.6f
        else -> 0f
    }

    val lInteractAnimate by animateFloatAsState(mappingValue(lActive))
    val rInteractAnimate by animateFloatAsState(mappingValue(rActive))

    var sliderPressing by remember { mutableStateOf(false) }
    var sliderHovering by remember { mutableStateOf(false) }

    if (!sliderPressing && !sliderHovering) {
        lActive = 0
        rActive = 0
    }

    fun testActive(percent: Float, pressed: Boolean, inTrack: Boolean) {
        val dl = abs(lProgress - percent)
        val dr = abs(rProgress - percent)
        if (inTrack) {   // between range
            if (pressed) {
                lActive = 1
                rActive = 1
            } else {
                if (dl < dr) {
                    lActive = 1
                    rActive = 2
                } else {
                    lActive = 2
                    rActive = 1
                }
            }
        } else {
            if (dl < dr) {
                lActive = 1
                rActive = 0
            } else {
                lActive = 0
                rActive = 1
            }
        }
    }

    // 在长周期lambda内执行的 下同 steps和minLength不会改变 但问题不大
    fun moveThumb(percent: Float, thumb: Int) {
        val nearest = range.closestTick(steps, percent)
        if (thumb == 0) {
            if (nearest > currentRange.last - minLength) return
            lProgress = (nearest - range.first) / totalTicks
            onLValueChanged(nearest)
        } else if (thumb == 1) {
            if (nearest < currentRange.first + minLength) return
            rProgress = (nearest - range.first) / totalTicks
            onRValueChanged(nearest)
        }
    }

    fun moveTrack(currPercent: Float, startPercent: Float, dragStartRange: IntRange) {
        val currNearest = range.closestTick(steps, currPercent)
        val startNearest = range.closestTick(steps, startPercent)

        val delta = currNearest - startNearest
        val lN = dragStartRange.first + delta
        val rN = dragStartRange.last + delta
        if (lN < range.first || rN > range.last) return
        lProgress = (lN - range.first) / totalTicks
        rProgress = (rN - range.first) / totalTicks
        onLValueChanged(lN)
        onRValueChanged(rN)
    }

    Box(
        modifier = modifier
            .height(20.dp)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    val thumbPxPercent = 10.dp.toPx() / size.width

                    var isDragging = false
                    var startOffset = Offset.Zero
                    var startRange = currentRange

                    var interactionMethod = 0   // 0=boundary 1=track
                    var draggingThumb = 0
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue

                        val percent = coerceBound(change.position.x, size.width)
                        val inTrack = lProgress + thumbPxPercent < percent && percent + thumbPxPercent < rProgress
                        val startPercent = coerceBound(startOffset.x, size.width)
                        testActive(percent, change.pressed, inTrack)
                        when (event.type) {
                            PointerEventType.Move -> {
                                isDragging = change.pressed
                                if (isDragging) {
                                    when (interactionMethod) {
                                        0 -> moveThumb(percent, draggingThumb)
                                        1 -> {
                                            testActive(percent, change.pressed, true)
                                            moveTrack(percent, startPercent, startRange)
                                        }
                                    }
                                }
                            }
                            PointerEventType.Press -> {
                                startOffset = change.position
                                startRange = currentRange

                                interactionMethod = if (inTrack) 1 else 0
                                val dl = abs(lProgress - percent)
                                val dr = abs(rProgress - percent)
                                draggingThumb = if (dl < dr) 0 else 1
                            }
                            PointerEventType.Release -> {
                                if (!isDragging) moveThumb(percent, draggingThumb)
                                isDragging = false
                                startOffset = Offset.Zero
                            }
                            PointerEventType.Enter -> sliderHovering = true
                            PointerEventType.Exit -> sliderHovering = false
                        }
                        change.consume()
                        sliderPressing = event.changes.any { it.pressed }
                    }
                }
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
                topLeft = Offset(size.width * animatedLProgress, 0f),
                size = size.copy(width = size.width * (animatedRProgress - animatedLProgress)),
                cornerRadius = CornerRadius(size.height / 2),
            )
        }
        val textLayoutResults = rememberTextLayoutResult(
            texts = listOf(valueMapping(lValue), valueMapping(rValue)),
            fontSize = labelFontSize,
            color = colorScheme.onPrimaryContainer,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val lCenter = Offset(size.width * animatedLProgress, size.height / 2)
                    val rCenter = Offset(size.width * animatedRProgress, size.height / 2)
                    val lHoverScale = 1f + 0.14f * lInteractAnimate
                    val rHoverScale = 1f + 0.14f * rInteractAnimate
                    val lShadow = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        center = lCenter,
                        radius = size.height / 1.6f * lHoverScale
                    )
                    val rShadow = Brush.radialGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        center = rCenter,
                        radius = size.height / 1.6f * rHoverScale
                    )
                    onDrawBehind {
                        drawCircle(
                            brush = lShadow,
                            radius = size.height * 0.667f * lHoverScale,
                            center = lCenter
                        )
                        drawCircle(
                            brush = rShadow,
                            radius = size.height * 0.667f * rHoverScale,
                            center = rCenter
                        )
                        drawCircle(
                            color = colorScheme.primary,
                            radius = size.height * 0.5f * lHoverScale,
                            center = lCenter
                        )
                        drawCircle(
                            color = colorScheme.primary,
                            radius = size.height * 0.5f * rHoverScale,
                            center = rCenter
                        )
                    }
                }
                .then(drawLabelModifier(
                    lInteractAnimate,
                    animatedLProgress,
                    colorScheme.primaryContainer,
                    textLayoutResults[0]
                ))
                .then(drawLabelModifier(
                    rInteractAnimate,
                    animatedRProgress,
                    colorScheme.primaryContainer,
                    textLayoutResults[1]
                ))
        )
    }
}

@Composable
fun LabeledSliderWithPrefixSuffix(
    lValue: Int,
    rValue: Int,
    range: IntRange,
    steps: Int = 1,
    minLength: Int = 0,
    modifier: Modifier = Modifier,
    onLValueChanged: (Int) -> Unit = {},
    onRValueChanged: (Int) -> Unit = {},
    extraPrefix: String? = null,
    extraSuffix: String? = null,
    valueMapping: (Int) -> String = { it.toString() }
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelRow(valueMapping(lValue), extraPrefix)
        Spacer(Modifier.width(10.dp))
        LabeledRangeSlider(lValue, rValue, range, steps, minLength, Modifier.weight(1f), onLValueChanged, onRValueChanged, valueMapping)

        Spacer(Modifier.width(10.dp))
        LabelRow(valueMapping(rValue), extraSuffix)
    }
}

@Composable
private fun LabelRow(value: String, extraSuffix: String?) {
    Row(Modifier.width(44.dp), horizontalArrangement = Arrangement.Center) {
        Text(
            text = value,
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

private fun drawLabelModifier(
    animateProgress: Float,
    percent: Float,
    containerColor: Color,
    textLayoutResult: TextLayoutResult
): Modifier = if (animateProgress > 0f) Modifier.drawWithCache {
    val scaleFactor = 2f * density
//    val offsetY = -12f

    val matrix = Matrix()
    matrix.scale(scaleFactor, scaleFactor)
    matrix.translate(size.width * percent / scaleFactor, -12f)
    onDrawBehind {
        withTransform({
            transform(matrix)
        }) {
            drawPath(
                path = tooltipIconPath,
                color = containerColor,
                alpha = animateProgress
            )
            drawTextCentered(
                textLayoutResult,
                center = Offset(0f, -3f),
                alpha = animateProgress
            )
        }
    }
} else Modifier

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