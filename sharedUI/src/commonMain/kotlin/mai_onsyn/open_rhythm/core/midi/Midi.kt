package mai_onsyn.open_rhythm.core.midi

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.Midi1CompoundMessage
import dev.atsushieno.ktmidi.Midi1Music
import dev.atsushieno.ktmidi.read

class Midi(
    val name: String,
    val ppq: Int,
    val totalTicks: Int,
    val tracks: MutableList<MidiTrack> = mutableListOf(),
    val tempoEvents: MutableList<TempoEvent> = mutableListOf(),
    val timeSignatureEvents: MutableList<TimeSignatureEvent> = mutableListOf()
) {
    var hasNoteTracks: Int = 0
        private set

    companion object {
        private const val META_TRACK_NAME = 0x03
        private const val META_TEMPO = 0x51
        private const val META_TIME_SIGNATURE = 0x58

        fun fromFile(name: String, bytes: List<Byte>): Midi {
            val midiFile = Midi1Music()
            midiFile.read(bytes)

            val ppq = midiFile.deltaTimeSpec // 正数即 ticks per quarter note

            val tracks = mutableListOf<MidiTrack>()
            val tempoEvents = mutableListOf<TempoEvent>()
            val timeSignatureEvents = mutableListOf<TimeSignatureEvent>()

            for (track in midiFile.tracks) {
                var deltaTick = 0L
                val resultTrack = MidiTrack()

                data class UniqueNoteKey(val pitch: Int, val channel: Int)
                data class UniqueNoteData(val velocity: Int, val tick: Long)
                val pressedNote = HashMap<UniqueNoteKey, UniqueNoteData>()

                for (event in track.events) {
                    deltaTick += event.deltaTime

                    val msg = event.message
                    val statusByteInt = msg.statusByte.toInt() and 0xFF

                    // ---- Meta 事件（0xFF）单独处理，没有 channel 概念 ----
                    if (statusByteInt == 0xFF && msg is Midi1CompoundMessage) {
                        val metaType = msg.msb.toInt() and 0xFF
                        val data = msg.extraData
                        val offset = msg.extraDataOffset
                        val length = msg.extraDataLength

                        when (metaType) {
                            META_TRACK_NAME -> {
                                if (data != null) {
                                    resultTrack.name = data.decodeToString(offset, offset + length)//String(data, offset, length)
                                }
                            }
                            META_TEMPO -> {
                                // 3 字节大端，单位：微秒/四分音符
                                if (data != null && length >= 3) {
                                    val usPerQuarter =
                                        ((data[offset].toInt() and 0xFF) shl 16) or
                                                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                                (data[offset + 2].toInt() and 0xFF)
                                    if (usPerQuarter > 0) {
                                        tempoEvents.add(TempoEvent(deltaTick, 60_000_000.0 / usPerQuarter))
                                    }
                                }
                            }
                            META_TIME_SIGNATURE -> {
                                // byte0=分子, byte1=分母的2的幂指数
                                if (data != null && length >= 2) {
                                    val numerator = data[offset].toInt() and 0xFF
                                    val denominator = 1 shl (data[offset + 1].toInt() and 0xFF)
                                    timeSignatureEvents.add(TimeSignatureEvent(deltaTick, numerator, denominator))
                                }
                            }
                        }
                        continue
                    }

                    // ---- 普通 Channel Message ----
                    val opcode = statusByteInt and 0xF0
                    val channel = msg.channel.toInt()

                    when (opcode) {
                        0x90, 0x80 -> {
                            val noteNumber = msg.msb.toInt() and 0x7F
                            val velocity = msg.lsb.toInt() and 0x7F
                            val key = UniqueNoteKey(noteNumber, channel)

                            fun handleNoteOff() {
                                if (pressedNote.contains(key)) {
                                    val old = pressedNote[key]!!
                                    val duration = deltaTick - old.tick
                                    if (duration > 0) {
                                        resultTrack.notes.add(Note(noteNumber, old.tick, duration, old.velocity, channel))
                                    }
                                    pressedNote.remove(key)
                                }
                            }

                            if (opcode == 0x90 && velocity > 0) {
                                if (pressedNote.contains(key)) {
                                    val old = pressedNote[key]!!
                                    val duration = deltaTick - old.tick
                                    if (duration > 0) {
                                        // 用旧音符自己的 velocity，而不是当前 NoteOn 的 velocity
                                        resultTrack.notes.add(Note(noteNumber, old.tick, duration, old.velocity, channel))
                                    }
                                }
                                pressedNote[key] = UniqueNoteData(velocity, deltaTick)
                            } else {
                                handleNoteOff()
                            }
                        }
                        0xB0 -> { // Control Change
                            resultTrack.controllerEvents.add(
                                MidiCCEvent(deltaTick, byteArrayOf(msg.statusByte, msg.msb, msg.lsb))
                            )
                        }
                        0xC0 -> { // Program Change（只有 1 个数据字节）
                            resultTrack.controllerEvents.add(
                                MidiPCEvent(deltaTick, byteArrayOf(msg.statusByte, msg.msb))
                            )
                        }
                        0xE0 -> { // Pitch Bend
                            resultTrack.controllerEvents.add(
                                MidiPBEvent(deltaTick, byteArrayOf(msg.statusByte, msg.msb, msg.lsb))
                            )
                        }
                    }
                }
                tracks.add(resultTrack)
            }

            return Midi(
                name, ppq,
                midiFile.getTotalTicks(),
                tracks, tempoEvents, timeSignatureEvents
            ).apply { divideMultiChannelTrack(this) }
        }

        private fun divideMultiChannelTrack(midi: Midi) {
            var idx = 0
            while (idx < midi.tracks.size) {
                val splitTracks = midi.tracks[idx].splitTrackByPC()
                if (splitTracks.isEmpty()) {
                    midi.tracks.removeAt(idx)
                    continue
                }
                midi.tracks[idx] = splitTracks[0]
                for (j in 1 until splitTracks.size) {
                    midi.tracks.add(splitTracks[j])
                    Logger.d { "Added track ${midi.tracks.size - 1} from track ${splitTracks[j].name}" }
                }
                idx++
            }
//            for (i in 0 until midi.tracks.size) {
//                val splitTracks = midi.tracks[i].splitTrackByPC()
//                if (splitTracks.isEmpty()) {
//                    midi.tracks.removeAt(i)
//                    continue
//                }
//                midi.tracks[i] = splitTracks[0]
//                for (j in 1 until splitTracks.size) {
//                    midi.tracks.add(splitTracks[j])
//                    Logger.d { "Added track ${midi.tracks.size - 1} from track ${splitTracks[j].name}" }
//                }
//            }

            val (nonEmpty, empty) = midi.tracks.partition { it.notes.isNotEmpty() }
            midi.tracks.clear()
            midi.tracks.addAll(nonEmpty + empty)
            midi.hasNoteTracks = nonEmpty.size
//            for (track in midi.tracks) {
//                track.controllerEvents.forEach {
//                    if (it is MidiPCEvent) {
//                        Logger.d { "${track.name} has PC event: channel ${it.channel} at tick ${it.tick} change to ${it.program}" }
//                    }
//                }
//            }
        }
    }
}