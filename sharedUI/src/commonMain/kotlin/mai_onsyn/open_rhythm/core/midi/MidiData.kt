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
) {
    val instrumentEvent: MidiPCEvent get() {
        return controllerEvents.first { it is MidiPCEvent } as MidiPCEvent
    }

    private enum class EventType { PC, CC_PB, NOTE }
    fun splitTrackByPC(): List<MidiTrack> {
        if (this.notes.isEmpty() && this.controllerEvents.isEmpty()) {
            return emptyList()
        }

        // ---------- 1. 构建事件包装列表 ----------
        // 事件类型优先级：PC > CC/PB > NOTE（用于同 tick 排序）
        data class EventWrapper(val tick: Long, val type: EventType, val data: Any)

        val wrappers = mutableListOf<EventWrapper>()

        // 将 Note 包装为事件
        this.notes.forEach { note ->
            wrappers.add(EventWrapper(note.tick, EventType.NOTE, note))
        }

        // 将控制器事件包装，区分 PC 和其他
        this.controllerEvents.forEach { event ->
            when (event) {
                is MidiPCEvent -> wrappers.add(EventWrapper(event.tick, EventType.PC, event))
                is MidiCCEvent, is MidiPBEvent -> wrappers.add(EventWrapper(event.tick, EventType.CC_PB, event))
                else -> {
                    // 其他未知类型，当作普通控制事件处理（不影响拆分）
                    wrappers.add(EventWrapper(event.tick, EventType.CC_PB, event))
                }
            }
        }

        // ---------- 2. 按 tick 排序，同 tick 时优先 PC ----------
        val sortedWrappers = wrappers.sortedWith(compareBy<EventWrapper> { it.tick }
            .thenBy { when (it.type) {
                EventType.PC -> 0
                EventType.CC_PB -> 1
                EventType.NOTE -> 2
            } })

        // ---------- 3. 扫描拆分 ----------
        val result = mutableListOf<MidiTrack>()
        var lastPC: MidiPCEvent? = null
        val currentNotes = mutableListOf<Note>()
        val currentControllers = mutableListOf<MidiEvent>()

        // 辅助函数：将当前积累的内容生成一个子轨道
        fun flushCurrentSegment() {
            if (currentNotes.isEmpty() && currentControllers.isEmpty()) return

            val newTrack = MidiTrack(
                name = "${this.name}_part${result.size + 1}"
            )
            // 如果有 lastPC，将其作为第一个控制器事件（头部）
            lastPC?.let { newTrack.controllerEvents.add(it) }
            // 添加当前积累的音符和控制事件
            newTrack.notes.addAll(currentNotes)
            newTrack.controllerEvents.addAll(currentControllers)

            result.add(newTrack)
            currentNotes.clear()
            currentControllers.clear()
        }

        for (wrapper in sortedWrappers) {
            when (wrapper.type) {
                EventType.PC -> {
                    // 遇到 PC：结束当前段（若有内容），并更新 lastPC
                    flushCurrentSegment()
                    lastPC = wrapper.data as MidiPCEvent
                }
                EventType.CC_PB -> {
                    // 控制事件（非 PC）归入当前段
                    currentControllers.add(wrapper.data as MidiEvent)
                }
                EventType.NOTE -> {
                    // 音符归入当前段
                    currentNotes.add(wrapper.data as Note)
                }
            }
        }

        // 处理最后一段
        flushCurrentSegment()

        // 如果没有任何分段（例如只有 PC 事件而无其他事件），result 可能为空
        // 此时我们仍然可能希望保留 PC 事件本身？但既然没有音符，通常丢弃。
        return result
    }
}

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