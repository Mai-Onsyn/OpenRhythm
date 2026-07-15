package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiChannelStatus
import dev.atsushieno.ktmidi.MidiOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.util.Time

class MidiPlayer(var deviceOutput: MidiOutput?) {
    enum class State { PLAYING, STOPPED, PAUSED }

    private data class ScheduledEvent(
        val tick: Long,
        val order: Int,      // 0 = NoteOff, 1 = NoteOn，用于同一时间排序
        val message: ByteArray,
        val logMsg: String,
        val originalData: Any
    )

    private var state: State = State.STOPPED
    private var currentTick: Long = 0L
    private var currentEventIndex: Int = 0
    private var currentTempo: Double = 120.0
    private var performingChannel = 0
    private var midi: Midi? = null
    private var eventList: MutableList<ScheduledEvent> = mutableListOf()
    private var currentPlayStartNanoOffset = -1L
    private var currentPlayStartNanos = -1L

    private val midiStateTracker = MidiStateTracker()
    private val channelIndices = Array(16) { ChannelControllerIndex() }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var playJob: Job? = null

    fun play() {
        if (midi == null) {
            Logger.w { "MidiPlayer.play(): no midi loaded" }
            return
        }

        state = State.PLAYING
        playJob = scope.launch {
            val startTick = currentTick
            val startTickNanoOffset = midi!!.nanoAtTick(startTick)
            val startNanos = Time.nanos
            currentPlayStartNanoOffset = startTickNanoOffset
            currentPlayStartNanos = startNanos
            currentEventIndex = eventList.indexOfFirst { it.tick >= startTick }
            while (state == State.PLAYING) {
                ensureActive()

                if (currentEventIndex >= eventList.size) {
                    state = State.STOPPED
                    break
                }
                val event = eventList[currentEventIndex++]
                val eventNanoOffset = midi!!.nanoAtTick(event.tick)

                val now = Time.nanos
                val remainingNanos = eventNanoOffset - startTickNanoOffset - (now - startNanos)
                if (remainingNanos > 0) {
                    var shouldContinue = true
                    try {
                        Time.wait(remainingNanos / 1_000_000L) { shouldContinue = false }
                    } catch (_: CancellationException) {}
                    if (!shouldContinue) {
                        val elapsedTickNanos = Time.nanos - startNanos + startTickNanoOffset
                        currentTick = midi!!.tickAtNanoOffset(elapsedTickNanos)
                        break
                    }
                }

                deviceOutput?.send(event.message, 0, event.message.size, now)
                midiStateTracker.handle(event)
                currentTick = event.tick
//                Logger.v { "Send event: ${event.logMsg}" }
            }
        }
        Logger.i { "Start playing midi: ${midi!!.name} at tick $currentTick" }
    }

    val precisTick: Long
        get() {
            if (midi == null) return 0
            return when (state) {
                State.STOPPED -> currentTick
                State.PAUSED -> currentTick
                State.PLAYING -> {
                    val elapsedTickNanos = Time.nanos - currentPlayStartNanos + currentPlayStartNanoOffset
                    midi!!.tickAtNanoOffset(elapsedTickNanos)
                }
            }
        }

    fun setMidi(midi: Midi) {
        this.midi = midi

        eventList.clear()
        channelIndices.forEach {
            it.programChanges.clear()
            it.pitchBends.clear()
            it.ccByController.clear()
        }
        for (track in midi.tracks) {
            for (note in track.notes) {
                val onMsg = byteArrayOf(
                    (MidiChannelStatus.NOTE_ON or note.channel).toByte(),
                    note.pitch.toByte(),
                    note.velocity.toByte()
                )
                val offMsg = byteArrayOf(
                    (MidiChannelStatus.NOTE_OFF or note.channel).toByte(),
                    note.pitch.toByte(),
                    0.toByte()
                )

                eventList.add(ScheduledEvent(note.tick + note.duration, 0, offMsg, "Off{$note}", note))
                eventList.add(ScheduledEvent(note.tick, 1, onMsg, "On{$note}", note))
            }

            for (event in track.controllerEvents) {
                eventList.add(ScheduledEvent(event.tick, -1, event.event, "ControllerEvent{$event}", event))

                val idx = channelIndices[event.channel]
                when (event) {
                    is MidiPCEvent -> idx.programChanges.add(event)
                    is MidiPBEvent -> idx.pitchBends.add(event)
                    is MidiCCEvent -> idx.ccByController.getOrPut(event.controller) { mutableListOf() }.add(event)
                }
            }
        }
        eventList.sortWith(compareBy({ it.tick }, { it.order }))

        Logger.i { "Switched to midi ${midi.name} with ${eventList.size} events" }
    }

    fun seek(percent: Double) {
        if (midi == null) return

        val tick = (midi!!.totalTicks * percent.coerceIn(0.0, 1.0)).toLong()

        if (state == State.PLAYING) {
            pause()
            currentTick = tick
            play()
        }
        else currentTick = tick
        resyncControllerState(tick)
    }

    fun seek(tick: Long) {
        if (midi == null) return
        if (state == State.PLAYING) {
            pause()
            currentTick = tick
            play()
        }
        else currentTick = tick
        resyncControllerState(tick)
    }

    fun pressKey(key: Int, velocity: Int) {
        sendShortEvent(
            (MidiChannelStatus.NOTE_ON or performingChannel).toByte(),
            key.toByte(),
            velocity.toByte()
        )
    }

    fun releaseKey(key: Int) {
        sendShortEvent(
            (MidiChannelStatus.NOTE_OFF or performingChannel).toByte(),
            key.toByte(),
            0.toByte()
        )
    }

    fun control(controller: Int, value: Int) {
        sendShortEvent(
            (MidiChannelStatus.CC or performingChannel).toByte(),
            controller.toByte(),
            value.toByte()
        )
    }

    private fun sendShortEvent(byte1: Byte, byte2: Byte, byte3: Byte) {
        deviceOutput?.let {
            val msg = byteArrayOf(byte1, byte2, byte3)
            it.send(msg, 0, 3, Time.nanos)
        }
    }

    fun pause() {
        state = State.PAUSED
        runBlocking { playJob?.cancelAndJoin() }
        playJob = null
        deviceOutput?.let {
            midiStateTracker.stopActiveNotes(it)
            midiStateTracker.resetControllers(it)
        }
        midiStateTracker.clear()
        Logger.i { "Paused at tick $currentTick (${100.0 * currentTick / (midi?.totalTicks ?: 0)}% of song)" }
    }

    fun stop() {
        state = State.STOPPED
        runBlocking { playJob?.cancelAndJoin() }
        playJob = null
        currentTick = 0
        currentEventIndex = 0
        currentTempo = 120.0
        deviceOutput?.let {
            midiStateTracker.stopActiveNotes(it)
            midiStateTracker.resetControllers(it)
        }
        midiStateTracker.clear()
        Logger.i { "Player stopped" }
    }

    private fun resyncControllerState(targetTick: Long) {
        val output = deviceOutput ?: return

        for (channel in 0 until 16) {
            val idx = channelIndices[channel]

            // Program Change：找最后一个 <= targetTick 的
            findLastAtOrBefore(idx.programChanges, targetTick) { it.tick }?.let { pc ->
                output.send(byteArrayOf((0xC0 or channel).toByte(), pc.program.toByte()), 0, 2, Time.nanos)
            }

            // Pitch Bend
            findLastAtOrBefore(idx.pitchBends, targetTick) { it.tick }?.let { pb ->
                val lsb = pb.value and 0x7F
                val msb = (pb.value shr 7) and 0x7F
                output.send(byteArrayOf((0xE0 or channel).toByte(), lsb.toByte(), msb.toByte()), 0, 3, Time.nanos)
            }

            // 每个出现过的 CC 号，各自找最后状态
            for ((controller, list) in idx.ccByController) {
                findLastAtOrBefore(list, targetTick) { it.tick }?.let { cc ->
                    output.send(byteArrayOf((0xB0 or channel).toByte(), controller.toByte(), cc.value.toByte()), 0, 3, Time.nanos)
                }
            }
        }
    }

    private inline fun <T> findLastAtOrBefore(list: List<T>, target: Long, tick: (T) -> Long): T? {
        if (list.isEmpty()) return null
        var lo = 0
        var hi = list.size - 1
        var result = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (tick(list[mid]) <= target) {
                result = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return if (result >= 0) list[result] else null
    }

    private class ChannelControllerIndex {
        val programChanges = mutableListOf<MidiPCEvent>()
        val pitchBends = mutableListOf<MidiPBEvent>()
        val ccByController = mutableMapOf<Int, MutableList<MidiCCEvent>>() // controller number -> 该 controller 的事件列表
    }

    private class MidiStateTracker {
        // channel -> pitch集合
        val activeNotes = Array(16) { mutableSetOf<Int>() }

        // channel -> controller -> value
        val controllers = Array(16) { IntArray(128) }

        fun handle(event: ScheduledEvent) {
            when (event.originalData) {
                is Note -> {
                    if (event.order == 1) {
                        activeNotes[event.originalData.channel].add(event.originalData.pitch)
                    } else {
                        activeNotes[event.originalData.channel].remove(event.originalData.pitch)
                    }
                }
                is MidiCCEvent -> {
                    controllers[event.originalData.channel][event.originalData.controller] = event.originalData.value
                }
            }
        }

        fun clear() {
            activeNotes.forEach { it.clear() }
            controllers.forEach { it.fill(0) }
        }

        fun stopActiveNotes(output: MidiOutput) {
            for(channel in 0 until 16) {
                for(pitch in activeNotes[channel]) {
                    val msg = byteArrayOf(
                        (MidiChannelStatus.NOTE_OFF or channel).toByte(),
                        pitch.toByte(),
                        0
                    )
                    output.send(
                        msg,
                        0,
                        3,
                        Time.nanos
                    )
                }
            }
        }

        fun resetControllers(output: MidiOutput) {
            for(channel in 0 until 16) {
                // Sustain pedal
                output.send(
                    byteArrayOf((0xB0 or channel).toByte(), 64, 0),
                    0, 3, Time.nanos
                )

                // All notes off
                output.send(
                    byteArrayOf((0xB0 or channel).toByte(), 123, 0),
                    0, 3, Time.nanos
                )

                // All sound off
                output.send(
                    byteArrayOf((0xB0 or channel).toByte(), 120, 0),
                    0, 3, Time.nanos
                )
            }
        }
    }
}