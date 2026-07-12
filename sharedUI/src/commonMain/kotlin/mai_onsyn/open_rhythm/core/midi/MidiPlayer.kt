package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiChannelStatus
import dev.atsushieno.ktmidi.MidiOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
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
    private var midi: Midi? = null
    private var eventList: MutableList<ScheduledEvent> = mutableListOf()

    private val midiStateTracker = MidiStateTracker()
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
                    Time.wait(remainingNanos / 1_000_000L) { shouldContinue = false }
                    if (!shouldContinue) {
                        val breakPointNanos = Time.nanos
                        val elapsedTickNanos = breakPointNanos - startNanos + startTickNanoOffset
                        currentTick = midi!!.tickAtNanoOffset(elapsedTickNanos)
                        break
                    }
                }

                deviceOutput?.send(event.message, 0, event.message.size, now)
                midiStateTracker.handle(event)
                currentTick = event.tick
                Logger.v { "Send event: ${event.logMsg}" }
            }
        }
        Logger.i { "Start playing midi: ${midi!!.name} at tick $currentTick" }
    }

    fun setMidi(midi: Midi) {
        this.midi = midi

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
    }

    fun pause() {
        state = State.PAUSED
        playJob?.cancel()
        playJob = null
        deviceOutput?.let {
            midiStateTracker.stopActiveNotes(it)
            midiStateTracker.resetControllers(it)
        }
        midiStateTracker.clear()
        Logger.i { "Paused at tick $currentTick (${100.0 * currentTick / midi!!.totalTicks}% of song)" }
    }

    fun stop() {
        state = State.STOPPED
        playJob?.cancel()
        playJob = null
        currentTick = 0
        currentEventIndex = 0
        currentTempo = 120.0
        Logger.i { "Player stopped" }
    }



    private class MidiStateTracker {
        // channel -> pitch集合
        val activeNotes = Array(16) { mutableSetOf<Int>() }

        // channel -> controller -> value
        val controllers = Array(16) { IntArray(128) }

        fun handle(event: ScheduledEvent) {
            when (event.originalData) {
                is Note -> {
                    val velocity = event.originalData.velocity

                    if (velocity == 0) {
                        activeNotes[event.originalData.channel].remove(event.originalData.pitch)
                    }
                    else {
                        activeNotes[event.originalData.channel].add(event.originalData.pitch)
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