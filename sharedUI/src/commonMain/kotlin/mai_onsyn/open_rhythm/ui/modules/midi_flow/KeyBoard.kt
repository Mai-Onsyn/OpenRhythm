package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.bridge.AppCursors
import mai_onsyn.open_rhythm.ui.utility.*


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
    appendTexts: Map<Int, String> = emptyMap(),
    centerAppendLayer: Boolean = false,
    whiteKeyColor: Color = Color.White,
    blackKeyColor: Color = Color.Black,
    darkPart: Color = Color.Black,
    onPress: (Int, Int) -> Unit = { pitch, velocity -> },
    onRelease: (Int) -> Unit = {},
    draggableAreaColor: Color? = null,
    enableSplitRedLine: Boolean = true,
    onVerticalDragged: (Float) -> Unit = {}
) {
    require(minPitch >= 0 && maxPitch <= 127) { "Pitch out of range [0, 127]" }
    require(minPitch <= maxPitch) { "minPitch must be <= maxPitch [$minPitch, $maxPitch]" }
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)

//    val keyRegions = remember(minPitch, maxPitch, blackVerticalPercentage, blackHorizontalPercentage, spacing) {
//        // (blackKeys, whiteKeys)
//        mutableStateOf(Pair(emptyList<Pair<Rect, Int>>(), emptyList<Pair<Rect, Int>>()))
//    }
    var blackKeyRects by remember { mutableStateOf(emptyMap<Int, Rect>()) }
    var whiteKeyRects by remember { mutableStateOf(emptyMap<Int, Rect>()) }

    val activeKey = midiActiveKey + userActiveKey

    val spacingPx = with(density) { spacing.toPx() }
    val offsetStartY = with(density) { (
            if (draggableAreaColor == null && enableSplitRedLine) 4
            else if (draggableAreaColor != null && enableSplitRedLine) 12
            else if (draggableAreaColor != null && !enableSplitRedLine) 8
            else 0
    ).dp.toPx() }
    val endPadding = with(density) { 4.dp.toPx() }

    val focusRequester = remember { FocusRequester() }
    val pointerPressedKey = mutableSetOf<Int>()

    var whiteKeyWidth by remember { mutableStateOf(0f) }
    val textRenderingItems = rememberTextLayoutResult(
        appendTexts,
        (whiteKeyWidth * 0.3f).sp,
        { if (isBlackKey(it.key)) Color.White else Color.Black }
    )

    var currentCursor by remember { mutableStateOf(PointerIcon.Default) }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    fun reCalcKeyRects() {
        val width = canvasSize.width.toFloat()
        val height = canvasSize.height.toFloat()
        val whiteKeyWidth = (width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount

        val wRects = mutableMapOf<Int, Rect>()
        val bRects = mutableMapOf<Int, Rect>()

        // 白键 Rect
        for (pitch in minPitch..maxPitch) {
            if (!isBlackKey(pitch)) {
                val x = (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacingPx)
                val topLeft = Offset(x, offsetStartY)
                val keySize = Size(whiteKeyWidth, height - offsetStartY - endPadding)
                wRects[pitch] = Rect(topLeft, keySize)
            }
        }
        // 黑键 Rect
        for (pitch in minPitch..maxPitch) {
            if (isBlackKey(pitch)) {
                val offsetPercent = blackKeyOffset(pitch)
                val centerX = (countWhiteKeys(minPitch, pitch)) * (whiteKeyWidth + spacingPx) - spacingPx / 2
                val blackBaseSize = Size(
                    whiteKeyWidth * blackHorizontalPercentage,
                    (height - offsetStartY - endPadding) * blackVerticalPercentage
                )
                val blackBaseOffset =
                    Offset(centerX - blackBaseSize.width / 2 + blackBaseSize.width * offsetPercent, offsetStartY)
                bRects[pitch] = Rect(blackBaseOffset, blackBaseSize)
            }
        }
        whiteKeyRects = wRects
        blackKeyRects = bRects
    }
    LaunchedEffect(canvasSize, minPitch, maxPitch) { reCalcKeyRects() }

    Canvas(
        modifier = modifier
            .background(darkPart)
            .focusRequester(focusRequester)
            .focusable()
            .onSizeChanged { size ->
                canvasSize = size
            }
            .pointerHoverIcon(currentCursor)
            .pointerInput(whiteKeyRects, blackKeyRects) {
                awaitPointerEventScope {
                    var inHeightRegionPressed = false
                    var lastCursorPressed = false
                    while (true) {
                        val event = awaitPointerEvent()

                        // =========== Height adjust region ==========
                        var inAdjust = false
                        val firstChange = event.changes.first()
                        val activeRect = Rect(Offset.Zero, Size(size.width.toFloat(), 8.dp.toPx()))
                        currentCursor = if (firstChange.position in activeRect) {
                            AppCursors.verticalResize
                        } else PointerIcon.Default
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
                        for (change in event.changes) {
                            if (change.pressed) {
                                var findInBlackRegion = false
                                for ((pitch, rect) in blackKeyRects) {
                                    if (change.position in rect) {
                                        currentPressedKeys[pitch] = (change.position.y - offsetStartY) / rect.height
                                        findInBlackRegion = true
                                        break
                                    }
                                }
                                if (!findInBlackRegion) {
                                    for ((pitch, rect) in whiteKeyRects) {
                                        if (change.position in rect) {
                                            currentPressedKeys[pitch] = (change.position.y - offsetStartY) / rect.height
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
                    pointerPressedKey.forEach { key -> onRelease(key) }
                    pointerPressedKey.clear()
                }
            }
    ) {
        whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacing.toPx()) / whiteKeyCount

        draggableAreaColor?.let {
            drawRect(
                color = if (it.isSpecified) it else colorScheme.surfaceContainer,
                size = Size(size.width, 8.dp.toPx())
            )
        }
        if (enableSplitRedLine) drawRect(
            color = Color(160, 32, 32),
            topLeft = Offset(0f, (if (draggableAreaColor == null) 0 else 8).dp.toPx()),
            size = Size(size.width, 4.dp.toPx())
        )

        // 白键
        for (pitch in minPitch..maxPitch) {
            if (!isBlackKey(pitch)) {
                val rect = whiteKeyRects[pitch] ?: continue
                drawRoundedBottomShape(
                    topLeft = rect.topLeft,
                    size = if (activeKey.containsKey(pitch)) Size(rect.size.width, rect.size.height + endPadding * 0.6f) else rect.size,
                    rx = whiteKeyWidth * 0.3f,
                    ry = whiteKeyWidth * 0.15f,
                    color = activeKey[pitch] ?: whiteKeyColor
                )
            }
        }
        // 黑键
        for (pitch in minPitch..maxPitch) {
            if (isBlackKey(pitch)) {
                val rect = blackKeyRects[pitch] ?: continue
                val radiusUnit = whiteKeyWidth * 0.03f
                drawRoundedBottomShape(
                    color = darkPart,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    rx = radiusUnit,
                    ry = radiusUnit
                )

                drawRoundedBottomShape(
                    color = activeKey[pitch]?.darken(1.5f) ?: blackKeyColor,
                    topLeft = Offset(rect.topLeft.x + rect.size.width * 0.07f, rect.topLeft.y),
                    size = Size(rect.size.width * 0.86f, rect.size.height - rect.size.width * 0.1f),
                    rx = radiusUnit * 4,
                    ry = radiusUnit * 2
                )
            }
        }

        textRenderingItems.entries.forEach { (pitch, obj) ->
            val pos = if (isBlackKey(pitch)) {
                val rect = blackKeyRects[pitch] ?: return@forEach
                if (centerAppendLayer) rect.center
                else rect.bottomCenter.let { it.copy(y = it.y - whiteKeyWidth * 0.5f) }
            } else {
                val rect = whiteKeyRects[pitch] ?: return@forEach
                if (centerAppendLayer) Offset(rect.center.x, rect.topLeft.y + rect.size.height * (blackVerticalPercentage * 0.5f + 0.5f))
                else rect.bottomCenter.let { it.copy(y = it.y - whiteKeyWidth * 0.5f) }
            }
            drawTextCentered(obj, pos)
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