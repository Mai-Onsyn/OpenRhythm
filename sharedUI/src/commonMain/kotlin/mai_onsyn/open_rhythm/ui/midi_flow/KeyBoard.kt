package mai_onsyn.open_rhythm.ui.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val W_pref = arrayOf(1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6, 7)
private val B_pref = arrayOf(false, true, false, true, false, false, true, false, true, false, true, false)
private val B_offset = arrayOf(0f, -1/6f, 1f, 1/6f, 0f, 0f, -1/4f, 0f, 0f, 0f, 1/4f, 0f)

private fun countWhiteKeys(min: Int, max: Int): Int {
    val hi = max / 12 * 7 + W_pref[max % 12]
    val lo = min / 12 * 7 + W_pref[min % 12]
    return hi - lo + 1
}

private fun isBlackKey(pitch: Int): Boolean = B_pref[pitch % 12]

private fun blackKeyOffset(pitch: Int): Float = B_offset[pitch % 12]

@Composable
fun MidiKeyBoard(
    modifier: Modifier = Modifier,
    minPitch: Int = 21,     // A0 default
    maxPitch: Int = 108,    // C8 default
    activeKey: Map<Int, Color> = emptyMap(),
    blackVerticalPercentage: Float = 0.64f,     // 黑键底部y在整个键盘高度的比例
    blackHorizontalPercentage: Float = 0.75f,   // 单个黑键宽度相对于白键宽度的比例
    spacing: Dp = 1.dp,
    darkPart: Color = Color.Black,
    onPress: (Int) -> Unit = {},
    onRelease: (Int, Int) -> Unit = { pitch, velocity -> }
) {
    require(minPitch >= 0 && maxPitch <= 127) { "Pitch out of range [0, 127]" }
    require(minPitch <= maxPitch) { "minPitch must be <= maxPitch [$minPitch, $maxPitch]" }
    val colorScheme = MaterialTheme.colorScheme

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)

    Canvas(
        modifier = modifier
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
        val endPadding = 4.dp.toPx()

        for (pitch in minPitch..maxPitch) {
            if (!isBlackKey(pitch))  drawWhiteKey(minPitch, pitch, whiteKeyWidth, spacing, offsetStartY, endPadding, maxPitch, darkPart)
        }
        for (pitch in minPitch..maxPitch) {
            if (isBlackKey(pitch)) {
                val offsetPercent = blackKeyOffset(pitch)

                val centerX = (countWhiteKeys(minPitch, pitch)) * (whiteKeyWidth + spacing.toPx()) - spacing.toPx() / 2
                val blackBaseSize = Size(whiteKeyWidth * blackHorizontalPercentage, (size.height - offsetStartY - endPadding) * blackVerticalPercentage)
                val blackBaseOffset = Offset(centerX - blackBaseSize.width / 2 + blackBaseSize.width * offsetPercent, offsetStartY)

                val radiusUnit = whiteKeyWidth * 0.03f
                drawRoundedBottomShape(
                    color = darkPart,
                    topLeft = blackBaseOffset,
                    size = blackBaseSize,
                    rx = radiusUnit,
                    ry = radiusUnit
                )

                drawRoundedBottomShape(
                    color = Color.Magenta,
                    topLeft = Offset(blackBaseOffset.x + blackBaseSize.width * 0.07f, blackBaseOffset.y),
                    size = Size(blackBaseSize.width * 0.86f, blackBaseSize.height - blackBaseSize.width * 0.1f),
                    rx = radiusUnit * 4,
                    ry = radiusUnit * 2
                )
            }
        }
    }
}

private fun DrawScope.drawWhiteKey(
    minPitch: Int,
    pitch: Int,
    whiteKeyWidth: Float,
    spacing: Dp,
    offsetStartY: Float,
    endPadding: Float,
    maxPitch: Int,
    darkPart: Color
) {
    val x = (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacing.toPx())
    drawRoundedBottomShape(
        topLeft = Offset(x, offsetStartY),
        size = Size(whiteKeyWidth, size.height - offsetStartY - endPadding),
        rx = whiteKeyWidth * 0.3f,
        ry = whiteKeyWidth * 0.15f
    )
    if (pitch != maxPitch) {
        drawRect(   // 分割线
            color = darkPart,
            topLeft = Offset(x + whiteKeyWidth, offsetStartY),
            size = Size(spacing.toPx(), size.height - offsetStartY)
        )
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