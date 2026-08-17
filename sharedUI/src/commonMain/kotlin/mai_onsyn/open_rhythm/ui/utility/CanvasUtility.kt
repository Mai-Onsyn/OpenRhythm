package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.findBarLines
import kotlin.collections.mapValues

fun DrawScope.drawTextCentered(
    layoutResult: TextLayoutResult,
    center: Offset,
    alpha: Float = 1.0f,
) {
    val width = layoutResult.size.width
    val height = layoutResult.size.height
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(center.x - width / 2, center.y - height / 2),
        alpha = alpha
    )
}

@Composable
fun rememberTextLayoutResult(
    text: String,
    fontSize: TextUnit,
    color: Color
): TextLayoutResult {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(text, fontSize, color, density) {
        textMeasurer.measure(
            text = text,
            style = TextStyle(fontSize = fontSize / density.density, color = color)
        )
    }
}

@Composable
fun rememberTextLayoutResult(
    texts: List<String>,
    fontSize: TextUnit,
    color: Color
): List<TextLayoutResult> {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(texts, fontSize, color, density) {
        texts.map {
            textMeasurer.measure(
                text = it,
                style = TextStyle(fontSize = fontSize / density.density, color = color)
            )
        }
    }
}

@Composable
fun <T> rememberTextLayoutResult(
    texts: Map<T, String>,
    fontSize: TextUnit,
    color: (Map.Entry<T, String>) -> Color
): Map<T, TextLayoutResult> {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(texts, fontSize, color, density) {
        texts.mapValues {
            textMeasurer.measure(
                text = it.value,
                style = TextStyle(fontSize = fontSize / density.density, color = color(it))
            )
        }
    }
}

fun DrawScope.drawOctaveLines(
    minPitch: Int,          // A0 default
    maxPitch: Int,          // C8 default
    gridPos: Map<Int, Pair<Float, Float>>,
    color: Color = Global.settings.OctaveLineColor,
    thickness: Dp = Global.settings.OctaveLineThickness.dp
) {
    val thicknessPx = thickness.toPx()
    for (pitch in minPitch..maxPitch) {
        if (pitch % 12 == 0) {
            val x = gridPos[pitch]!!.first - thicknessPx / 2
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = thicknessPx
            )
        }
        else if (pitch % 12 == 5) {
            val x = gridPos[pitch]!!.first - thicknessPx / 2
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = thicknessPx * 0.4f
            )
        }
    }
}

fun DrawScope.drawSectionLines(
    midi: Midi,
    loTick: Long,
    hiTick: Long,
    pxPerTick: Float,
    color: Color = Global.settings.SectionLineColor,
    thickness: Dp = Global.settings.SectionLineThickness.dp
) {
    val thicknessPx = thickness.toPx()
    val sectionTicks = findBarLines(midi.timeSignatureEvents, loTick, hiTick, midi.ppq)

    for (tick in sectionTicks) {
        val y = size.height - ((tick - loTick) * pxPerTick - thicknessPx * 0.5f)
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = thicknessPx * 0.4f
        )
    }
}

fun reMeasure(
    gridPos: MutableMap<Int, Pair<Float, Float>>,
    size: IntSize,
    spacingPx: Float,
    minPitch: Int,
    maxPitch: Int,
    blackHorizontalPercentage: Float
) {
    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)

    gridPos.clear()
    val whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount

    for (pitch in minPitch..maxPitch) {
        if (isBlackKey(pitch)) {
            val offsetPercent = blackKeyOffset(pitch)
            val centerX = (countWhiteKeys(minPitch, pitch)) * (whiteKeyWidth + spacingPx) - spacingPx / 2
            val keyWidth = whiteKeyWidth * blackHorizontalPercentage
            val left = centerX - keyWidth / 2 + keyWidth * offsetPercent
            gridPos[pitch] = Pair(left, keyWidth)
        } else {
            gridPos[pitch] = Pair(
                (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacingPx),
                whiteKeyWidth
            )
        }
    }
}