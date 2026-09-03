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
    val controllerEvents: MutableList<MidiEvent> = mutableListOf(),
    val tickRange: IntRange = IntRange.EMPTY,
    val trackInst: Int = 0,
    var enable: Boolean = true,
    var visible: Boolean = true
) {
    val trackChannel: Int get() = notes.firstOrNull()?.channel ?: 0
}

open class MidiEvent(
    val tick: Long,
    val event: ByteArray
) {
    val channel: Int get() = (event[0].toInt() and 0x0F)

    open val order = -100

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

    companion object {
        fun parse(tick: Long, event: ByteArray): MidiEvent {
            return when (event[0].toInt() and 0xF0) {
                0x80, 0x90 -> NoteEvent(tick, event)
                0xB0       -> MidiCCEvent(tick, event)
                0xC0       -> MidiPCEvent(tick, event)
                0xE0       -> MidiPBEvent(tick, event)
                else       -> MidiEvent(tick, event)
            }
        }
    }
}

class MidiCCEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val controller: Int get() = event[1].toInt()
    val value: Int get() = event[2].toInt()

    override val order = -10

    companion object {
        fun of(tick: Long, channel: Int, controller: Int, value: Int): MidiCCEvent {
            return MidiCCEvent(tick, byteArrayOf((0xB0 + channel).toByte(), controller.toByte(), value.toByte()))
        }
    }

    override fun toString(): String = "MidiCCEvent(tick=$tick, controller=$controller, value=$value, channel=$channel)"
}

class MidiPBEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val value: Int get() = event[1].toInt() + (event[2].toInt() shl 7)

    override val order = -5

    companion object {
        fun of(tick: Long, channel: Int, value: Int): MidiPBEvent {
            return MidiPBEvent(tick, byteArrayOf((0xE0 + channel).toByte(), (value and 0x7F).toByte(), (value shr 7 and 0x7F).toByte()))
        }
    }

    override fun toString(): String = "MidiPBEvent(tick=$tick, value=$value, channel=$channel)"
}

class MidiPCEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val program: Int get() = event[1].toInt()

    override val order = -50

    companion object {
        fun of(tick: Long, channel: Int, value: Int): MidiPCEvent {
            return MidiPCEvent(tick, byteArrayOf((0xC0 + channel).toByte(), value.toByte()))
        }
    }

    override fun toString(): String = "MidiPCEvent(tick=$tick, program=$program, channel=$channel)"
}

class NoteEvent(
    absoluteTick: Long,
    event: ByteArray
): MidiEvent(absoluteTick, event) {
    val pitch: Int get() = event[1].toInt() and 0xFF
    val velocity: Int get() = event[2].toInt() and 0xFF
    val on: Boolean get() = (event[0].toInt() and 0xF0) == 0x90

    override val order = if (on) 101 else 100

    companion object {
        fun noteOn(tick: Long, pitch: Int, velocity: Int, channel: Int): NoteEvent {
            require(pitch in 0..127 && velocity in 0..127 && channel in 0..15) {
                "parameters out of midi range"
            }
            val status = 0x90 or (channel and 0x0F)
            val event = byteArrayOf(status.toByte(), pitch.toByte(), velocity.toByte())
            return NoteEvent(tick, event)
        }
        fun noteOff(tick: Long, pitch: Int, velocity: Int, channel: Int): NoteEvent {
            require(pitch in 0..127 && velocity in 0..127 && channel in 0..15) {
                "parameters out of midi range"
            }
            val status = 0x80 or (channel and 0x0F)
            val event = byteArrayOf(status.toByte(), pitch.toByte(), velocity.toByte())
            return NoteEvent(tick, event)
        }
    }
}

data class TempoEvent(val tick: Long, val bpm: Double)
data class TimeSignatureEvent(val tick: Long, val numerator: Int, val denominator: Int)