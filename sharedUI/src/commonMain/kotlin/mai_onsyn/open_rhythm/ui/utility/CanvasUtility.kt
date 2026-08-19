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
import mai_onsyn.open_rhythm.core.midi.TimeSignatureEvent

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
    loTick: Double,
    hiTick: Double,
    pxPerTick: Float,
    color: Color = Global.settings.SectionLineColor,
    thickness: Dp = Global.settings.SectionLineThickness.dp
) {
    val thicknessPx = thickness.toPx()
    val sectionTicks = findBarLines(midi.timeSignatureEvents, loTick.toLong(), hiTick.toLong(), midi.ppq)

    for (tick in sectionTicks) {
        val y = size.height - ((tick - loTick) * pxPerTick - thicknessPx * 0.5f)
        drawLine(
            color = color,
            start = Offset(0f, y.toFloat()),
            end = Offset(size.width, y.toFloat()),
            strokeWidth = thicknessPx * 0.4f
        )
    }
}

fun findBarLines(
    events: List<TimeSignatureEvent>,
    startTick: Long,
    endTick: Long,
    ppq: Int
): List<Long> {
    if (startTick > endTick) return emptyList()

    // ---------- 1. 预分配容量（消除扩容 GC） ----------
    // 最小 barTicks = 1 * ppq * 4 / 256 = ppq / 64
    val minBarTicks = (ppq / 64L).coerceAtLeast(1)
    val estimatedSize = ((endTick - startTick) / minBarTicks + 1).toInt()
    // 防止极端异常值撑爆内存，加个合理上限（比如 200 万）
    val capacity = estimatedSize.coerceAtMost(2_000_000)
    val result = ArrayList<Long>(capacity)

    // ---------- 2. 找到 startTick 时刻的有效拍号 ----------
    // 二分查找最后一个 tick <= startTick 的事件
    var searchIdx = events.binarySearchBy(startTick) { it.tick }
    val activeIdx = if (searchIdx >= 0) searchIdx else -searchIdx - 2

    var num: Int
    var den: Int
    var anchor: Long

    if (activeIdx < 0) {
        // 默认 4/4，起始锚点在第 0 tick
        num = 4
        den = 4
        anchor = 0L
    } else {
        val ev = events[activeIdx]
        num = ev.numerator
        den = ev.denominator
        anchor = ev.tick
    }

    // ---------- 3. 分段处理 [anchor, nextEventTick) ----------
    var eventPtr = activeIdx + 1
    // 当前片段的结束边界（不包含），若没有下一个事件则设为 Long.MAX_VALUE
    var limit = if (eventPtr < events.size) events[eventPtr].tick else Long.MAX_VALUE

    while (true) {
        val barTicks = num * ppq * 4L / den

        // 计算本片段内第一个 >= startTick 的小节线
        val segmentStart = if (anchor > startTick) anchor else startTick
        val diff = segmentStart - anchor
        // 整数除法向下取整，定位到锚点之后的第几个小节
        var bar = anchor + (diff / barTicks) * barTicks
        if (bar < segmentStart) bar += barTicks

        // 循环添加小节线（直到达到片段边界或 endTick）
        while (bar < limit && bar <= endTick) {
            result.add(bar)
            bar += barTicks
        }

        // 如果已经超出 endTick 或没有更多事件，结束
        if (bar > endTick || eventPtr >= events.size) break

        // ---------- 切换下一个拍号 ----------
        val nextEv = events[eventPtr]
        num = nextEv.numerator
        den = nextEv.denominator
        anchor = nextEv.tick          // 事件 tick 即新锚点
        eventPtr++
        limit = if (eventPtr < events.size) events[eventPtr].tick else Long.MAX_VALUE

        // 如果锚点已经超出 endTick，后续不可能有结果，提前退出
        if (anchor > endTick) break
    }

    return result
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