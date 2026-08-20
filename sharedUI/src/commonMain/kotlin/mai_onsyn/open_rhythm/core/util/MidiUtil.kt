package mai_onsyn.open_rhythm.core.util

import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.TempoEvent

fun List<TempoEvent>.bpmAtTick(t: Long, default: Double = 120.0): Double {
    val index = lastOrNull { it.tick <= t } ?: firstOrNull() ?: return default
    return index.bpm
}

fun Midi.msAtTick(t: Long): Double = this.nanoAtTick(t) / 1_000_000.0

fun Midi.nanoAtTick(t: Long): Long {
    return nanoAtTick(t.toDouble()).toLong()
//    var passedMs = 0.0
//
//    if (tempoEvents.size > 1) {
//        for (i in 0 until tempoEvents.size - 1) {
//            val preTempo = tempoEvents[i]
//            val nextTempo = tempoEvents[i + 1]
//
//            if (t in preTempo.tick until nextTempo.tick) {
//                passedMs += (t - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
//                return passedMs.toLong()
//            } else if (t >= nextTempo.tick) {
//                passedMs += (nextTempo.tick - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
//            }
//        }
//    } else if (tempoEvents.isEmpty()) {
//        return (t * 60_000_000_000.0 / (120 * ppq)).toLong()
//    }
//    val lastTempo = tempoEvents.last()
//    passedMs += (t - lastTempo.tick) * 60_000_000_000.0 / (lastTempo.bpm * ppq)
//
//    return passedMs.toLong()
}

//fun Midi.nanoAtTick(t: Double): Double {
//    var passedMs = 0.0
//
//    if (tempoEvents.size > 1) {
//        for (i in 0 until tempoEvents.size - 1) {
//            val preTempo = tempoEvents[i]
//            val nextTempo = tempoEvents[i + 1]
//
//            if (preTempo.tick <= t && t < nextTempo.tick) {
//                passedMs += (t - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
//                return passedMs
//            } else if (t >= nextTempo.tick) {
//                passedMs += (nextTempo.tick - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
//            }
//        }
//    } else if (tempoEvents.isEmpty()) {
//        return t * 60_000_000_000.0 / (120 * ppq)
//    }
//    val lastTempo = tempoEvents.last()
//    passedMs += (t - lastTempo.tick) * 60_000_000_000.0 / (lastTempo.bpm * ppq)
//
//    return passedMs
//}

fun Midi.nanoAtTick(t: Double): Double = this.tempoMap.tickToNanos(t)

fun Midi.tickAtNanoOffset(nanoOffset: Long): Long {
    return tickAtNanoOffset(nanoOffset.toDouble()).toLong()
//    val ppq = this.ppq
//    // 无 tempo 事件，使用默认 BPM = 120
//    if (tempoEvents.isEmpty()) {
//        return (nanoOffset * 120.0 * ppq / 60_000_000_000.0).toLong()
//    }
//
//    var remainingNano = nanoOffset.toDouble()  // 剩余未分配的纳秒数
//
//    // 遍历所有完整区间（最后一个之前）
//    for (i in 0 until tempoEvents.size - 1) {
//        val pre = tempoEvents[i]
//        val next = tempoEvents[i + 1]
//        // 当前段的纳秒时长
//        val segmentNano = (next.tick - pre.tick) * 60_000_000_000.0 / (pre.bpm * ppq)
//
//        if (remainingNano < segmentNano) {
//            // 目标落在当前段内
//            val tickOffset = remainingNano * pre.bpm * ppq / 60_000_000_000.0
//            return (pre.tick + tickOffset).toLong()
//        } else {
//            // 跳过整个段
//            remainingNano -= segmentNano
//        }
//    }
//
//    // 处理最后一个 tempo 事件之后的区间（无限延伸）
//    val last = tempoEvents.last()
//    val tickOffset = remainingNano * last.bpm * ppq / 60_000_000_000.0
//    return (last.tick + tickOffset).toLong()
}

//fun Midi.tickAtNanoOffset(nanoOffset: Double): Double {
//    val ppq = this.ppq
//    if (tempoEvents.isEmpty()) {
//        return (nanoOffset * 120.0 * ppq / 60_000_000_000.0)
//    }
//
//    var remainingNano = nanoOffset
//
//    for (i in 0 until tempoEvents.size - 1) {
//        val pre = tempoEvents[i]
//        val next = tempoEvents[i + 1]
//        val segmentNano = (next.tick - pre.tick) * 60_000_000_000.0 / (pre.bpm * ppq)
//
//        if (remainingNano < segmentNano) {
//            val tickOffset = remainingNano * pre.bpm * ppq / 60_000_000_000.0
//            return pre.tick + tickOffset
//        } else {
//            remainingNano -= segmentNano
//        }
//    }
//
//    val last = tempoEvents.last()
//    val tickOffset = remainingNano * last.bpm * ppq / 60_000_000_000.0
//    return (last.tick + tickOffset)
//}

fun Midi.tickAtNanoOffset(nanoOffset: Double): Double = this.tempoMap.nanosToTick(nanoOffset)