package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.AppCursors
import mai_onsyn.open_rhythm.ui.utility.blackKeyOffset
import mai_onsyn.open_rhythm.ui.utility.countWhiteKeys
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import kotlin.collections.iterator
import kotlin.collections.mutableMapOf


@Composable
fun MidiKeyBoard(
    modifier: Modifier = Modifier,
    minPitch: Int = 21,     // A0 default
    maxPitch: Int = 108,    // C8 default
    midiActiveKey: Map<Int, Color> = emptyMap(),
    userActiveKey: Map<Int, Color> = emptyMap(),
    blackVerticalPercentage: Float = 0.64f,     // 黑键底部y在整个键盘高度的比例
    blackHorizontalPercentage: Float = 0.75f,   // 单个黑键宽度相对于白键宽度的比例
    spacing: Dp = 1.dp,
    darkPart: Color = Color.Black,
    onPress: (Int, Int) -> Unit = { pitch, velocity -> },
    onRelease: (Int) -> Unit = {},
    onVerticalDragged: (Float) -> Unit = {}
) {
    require(minPitch >= 0 && maxPitch <= 127) { "Pitch out of range [0, 127]" }
    require(minPitch <= maxPitch) { "minPitch must be <= maxPitch [$minPitch, $maxPitch]" }
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)

    val keyRegions = remember(minPitch, maxPitch, blackVerticalPercentage, blackHorizontalPercentage, spacing) {
        // (blackKeys, whiteKeys)
        mutableStateOf(Pair(emptyList<Pair<Rect, Int>>(), emptyList<Pair<Rect, Int>>()))
    }
    val activeKey = midiActiveKey + userActiveKey

    val spacingPx = with(density) { spacing.toPx() }
    val offsetStartY = with(density) { 12.dp.toPx() }
    val endPadding = with(density) { 4.dp.toPx() }

    val focusRequester = remember { FocusRequester() }
    val pointerPressedKey = mutableSetOf<Int>()

    var currentCursor by remember { mutableStateOf(PointerIcon.Default) }
    Canvas(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onSizeChanged { size ->
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val whiteKeyWidth = (width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount

                val wRects = mutableListOf<Pair<Rect, Int>>()
                val bRects = mutableListOf<Pair<Rect, Int>>()

                // 白键 Rect
                for (pitch in minPitch..maxPitch) {
                    if (!isBlackKey(pitch)) {
                        val x = (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacingPx)
                        val topLeft = Offset(x, offsetStartY)
                        val keySize = Size(whiteKeyWidth, height - offsetStartY - endPadding)
                        wRects.add(Pair(Rect(topLeft, keySize), pitch))
                    }
                }
                // 黑键 Rect
                for (pitch in minPitch..maxPitch) {
                    if (isBlackKey(pitch)) {
                        val offsetPercent = blackKeyOffset(pitch)
                        val centerX = (countWhiteKeys(minPitch, pitch)) * (whiteKeyWidth + spacingPx) - spacingPx / 2
                        val blackBaseSize = Size(whiteKeyWidth * blackHorizontalPercentage, (height - offsetStartY - endPadding) * blackVerticalPercentage)
                        val blackBaseOffset = Offset(centerX - blackBaseSize.width / 2 + blackBaseSize.width * offsetPercent, offsetStartY)
                        bRects.add(Pair(Rect(blackBaseOffset, blackBaseSize), pitch))
                    }
                }
                keyRegions.value = Pair(bRects, wRects)
            }
            .pointerHoverIcon(currentCursor)
            .pointerInput(keyRegions) {
                awaitPointerEventScope {
                    var inHeightRegionPressed = false
                    var lastCursorPressed = false
                    while (true) {
                        val event = awaitPointerEvent()

                        // =========== Height adjust region ==========
                        var inAdjust = false
                        val firstChange = event.changes.first()
                        val activeRect = Rect(Offset.Zero, Size(size.width.toFloat(), 8.dp.toPx()))
                        if (firstChange.position in activeRect) {
                            currentCursor = AppCursors.verticalResize
                        } else currentCursor = PointerIcon.Default
                        if (inHeightRegionPressed) {
                            onVerticalDragged(firstChange.position.y - firstChange.previousPosition.y)
                            inAdjust = true
                        }
                        if (firstChange.pressed && !lastCursorPressed) {
                            lastCursorPressed = true
                            if (firstChange.position in activeRect)
                                inHeightRegionPressed = true
                        } else if (!firstChange.pressed && lastCursorPressed) {
                            lastCursorPressed = false
                            inHeightRegionPressed = false
                        }
                        if (inAdjust) continue
                        // =========== Height adjust region ==========

                        val currentPressedKeys = mutableMapOf<Int, Float>()
                        val (blackKeyRect, whiteKeyRect) = keyRegions.value
                        for (change in event.changes) {
                            if (change.pressed) {
                                var findInBlackRegion = false
                                for (blackKeyRegion in blackKeyRect) {
                                    if (change.position in blackKeyRegion.first) {
                                        currentPressedKeys[blackKeyRegion.second] = (change.position.y - offsetStartY) / blackKeyRegion.first.height
                                        findInBlackRegion = true
                                        break
                                    }
                                }
                                if (!findInBlackRegion) {
                                    for (whiteKeyRegion in whiteKeyRect) {
                                        if (change.position in whiteKeyRegion.first) {
                                            currentPressedKeys[whiteKeyRegion.second] = (change.position.y - offsetStartY) / whiteKeyRegion.first.height
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        for (currentKey in currentPressedKeys) {
                            if (!pointerPressedKey.contains(currentKey.key)) {
                                pointerPressedKey.add(currentKey.key)
                                onPress(currentKey.key, (currentKey.value * 127).toInt())
                                focusRequester.requestFocus()
                            }
                        }
                        val userIterator = pointerPressedKey.iterator()
                        while (userIterator.hasNext()) {
                            val userKey = userIterator.next()
                            if (!currentPressedKeys.contains(userKey)) {
                                userIterator.remove()   // 安全删除
                                onRelease(userKey)
                            }
                        }
                    }
                }
            }
            .onFocusChanged {
                if (!it.isFocused) {
//                    keyboardPressedKey.forEach { key -> onRelease(key) }
//                    keyboardPressedKey.clear()

                    pointerPressedKey.forEach { key -> onRelease(key) }
                    pointerPressedKey.clear()
                }
            }
    ) {
        val whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacing.toPx()) / whiteKeyCount

        drawRect(
            color = colorScheme.surfaceContainer,
            size = Size(size.width, 8.dp.toPx())
        )
        drawRect(
            color = Color(160, 32, 32),
            topLeft = Offset(0f, 8.dp.toPx()),
            size = Size(size.width, 4.dp.toPx())
        )

        val offsetStartY = 12.dp.toPx()

        // 白键
        for (pitch in minPitch..maxPitch) {
            if (!isBlackKey(pitch)) {
                val x = (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacing.toPx())
                val rect = keyRegions.value.second[keyRegions.value.second.binarySearchBy(pitch) { it.second }].first
                val isActive = activeKey.containsKey(pitch)
                drawRoundedBottomShape(
                    topLeft = rect.topLeft,
                    size = if (isActive) Size(rect.size.width, rect.size.height + endPadding * 0.6f) else rect.size,
                    rx = whiteKeyWidth * 0.3f,
                    ry = whiteKeyWidth * 0.15f,
                    color = if (isActive) activeKey[pitch]!! else Color.White
                )
                if (pitch != maxPitch) {
                    drawRect(   // 分割线
                        color = darkPart,
                        topLeft = Offset(x + whiteKeyWidth, offsetStartY),
                        size = Size(spacing.toPx(), size.height - offsetStartY)
                    )
                }
            }
        }
        // 黑键
        for (pitch in minPitch..maxPitch) {
            if (isBlackKey(pitch)) {
                val rect = keyRegions.value.first[keyRegions.value.first.binarySearchBy(pitch) { it.second }].first
                val radiusUnit = whiteKeyWidth * 0.03f
                drawRoundedBottomShape(
                    color = darkPart,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    rx = radiusUnit,
                    ry = radiusUnit
                )

                drawRoundedBottomShape(
                    color = if (activeKey.containsKey(pitch)) {
                        val targetColor = activeKey[pitch]!!
                        Color(targetColor.red, targetColor.green, targetColor.blue, 0.8f).compositeOver(Color.Black)
                    } else Color.Black,
                    topLeft = Offset(rect.topLeft.x + rect.size.width * 0.07f, rect.topLeft.y),
                    size = Size(rect.size.width * 0.86f, rect.size.height - rect.size.width * 0.1f),
                    rx = radiusUnit * 4,
                    ry = radiusUnit * 2
                )
            }
        }
    }
}

private fun DrawScope.drawRoundedBottomShape(
    color: Color = Color.White,
    topLeft: Offset,
    size: Size,
    rx: Float,
    ry: Float
) {
    val left = topLeft.x
    val top = topLeft.y
    val right = left + size.width
    val bottom = top + size.height

    val path = Path().apply {
        moveTo(left, top)
        lineTo(right, top)
        lineTo(right, bottom - ry)
        cubicTo(
            x1 = right, y1 = bottom - ry * 0.45f,
            x2 = right - rx * 0.45f, y2 = bottom,
            x3 = right - rx, y3 = bottom
        )
        lineTo(left + rx, bottom)
        cubicTo(
            x1 = left + rx * 0.45f, y1 = bottom,
            x2 = left, y2 = bottom - ry * 0.45f,
            x3 = left, y3 = bottom - ry
        )
        close()
    }
    drawPath(path = path, color = color)
}