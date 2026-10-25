package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls

import androidx.compose.ui.graphics.Color
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
            toDrawNotes.add(DrawableNote(it, trackColors[i % trackColors.size], i))
        }
    }
//    toDrawNotes.sortWith(compareBy({ isBlackKey(it.note.pitch) }, { -it.note.pitch }, { it.note.tick }, { it.trackNum }))
    sortNotes(toDrawNotes)
    return toDrawNotes
}

fun sortNotes(notes: MutableList<DrawableNote>) {
    val n = notes.size
    if (n < 2) return

    // ---- 1. 扫描字段范围 ----
    var maxPitch = 0
    var maxTrack = 0
    var maxTick = Long.MIN_VALUE
    var minTick = Long.MAX_VALUE
    for (item in notes) {
        val p = item.note.pitch
        if (p > maxPitch) maxPitch = p
        val tn = item.trackNum
        if (tn > maxTrack) maxTrack = tn
        val t = item.note.tick
        if (t > maxTick) maxTick = t
        if (t < minTick) minTick = t
    }

    // ---- 2. 动态计算每个字段需要的位宽 ----
    var indexBits = 1
    while ((1L shl indexBits) < n) indexBits++      // 支持百万级：n=1e6 → 20 位

    val pitchBits = bitsNeeded(maxPitch.toLong())
    val trackBits = bitsNeeded(maxTrack.toLong())
    val tickBits  = bitsNeeded(maxTick - minTick)

    val totalBits = 1 + pitchBits + tickBits + trackBits + indexBits
    if (totalBits > 63) {
        // 超出 63 位（例如 tick 是纳秒级大范围），回退保证正确
        notes.sortWith(
            compareBy(
                { isBlackKey(it.note.pitch) },
                { -it.note.pitch },
                { it.note.tick },
                { it.trackNum }
            )
        )
        return
    }

    // ---- 3. 位布局：低位到高位 index | trackNum | tick | pitch | bk ----
    val indexShift = 0
    val trackShift = indexShift + indexBits
    val tickShift  = trackShift + trackBits
    val pitchShift = tickShift + tickBits
    val bkShift    = pitchShift + pitchBits

    val indexMask = (1L shl indexBits) - 1
    val trackMask = (1L shl trackBits) - 1
    val tickMask  = (1L shl tickBits)  - 1
    val pitchMask = (1L shl pitchBits) - 1

    // ---- 4. 打包 ----
    val packed = LongArray(n)
    for (i in 0 until n) {
        val item = notes[i]
        val pitch = item.note.pitch
        val bk = if (isBlackKey(pitch)) 1L else 0L
        val pitchEnc = (maxPitch - pitch).toLong() and pitchMask   // 实现 -pitch 升序
        val tickEnc  = (item.note.tick.toLong() - minTick) and tickMask
        val trackEnc = item.trackNum.toLong() and trackMask

        packed[i] = (bk shl bkShift) or
                (pitchEnc shl pitchShift) or
                (tickEnc  shl tickShift) or
                (trackEnc shl trackShift) or
                (i.toLong() shl indexShift)                    // 保稳定排序
    }

    // ---- 5. 排序（JDK 双轴快排，无比较器调用）----
//    java.util.Arrays.sort(packed)
    packed.sort()

    // ---- 6. 原地重建，避免 clear + addAll ----
    val old = notes.toTypedArray()
    for (i in 0 until n) {
        notes[i] = old[(packed[i] and indexMask).toInt()]
    }
}

private fun bitsNeeded(v: Long): Int {
    if (v <= 0L) return 1
    return 64 - v.countLeadingZeroBits()
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