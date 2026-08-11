package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.ktx.darken
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.midi.TimeSignatureEvent
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.getContrastTextColor
import mai_onsyn.open_rhythm.ui.utility.countWhiteKeys
import mai_onsyn.open_rhythm.ui.utility.drawOctaveLines
import mai_onsyn.open_rhythm.ui.utility.drawSectionLines
import mai_onsyn.open_rhythm.ui.utility.drawTextCentered
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import mai_onsyn.open_rhythm.ui.utility.reMeasure
import mai_onsyn.open_rhythm.ui.utility.rememberTextLayoutResult
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun MidiWaterFall(
    modifier: Modifier = Modifier,
    currTick: Long = 0,
    midi: Midi,
    minPitch: Int = 21,     // A0 default
    maxPitch: Int = 108,    // C8 default
    trackColors: List<Color> = emptyList(),
    hpb: Dp = 120.dp, // height(dp) per beat
    spacing: Dp = 1.dp,
    blackHorizontalPercentage: Float = 0.75f,
    activeNoteOutput: MutableMap<Int, Color> = mutableMapOf(),
    drawSectionLine: Boolean = true,
    drawOctaveLine: Boolean = true,
    noteRoundPercent: Float = 0.2f,
    drawPitchLabel: Boolean = false,
    onVerticalDragged: (Float) -> Unit = {}
) {
//    require(trackColors.size >= midi.hasNoteTracks) { "Not enough colors for tracks" }
    val density = LocalDensity.current

    val spacingPx = with(density) { spacing.toPx() }

    val gridPos = remember { mutableMapOf<Int, Pair<Float, Float>>() }  // Map<key, Pair<x, width>>
    val maxNoteDurationList = remember(midi) {
        val result = mutableListOf<Long>()
        for (midiTrack in midi.tracks) {
            if (midiTrack.visible && midiTrack.enable)
                result.add(midiTrack.notes.maxOfOrNull { it.duration } ?: 0)
            else result.add(0L)
        }
        result
    }

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)
    var whiteKeyWidth by remember { mutableStateOf(0f) }

    val pitchLabels = if (drawPitchLabel) {
        val pitchNames = remember(minPitch, maxPitch) {
            mutableStateMapOf<Int, String>().apply {
                for (i in minPitch..maxPitch)
                    put(i, Note.toString(i))
            }
        }
        rememberTextLayoutResult(
            texts = pitchNames,
            fontSize = (whiteKeyWidth * 0.3f).sp,
            color = { Color.White }
        )
    } else null

    // =====调试状态统计=====
    var lastDrawTime by remember { mutableStateOf(0L) }
    var frameTime by remember { mutableStateOf(0L) }
    var fps by remember { mutableStateOf(0) }
    var countedFrames by remember { mutableStateOf(0) }
    var lastClearFrameCounterTime by remember { mutableStateOf(0L) }
    if (Global.settings.ShowFps) LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            val now = Time.micros
            fps = (1_000_000f * countedFrames / (now - lastClearFrameCounterTime)).roundToInt()
            countedFrames = 0
            lastClearFrameCounterTime = now
        }
    }
    var renderingNoteCount by remember { mutableStateOf(0) }
    var activeNoteCount by remember { mutableStateOf(0) }

    Box {
        Canvas(
            modifier = modifier
                .clip(RectangleShape)
                .onSizeChanged { size ->
                    whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount
                    reMeasure(gridPos, size, spacingPx, minPitch, maxPitch, blackHorizontalPercentage)
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()
                            if (change.pressed && change.positionChanged()) {
                                val deltaPx = change.position.y - change.previousPosition.y
                                onVerticalDragged(deltaPx)
                            }
                        }
                    }
                }
        ) {
            if (drawOctaveLine) drawOctaveLines(minPitch, maxPitch, gridPos)
            val height = size.height
            val visibleTickCount = (height / hpb.toPx() * midi.ppq).toInt()
            val pxPerTick = hpb.toPx() / midi.ppq
            if (drawSectionLine) drawSectionLines(midi, currTick, currTick + visibleTickCount, pxPerTick)

            val toDrawNotes = mutableListOf<DrawableNote>()
            for (i in 0 until midi.hasNoteTracks) {
                if (!midi.tracks[i].visible || !midi.tracks[i].enable) continue
                findVisibleNotes(
                    currTick,
                    currTick + visibleTickCount,
                    maxNoteDurationList[i],
                    midi.tracks[i].notes
                ).forEach {
                    toDrawNotes.add(DrawableNote(it, trackColors[i % trackColors.size], i))
                }
            }
            toDrawNotes.sortWith(compareBy({ it.note.tick }, { it.trackNum }))
            renderingNoteCount = toDrawNotes.size

            activeNoteOutput.clear()
            fun drawNote(pack: DrawableNote, blackKey: Boolean) {
                val (x, w) = gridPos[pack.note.pitch] ?: return
                val pixelPos = (pack.note.tick - currTick) * pxPerTick
                val durationPx = pack.note.duration * pxPerTick
                val noteRect = Rect(Offset(x, (height - pixelPos - durationPx)), Size(w, durationPx))
                drawNoteGraphics(
                    if (blackKey) pack.color.darken(1.5f) else pack.color,
                    noteRect,
                    w * 0.5f * noteRoundPercent
                )
                pitchLabels?.get(pack.note.pitch)?.let {
                    val labelCenterPos = noteRect.bottomCenter.let { offset -> offset.copy(y = offset.y - whiteKeyWidth * 0.4f) }
                    drawTextCentered(
                        it,
                        labelCenterPos,
                    )
                }
                if (pack.note.tick <= currTick && pack.note.tick + pack.note.duration >= currTick) {
                    activeNoteOutput[pack.note.pitch] = pack.color
                }
            }

            for (pack in toDrawNotes) {
                if (!isBlackKey(pack.note.pitch)) drawNote(pack, false)
            }
            for (pack in toDrawNotes) {
                if (isBlackKey(pack.note.pitch)) drawNote(pack, true)
            }
            activeNoteCount = activeNoteOutput.size
            val now = Time.millis
            frameTime = now - lastDrawTime
            lastDrawTime = now
            countedFrames++
        }

        Box(Modifier.matchParentSize()) {
            val color by Global.settings.WaterfallBackgroundColor.let { remember(it) {
                mutableStateOf(getContrastTextColor(it))
            } }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                @Composable
                fun ShowText(str: String) {
                    Text(
                        text = str,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
                if (Global.settings.ShowCurrentTick)
                    ShowText("Tick=${currTick}")
                if (Global.settings.ShowFps)
                    ShowText("FPS=${fps}")
                if (Global.settings.ShowFrameTime)
                    ShowText("Frame Time=${frameTime}")
                if (Global.settings.ShowRenderingNoteCount)
                    ShowText("Note Count=${renderingNoteCount}")
                if (Global.settings.ShowActiveNoteCount)
                    ShowText("Active Note Count=${activeNoteCount}")
            }
        }
    }
}

data class DrawableNote(
    val note: Note,
    val color: Color,
    val trackNum: Int
)

private fun findVisibleNotes(
    loTick: Long,
    hiTick: Long,
    maxDuration: Long,
    notes: List<Note>
): List<Note> {
    if (notes.isEmpty()) return emptyList()

    // 二分查找左边界：寻找第一个 startTick >= (loTick - maxDuration) 的位置
    val targetLeft = loTick - maxDuration
    val startIndex = binarySearchFirstGe(notes, targetLeft)

    // 二分查找右边界：寻找第一个 startTick > hiTick 的位置
    val endIndex = binarySearchFirstGt(notes, hiTick)

    // 在极小的 [startIndex, endIndex) 范围内做精确过滤
    val result = ArrayList<Note>(endIndex - startIndex)
    for (i in startIndex until endIndex) {
        val note = notes[i]
        // 判断音符的尾部是否已经进入了 loTick 之后
        if (note.tick + note.duration >= loTick) {
            result.add(note)
        }
    }

    return result
}

// 二分查找第一个 startTick >= target 的索引
private fun binarySearchFirstGe(notes: List<Note>, target: Long): Int {
    var low = 0
    var high = notes.size - 1
    var result = notes.size
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (notes[mid].tick >= target) {
            result = mid
            high = mid - 1 // 尝试找更左边的
        } else {
            low = mid + 1
        }
    }
    return result
}

// 辅助函数：二分查找第一个 startTick > target 的索引
private fun binarySearchFirstGt(notes: List<Note>, target: Long): Int {
    var low = 0
    var high = notes.size - 1
    var result = notes.size
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (notes[mid].tick > target) {
            result = mid
            high = mid - 1 // 尝试找更左边的
        } else {
            low = mid + 1
        }
    }
    return result
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