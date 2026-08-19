package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiOutput
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mai_onsyn.open_rhythm.core.util.NoteBlocker
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.core.util.nanoAtTick
import mai_onsyn.open_rhythm.core.util.tickAtNanoOffset

class MidiPlayer2(
    var midiOutput: MidiOutput? = null
) {
    enum class State { PLAYING, STOPPED, PAUSED, WAITING }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var senderThread: Job? = null
    private var playbackThread: Job? = null
    private val eventChannel = Channel<ByteArray>(Channel.UNLIMITED)

    private var midi: Midi? = null
    private var state: State = State.STOPPED
    private val eventList = mutableListOf<MidiEvent>()
    private var playingIndex = 0
    private var offsetTick = 0.0
    private var offsetNanos = 0L
    private var speed = 1.0f
    var interactChannel = 0

    var practiceMode = false
    var blocker = NoteBlocker()
        private set

    var onCompletion: (() -> Unit)? = null

    init { launchGuardThread() }

    fun setOutput(output: MidiOutput?) {
        stopPlayback()
        senderThread?.cancel()
        this.midiOutput = output
        launchGuardThread()
    }

    fun setMidi(midi: Midi?) {
        this.midi = midi
        reset()
        buildEventSequence(midi)
    }

    val preciseTick: Double
        get() = when (state) {
            State.PLAYING -> lerpTick()
            State.STOPPED, State.PAUSED, State.WAITING -> offsetTick
        }

    fun play() {
        val pMidi = midi ?: return
        playbackThread?.cancel()
        launchPlaybackThread(pMidi)
        Logger.i { "Player Playing" }
    }

    fun pause() {
        stopPlayback()
        if (state == State.PLAYING) {
            offsetTick = preciseTick
        }
        releaseAllNotes()
        state = State.PAUSED
        Logger.i { "Player Paused" }
    }

    fun stop() {
        stopPlayback()
        reset()
        state = State.STOPPED
        Logger.i { "Player Stopped" }
    }

    fun seek(value: Double, percentage: Boolean = true) {
        if (percentage) {
            val totalTicks = midi?.totalTicks ?: return
            seek((totalTicks * value.coerceIn(0.0, 1.0)).toLong())
        } else {
            if (midi == null) return

            var shouldReplay = false
            if (state == State.PLAYING) {
                shouldReplay = true
                pause()
            }
            this.offsetTick = value
            if (shouldReplay) play()
        }
    }

    fun seek(tick: Long) {
        if (midi == null) return

        var shouldReplay = false
        if (state == State.PLAYING) {
            shouldReplay = true
            pause()
        }
        this.offsetTick = tick.toDouble()
        if (shouldReplay) play()
    }

    fun setSpeed(speed: Float) {
        if (speed <= 0f || this.speed == speed) return
        if (state == State.PLAYING) {
            offsetTick = lerpTick()
            offsetNanos = Time.nanos
            // 随后重启播放线程（重新以新的 offsetTick 和新的 speed 建立内部锚点）
            stopPlayback()
            midi?.let { launchPlaybackThread(it, false) }
        }
        this.speed = speed
    }

    fun getSpeed(): Float = speed

    fun noteOn(key: Int, velocity: Int, channel: Int = interactChannel) =
        eventChannel.trySend(createMidiMessage(0x90, channel, key, velocity))

    fun noteOff(key: Int, channel: Int = interactChannel) =
        eventChannel.trySend(createMidiMessage(0x80, channel, key))

    fun cc(controller: Int, value: Int, channel: Int = interactChannel) =
        eventChannel.trySend(createMidiMessage(0xB0, channel, controller, value))

    fun pc(value: Int, channel: Int = interactChannel) =
        eventChannel.trySend(createMidiMessage(0xC0, channel, value))

    fun pb(value: Int, channel: Int = interactChannel) =
        eventChannel.trySend(createMidiMessage(0xE0, channel, (value and 0x7F), (value shr 7 and 0x7F)))

    fun sendShortEvent(bytes: ByteArray) {
        eventChannel.trySend(bytes)
    }

    private fun lerpTick(): Double {
        if (midi == null) return 0.0

        // 在offsetTick时 从tick0开始已经过的播放器内纳秒为
        val baseNano = midi!!.nanoAtTick(offsetTick)
        // 从记录时刻到当前时刻 现实时间差为
        val realDelta = Time.nanos - offsetNanos
        // speed为时间倍率 意味着播放器内时间流逝速度是现实的speed倍 因此这段时间内播放器内增加的纳秒为
        val gameDelta = (speed * realDelta).toDouble()
        // 当前时刻 从tick0起总共经过的播放器内纳秒为
        val totalNano = baseNano + gameDelta
        // 用nanoAtTick的反函数tickAtNanoOffset即可得到当前tick
        val currentTick = midi!!.tickAtNanoOffset(totalNano)

        return currentTick
    }

    private fun buildEventSequence(midi: Midi?) {
        if (midi == null) {
            eventList.clear()
            return
        }
        eventList.clear()

        for (track in midi.tracks) {
            for (note in track.notes) {
                eventList.add(NoteEvent.noteOn(note.tick, note.pitch, note.velocity, note.channel))
                eventList.add(NoteEvent.noteOff(note.tick + note.duration, note.pitch, note.velocity, note.channel))
            }
            for (event in track.controllerEvents) {
                eventList.add(event)
            }
        }
        eventList.sortWith(compareBy({ it.tick }, { it.order }))
    }

    private fun launchGuardThread() {
        senderThread = scope.launch {
            for (msg in eventChannel) {
                try {
                    midiOutput?.send(msg, 0, msg.size, 0)
//                    Logger.v { "Send event: ${msg.contentToString()}" }
//                    if (msg[0].toInt() and 0xF0 == 0xE0) {
//                        Logger.w { "Send PB to channel ${msg[0].toInt() and 0x0F}, value ${msg[1].toInt() + (msg[2].toInt() shl 7)}" }
//                    }
                } catch (e: Exception) {
                    Logger.e { "Error while sending message: ${e.message}" }
                }
            }
        }
    }

    private fun launchPlaybackThread(midi: Midi, doClearTask: Boolean = true) {
        playbackThread = scope.launch {
            if (doClearTask) sendPreplayStatus(midi, offsetTick)
            state = State.PLAYING

            var startNanos = Time.nanos
            var startMidiNanos = midi.nanoAtTick(offsetTick)

            offsetNanos = startNanos
            playingIndex = eventList.indexOfFirst { it.tick >= offsetTick }.coerceAtLeast(0)

            val currTickEvents = mutableListOf<MidiEvent>()
            while (isActive && state == State.PLAYING) {
                ensureActive()
                if (playingIndex >= eventList.size) {
                    state = State.PAUSED
                    onCompletion?.invoke()
                    break
                }
                currTickEvents.clear()
                currTickEvents.add(eventList[playingIndex])
                val eventTick = currTickEvents.first().tick
                while (++playingIndex < eventList.size && eventList[playingIndex].tick == eventTick) {
                    currTickEvents.add(eventList[playingIndex])
                }

                val targetMidiDelta = midi.nanoAtTick(eventTick) - startMidiNanos
                val targetRealDelta = (targetMidiDelta / speed).toLong()

                val remainingNanos = targetRealDelta - (Time.nanos - startNanos)

                if (remainingNanos > 0 && !wait(remainingNanos)) {
                    break
                }

                offsetTick = eventTick.toDouble()
                offsetNanos = Time.nanos
                if (practiceMode) {
                    val notes = currTickEvents.filter { it is NoteEvent && it.on }
                    if (notes.isNotEmpty()) {
                        offsetTick++
                        state = State.WAITING
                        blocker.await(notes.mapTo(mutableSetOf()) { (it as NoteEvent).pitch })

                        startNanos = Time.nanos
                        startMidiNanos = midi.nanoAtTick(offsetTick)
                        offsetNanos = Time.nanos
                        state = State.PLAYING
                        blocker.clear()
                    }
                } else currTickEvents.forEach {
                    eventChannel.send(it.event)
                }
            }
        }
    }

    private suspend fun wait(nanos: Long): Boolean {
        var shouldContinue = true
        Time.waitNanos(nanos) { shouldContinue = false }
        return shouldContinue
    }

    private fun stopPlayback() {
        playbackThread?.cancel()
        playbackThread = null
    }

    private fun createMidiMessage(status: Int, channel: Int, data1: Int, data2: Int? = null): ByteArray {
        val byte1 = (status or channel).toByte()
        val byte2 = data1.toByte()
        return if (data2 != null) byteArrayOf(byte1, byte2, data2.toByte()) else byteArrayOf(byte1, byte2)
    }

    /**
     * 释放所有音符并松开踏板
     */
    private fun releaseAllNotes() {
        for (i in 0..15) {
            cc(123, 0, i)
            cc(64, 0, i)
        }
    }

    /**
     * 释放所有音符
     * 同时重置所有通道的乐器为钢琴 重置PB 重置CC
     */
    private fun reset() {
        for (i in 0..15) {
            pc(0, i)
            pb(8192, i)
            cc(123, 0, i)
            cc(121, 0, i)
        }
    }

    private fun sendPreplayStatus(midi: Midi, tick: Double) {
        val range = tick.toInt()..tick.toInt()
        reset()
        for (i in 0..15) {
            midi.ccChangeTimeline[i].getInterval(range).forEach {
                cc(it.controller, it.value, i)
            }
            midi.pcChangeTimeline[i].getInterval(range).forEach {
                pc(it.value, i)
            }
            midi.pbChangeTimeline[i].getInterval(range).forEach {
                pb(it.value, i)
            }
//            cc(7, 127, i)
        }
    }
}