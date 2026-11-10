package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls

import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.isBlackKey


/**
 * 确保按音高排列 且所有黑键在白键后面
 */
fun filterWindowNotes(
    midi: Midi,
    currTick: Double,
    visibleTickCount: Int,
    maxNoteDurationList: List<Long>,
    trackColors: List<Color>
): MutableList<DrawableNote> {
    val toDrawNotes = mutableListOf<DrawableNote>()
    for ((i, track) in midi.tracks.withIndex()) {
        if (!track.visible || !track.enable) continue
        findVisibleNotes(
            currTick.toLong(),
            (currTick + visibleTickCount).toLong(),
            maxNoteDurationList[i],
            track.notes
        ).forEach {
            toDrawNotes.add(DrawableNote(it, trackColors[i % trackColors.size], i, it.depth))
        }
    }

    toDrawNotes.sort()
//    toDrawNotes.sortWith(compareBy({ isBlackKey(it.note.pitch) }, { -it.note.pitch }, { it.note.tick }, { it.trackNum }))
    return toDrawNotes
}

data class DrawableNote(
    val note: Note,
    val color: Color,
    val trackNum: Int,
    val depth: UInt
) : Comparable<DrawableNote> {
    override fun compareTo(other: DrawableNote): Int = depth.compareTo(other.depth)
}

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