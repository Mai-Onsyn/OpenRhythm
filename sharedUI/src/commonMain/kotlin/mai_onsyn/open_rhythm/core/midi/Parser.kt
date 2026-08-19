package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.Midi1CompoundMessage
import dev.atsushieno.ktmidi.Midi1Music
import dev.atsushieno.ktmidi.read

class CCTimeline(
    val eventList: MutableList<SimpleCCEvent> = mutableListOf()
) {
    data class SimpleCCEvent(
        val tick: Int,
        val controller: Int,
        val value: Int
    )

    fun pushEvent(event: SimpleCCEvent) = eventList.add(event)

    private val controllerStates = IntArray(128) { -1 }
    fun getInterval(range: IntRange): List<SimpleCCEvent> {
        controllerStates.fill(-1)

        var searchIndex = 0
        for ((i, event) in eventList.withIndex()) {
            if (event.tick > range.first) {
                searchIndex = i
                break
            }
            controllerStates[event.controller] = event.value
            if (i + 1 == eventList.size) searchIndex = i + 1
        }

        val result = mutableListOf<SimpleCCEvent>()
        controllerStates.forEachIndexed { controller, value ->
            if (value != -1) {
                result.add(SimpleCCEvent(range.first, controller, value))
            }
        }
        while (searchIndex < eventList.size) {
            val event = eventList[searchIndex++]
            if (event.tick > range.last) {
                break
            }

            result.add(event)
        }

        return result
    }

    override fun toString(): String = "CCTimeline(${eventList.size} events)"
}

class SingleChangeTimeline(
    val eventList: MutableList<SimpleChangeEvent> = mutableListOf()
) {
    data class SimpleChangeEvent(
        val tick: Int,
        val value: Int
    )

    fun pushEvent(event: SimpleChangeEvent) = eventList.add(event)

    fun simplify(default: Int) {
        if (eventList.isEmpty()) return
        var lastValue = default
        var i = 0
        while (i < eventList.size) {
            val currentValue = eventList[i].value
            if (currentValue != lastValue) {
                i++
                lastValue = currentValue
                continue
            }
            else eventList.removeAt(i)
        }
    }

    fun getInterval(range: IntRange): List<SimpleChangeEvent> {
        if (eventList.isEmpty()) return emptyList()

        val end = eventList.lastLessThanOrEqual(range.last) { it.tick } ?: return emptyList()
        val start = eventList.lastLessThanOrEqual(range.first) { it.tick } ?: return mutableListOf<SimpleChangeEvent>().apply {
//            add(SimpleChangeEvent(range.first, 8192))
            addAll(eventList.take(end + 1))
        }

        return mutableListOf<SimpleChangeEvent>().apply {
            this.add(SimpleChangeEvent(range.first, eventList[start].value))
            if (start < end) this.addAll(eventList.subList(start + 1, end + 1))
        }
    }

    private fun <T> List<T>.lastLessThanOrEqual(
        target: Int,
        keySelector: (T) -> Int
    ): Int? {
        var left = 0
        var right = size - 1
        var result: Int? = null

        while (left <= right) {
            val mid = (left + right) ushr 1
            val midKey = keySelector(this[mid])

            if (midKey <= target) {
                result = mid
                left = mid + 1
            } else {
                right = mid - 1
            }
        }

        return result
    }

    override fun toString(): String = "Timeline(${eventList.size} events)"
}

private data class SimpleNoteEvent(
    val on: Boolean,
    val tick: Int,
    val pitch: Int,
    val velocity: Int
)

private data class NoteGroup(
    val channel: Int = 0,
    val noteEvents: MutableList<SimpleNoteEvent> = mutableListOf()
)

fun parseMidi(name: String, bytes: List<Byte>): Midi {
//    val KTMIDI_PARSE_START = Time.nanos
    val midiFile = Midi1Music()
    midiFile.read(bytes)
//    val KTMIDI_PARSE_END__BUILD_TIMELINE_START = Time.nanos

    val tempoEvents = mutableListOf<TempoEvent>()
    val timeSignatureEvents = mutableListOf<TimeSignatureEvent>()

    // 全局通道的控制器状态 与轨道无关 用于合并轨道的时候取对应的状态区间事件
    val ccTimeline = Array(16) { CCTimeline() }
    val pbTimeline = Array(16) { SingleChangeTimeline() }
    val pcTimeline = Array(16) { SingleChangeTimeline() }
    // 先遍历一遍 构建整个midi时间线 顺便找tempo和小节线
    midiFile.tracks.forEach { track ->
        var currentTick = 0
        track.events.forEach { event ->
            val msg = event.message
            val statusByteInt = msg.statusByte.toInt() and 0xFF
            val opcode = statusByteInt and 0xF0
            val channel = msg.channel.toInt()
            currentTick += event.deltaTime

            if (statusByteInt == 0xFF && msg is Midi1CompoundMessage) {
                val metaType = msg.msb.toInt() and 0xFF
                val data = msg.extraData
                val offset = msg.extraDataOffset
                val length = msg.extraDataLength

                when (metaType) {
                    0x51 -> {   // meta tempo
                        // 3 字节大端，单位：微秒/四分音符
                        if (data != null && length >= 3) {
                            val usPerQuarter =
                                ((data[offset].toInt() and 0xFF) shl 16) or
                                        ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                        (data[offset + 2].toInt() and 0xFF)
                            if (usPerQuarter > 0) {
                                tempoEvents.add(TempoEvent(currentTick.toLong(), 60_000_000.0 / usPerQuarter))
                            }
                        }
                    }
                    0x58 -> {   // meta time signature
                        // byte0=分子, byte1=分母的2的幂指数
                        if (data != null && length >= 2) {
                            val numerator = data[offset].toInt() and 0xFF
                            val denominator = 1 shl (data[offset + 1].toInt() and 0xFF)
                            timeSignatureEvents.add(TimeSignatureEvent(currentTick.toLong(), numerator, denominator))
                        }
                    }
                }
                return@forEach
            }

            when(opcode) {
                0xB0 -> {   // CC
                    ccTimeline[channel].pushEvent(CCTimeline.SimpleCCEvent(currentTick, msg.msb.toInt(), msg.lsb.toInt()))
                }
                0xE0 -> {   // PB
                    pbTimeline[channel].pushEvent(SingleChangeTimeline.SimpleChangeEvent(currentTick, msg.msb.toInt() + (msg.lsb.toInt() shl 7)))
                }
                0xC0 -> {   // PC
                    pcTimeline[channel].pushEvent(SingleChangeTimeline.SimpleChangeEvent(currentTick, msg.msb.toInt()))
                }
            }
        }
    }
    ccTimeline.forEach { it.eventList.sortBy { event -> event.tick } }
    pbTimeline.forEach { it.eventList.sortBy { event -> event.tick } }
    pcTimeline.forEach { it.eventList.sortBy { event -> event.tick } }

//    val BUILD_TIMELINE_END_PARSE_NOTE_START = Time.nanos

    // 被<原始轨道-轨道上的多通道>拆分出来的音符组表
    val validTrackGroups = mutableListOf<NoteGroup>()
    midiFile.tracks.forEach { track ->
        var currentTick = 0

        // 为每个通道都分配轨道 防止单轨上出现发送给不同通道音符事件的问题
        val originalNoteGroups = Array(16) { NoteGroup(it) }
        track.events.forEach { event ->
            val msg = event.message
            val statusByteInt = msg.statusByte.toInt() and 0xFF
            val opcode = statusByteInt and 0xF0
            val channel = msg.channel.toInt()

            currentTick += event.deltaTime
            when (opcode) {
                0x80, 0x90 -> {   // Note OFF/ON
                    val pitch = msg.msb.toInt() and 0xFF
                    val velocity = msg.lsb.toInt() and 0xFF
                    val on = opcode == 0x90 && velocity > 0
                    originalNoteGroups[channel].noteEvents.add(SimpleNoteEvent(on, currentTick, pitch, velocity))
                }
            }
        }
        originalNoteGroups.forEach {
            if (it.noteEvents.isNotEmpty()) {
                validTrackGroups.add(it)
            }
        }
    }

//    val PARSE_NOTE_END_SIMPLIFY_START = Time.nanos

    for (i in 0 until 16) {
        pcTimeline[i].simplify(0)
        pbTimeline[i].simplify(8192)
    }

//    val SIMPLIFY_END_BUILD_RESULT_START = Time.nanos

    val resultTrackList = mutableListOf<MidiTrack>()
    var firstTick = Int.MAX_VALUE
    var lastTick = 0
    for (group in validTrackGroups) {
        val chunkStartTick = group.noteEvents.first().tick
        val chunkEndTick = group.noteEvents.last().tick
        if (firstTick > chunkStartTick) firstTick = chunkStartTick
        if (lastTick < chunkEndTick) lastTick = chunkEndTick

        val cpcLine = pcTimeline[group.channel]
        val cpbLine = pbTimeline[group.channel]
        val cccLine = ccTimeline[group.channel]

//        val SOTR_START = Time.nanos
//        group.noteEvents.sortWith(compareBy({ it.tick }, { it.on }))
//        val SOTR_END_MERGE_START = Time.nanos
        val notes = mergeToNoteList(group)
//        logDurations("build", listOf("sort", "merge"), SOTR_START, SOTR_END_MERGE_START, Time.nanos)

        fun detectInstTrack(inst: Int, range: IntRange) {
//            val DETECT_INST_TRACK_START = Time.nanos
            val ccEvents = cccLine.getInterval(range)
            val pbEvents = cpbLine.getInterval(range)

            resultTrackList.add(
                MidiTrack(
                    notes = notes.takeRange(range),
                    controllerEvents = ccEvents.mergeWith(
                        pbEvents,
                        { it.tick },
                        { it.tick },
                        { MidiCCEvent.of(it.tick.toLong(), group.channel, it.controller, it.value) },
                        { MidiPBEvent.of(it.tick.toLong(), group.channel, it.value) }
                    ).apply {
                        this.add(0, MidiPCEvent.of(range.first.toLong(), group.channel, inst))
                    },
                    tickRange = range,
                    trackInst = inst,
                    enable = true,
                    visible = true//group.channel != 9
                )
            )
//            Logger.d { "Detect Inst Track cost ${(Time.nanos - DETECT_INST_TRACK_START) / 1000000f}ms" }
        }
        if (firstTick == Int.MAX_VALUE) firstTick = 0

        // 单通道独占乐器 简化操作
        if (cpcLine.eventList.size < 2) {
            detectInstTrack(cpcLine.eventList.firstOrNull()?.value ?: 0, chunkStartTick..chunkEndTick)
        } else {    // 单通道多乐器 先获取音符区间的pc事件 再按pc事件在同一通道上拆分逻辑轨道
            val pcEvents = cpcLine.getInterval(chunkStartTick..chunkEndTick)
            if (pcEvents.size < 2) {
                // 通道有多个乐器但该通道的音符覆盖的区域只有单一乐器
                detectInstTrack(pcEvents.firstOrNull()?.value ?: cpcLine.eventList.firstOrNull()?.value ?: 0, chunkStartTick..chunkEndTick)
            } else {
                pcEvents.forEachIndexed { index, pc ->
                    val instRange = pc.tick..(pcEvents.getOrNull(index + 1)?.tick ?: chunkEndTick)
                    detectInstTrack(pc.value, instRange)
                }
            }
        }
    }

//    val BUILD_RESULT_END = Time.nanos
//    logDurations(
//        name,
//        listOf("ktmidi parse", "build timeline", "parse note", "simplify", "build result"),
//        KTMIDI_PARSE_START, KTMIDI_PARSE_END__BUILD_TIMELINE_START, BUILD_TIMELINE_END_PARSE_NOTE_START, PARSE_NOTE_END_SIMPLIFY_START, SIMPLIFY_END_BUILD_RESULT_START, BUILD_RESULT_END
//        )

    return Midi(
        name = name,
        ppq = midiFile.deltaTimeSpec,
        totalTicks = lastTick,//midiFile.getTotalTicks(),
        tracks = resultTrackList,
        tempoEvents = tempoEvents,
        timeSignatureEvents = timeSignatureEvents,
        startTick = firstTick,
        endTick = lastTick,
        ccChangeTimeline = ccTimeline,
        pcChangeTimeline = pcTimeline,
        pbChangeTimeline = pbTimeline
    )
}

fun logDurations(title: String, labels: List<String>, vararg timestamps: Long) {
    val sb = StringBuilder("$title: ")
    for ((idx, label) in labels.withIndex()) {
        sb.append("$label: ${(timestamps[idx + 1] - timestamps[idx]) / 1000000f}ms")
        if (idx != labels.size - 1) {
            sb.append(", ")
        }
    }
    Logger.d { sb.toString() }
}

private fun mergeToNoteList(group: NoteGroup): MutableList<Note> {
    val noteList = mutableListOf<Note>()

    // 为 128 个 MIDI 音高分别建立 FIFO 队列 (0..127)
    val noteActive = Array(128) { ArrayDeque<SimpleNoteEvent>() }

    group.noteEvents.forEach { event ->
        val pitch = event.pitch
        if (pitch !in 0..127) return@forEach

        if (event.on) {
            // Note ON：直接压入队列尾部，不强行闭合前音
            noteActive[pitch].addLast(event)
        } else {
            // Note OFF：弹出队列头部最早的 Note ON (FIFO)
            val pressEvent = noteActive[pitch].removeFirstOrNull()
            if (pressEvent != null) {
                val duration = (event.tick - pressEvent.tick).toLong()
                if (duration > 0) {
                    noteList.add(
                        Note(
                            pitch,
                            pressEvent.tick.toLong(),
                            duration,
                            pressEvent.velocity,
                            group.channel
                        )
                    )
                }
            }
        }
    }

    // 重新按按下时间排序，保证输出顺序正确
    noteList.sortBy { it.tick }
    return noteList
}

private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

/**
 * 合并两个已按 Key 升序的集合，分别转换成同类型 C，返回惰性序列。
 * 当 Key 相等时，先输出 transformA 的结果，再输出 transformB 的结果。
 */
fun <A, B, C, K : Comparable<K>> Iterable<A>.mergeWith(
    other: Iterable<B>,
    keyA: (A) -> K,
    keyB: (B) -> K,
    transformA: (A) -> C,
    transformB: (B) -> C
): MutableList<C> = sequence {
    val itA = iterator()
    val itB = other.iterator()
    var a = itA.nextOrNull()
    var b = itB.nextOrNull()

    while (a != null && b != null) {
        when (val compare = keyA(a).compareTo(keyB(b))) {
            -1 -> {
                yield(transformA(a))
                a = itA.nextOrNull()
            }
            1 -> {
                yield(transformB(b))
                b = itB.nextOrNull()
            }
            else -> { // 同一个 Int 值（比如 id），生成两条独立数据
                yield(transformA(a))
                yield(transformB(b))
                a = itA.nextOrNull()
                b = itB.nextOrNull()
            }
        }
    }
    while (a != null) { yield(transformA(a)); a = itA.nextOrNull() }
    while (b != null) { yield(transformB(b)); b = itB.nextOrNull() }
}.toMutableList()

//fun MutableList<Note>.takeRange(range: IntRange): MutableList<Note> {
//    if (this.isEmpty()) return this
//    if (this.first().tick in range && this.last().tick in range) {
//        return this
//    }
//
//    val result = mutableListOf<Note>()
//    for (i in this) {
//        if (i.tick > range.last) break  // 提前退出
//        if (i.tick < range.first) continue
//        result.add(i)
//    }
//    return result
//}

fun MutableList<Note>.takeRange(range: IntRange): MutableList<Note> {
    if (this.isEmpty()) return this
    if (this.first().tick >= range.first && this.last().tick <= range.last) {
        return this
    }

    // 二分查找第一个 >= range.first 的索引
    val start = binarySearchLower(range.first)
    if (start >= size || this[start].tick > range.last) return mutableListOf()

    // 二分查找最后一个 <= range.last 的索引
    val end = binarySearchUpper(range.last)

    if (start > end) return mutableListOf()

    // 直接截取子列表，避免逐个遍历 compare
    return this.subList(start, end + 1).toMutableList()
}

private fun List<Note>.binarySearchLower(targetTick: Int): Int {
    var low = 0
    var high = size - 1
    var result = size
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (this[mid].tick >= targetTick) {
            result = mid
            high = mid - 1
        } else {
            low = mid + 1
        }
    }
    return result
}

private fun List<Note>.binarySearchUpper(targetTick: Int): Int {
    var low = 0
    var high = size - 1
    var result = -1
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (this[mid].tick <= targetTick) {
            result = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return result
}