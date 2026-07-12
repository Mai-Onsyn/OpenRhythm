package mai_onsyn.open_rhythm.core.midi

private val pitchNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

data class Note(
    val pitch: Int,
    val tick: Long,
    val duration: Long,
    val velocity: Int,
    val channel: Int
) {
    companion object {
        fun toString(pitch: Int): String {
            val pitchClass = pitch.mod(12)
            val octave = pitch / 12 - 1
            return "${pitchNames[pitchClass]}$octave"
        }
    }

    override fun toString(): String = "Note(tick=$tick, pitch=${toString(pitch)}, duration=$duration, velocity=$velocity, channel=$channel)"
}

class MidiTrack(
    var name: String = "Unnumbered Track",
    val notes: MutableList<Note> = mutableListOf(),
    val controllerEvents: MutableList<MidiEvent> = mutableListOf()
)

open class MidiEvent(
    val tick: Long,
    val event: ByteArray
) {
    val channel: Int get() = (event[0].toInt() and 0x0F)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MidiEvent

        if (tick != other.tick) return false
        if (!event.contentEquals(other.event)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tick.hashCode()
        result = 31 * result + event.contentHashCode()
        return result
    }

    override fun toString(): String = "MidiEvent(tick=$tick, event=${event.contentToString()}, channel=$channel)"
}

class MidiCCEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val controller: Int get() = event[1].toInt()
    val value: Int get() = event[2].toInt()

    override fun toString(): String = "MidiCCEvent(tick=$tick, controller=$controller, value=$value, channel=$channel)"
}

class MidiPBEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val value: Int get() = event[1].toInt() + (event[2].toInt() shl 7)

    override fun toString(): String = "MidiPBEvent(tick=$tick, value=$value, channel=$channel)"
}

class MidiPCEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val program: Int get() = event[1].toInt()

    override fun toString(): String = "MidiPCEvent(tick=$tick, program=$program, channel=$channel)"
}

data class TempoEvent(val tick: Long, val bpm: Double)
data class TimeSignatureEvent(val absoluteTick: Long, val numerator: Int, val denominator: Int)

fun List<TempoEvent>.bpmAtTick(t: Long, default: Double = 120.0): Double {
    val index = lastOrNull { it.tick <= t } ?: return default
    return index.bpm
}

fun Midi.msAtTick(t: Long): Double = this.nanoAtTick(t) / 1_000_000.0

fun Midi.nanoAtTick(t: Long): Long {
    var passedMs = 0.0

    if (tempoEvents.size > 1) {
        for (i in 0 until tempoEvents.size - 1) {
            val preTempo = tempoEvents[i]
            val nextTempo = tempoEvents[i + 1]

            if (t in preTempo.tick until nextTempo.tick) {
                passedMs += (t - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
                return passedMs.toLong()
            } else if (t >= nextTempo.tick) {
                passedMs += (nextTempo.tick - preTempo.tick) * 60_000_000_000.0 / (preTempo.bpm * ppq)
            }
        }
    } else if (tempoEvents.isEmpty()) {
        return (t * 60_000_000_000.0 / (120 * ppq)).toLong()
    }
    val lastTempo = tempoEvents.last()
    passedMs += (t - lastTempo.tick) * 60_000_000_000.0 / (lastTempo.bpm * ppq)

    return passedMs.toLong()
}

fun Midi.tickAtNanoOffset(nanoOffset: Long): Long {
    val ppq = this.ppq
    // 无 tempo 事件，使用默认 BPM = 120
    if (tempoEvents.isEmpty()) {
        return (nanoOffset * 120.0 * ppq / 60_000_000_000.0).toLong()
    }

    var remainingNano = nanoOffset.toDouble()  // 剩余未分配的纳秒数

    // 遍历所有完整区间（最后一个之前）
    for (i in 0 until tempoEvents.size - 1) {
        val pre = tempoEvents[i]
        val next = tempoEvents[i + 1]
        // 当前段的纳秒时长
        val segmentNano = (next.tick - pre.tick) * 60_000_000_000.0 / (pre.bpm * ppq)

        if (remainingNano < segmentNano) {
            // 目标落在当前段内
            val tickOffset = remainingNano * pre.bpm * ppq / 60_000_000_000.0
            return (pre.tick + tickOffset).toLong()
        } else {
            // 跳过整个段
            remainingNano -= segmentNano
        }
    }

    // 处理最后一个 tempo 事件之后的区间（无限延伸）
    val last = tempoEvents.last()
    val tickOffset = remainingNano * last.bpm * ppq / 60_000_000_000.0
    return (last.tick + tickOffset).toLong()
}