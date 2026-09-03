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
}

fun Midi.nanoAtTick(t: Double): Double = this.tempoMap.tickToNanos(t)

fun Midi.tickAtNanoOffset(nanoOffset: Long): Long {
    return tickAtNanoOffset(nanoOffset.toDouble()).toLong()
}

fun Midi.tickAtNanoOffset(nanoOffset: Double): Double = this.tempoMap.nanosToTick(nanoOffset)