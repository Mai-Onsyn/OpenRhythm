import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.MidiChannelStatus
import dev.atsushieno.ktmidi.MidiOutput
import dev.zwander.kotlin.file.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.midi.bpmAtTick
import mai_onsyn.open_rhythm.core.midi.msAtTick
import java.util.Arrays
import java.util.concurrent.CountDownLatch
import kotlin.test.Test

class MidiTest {

    @Test
    fun testLoadAndPlay() {
        val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\蓬莱伝説 v1.11.mid", false)
//        val file = FileUtils.fromString("D:\\Users\\Desktop\\天球の奇蹟.mid", false)
        if (file == null) {
            Logger.e { "Cant find test.mid" }
            return
        }
        val byteArray = file.openInputStream()?.readByteArray()
        if (byteArray == null) {
            Logger.e { "Cant read test.mid" }
            return
        }

        val midi = Midi.fromFile(file.nameWithoutExtension, byteArray.toList())

        playMidi(midi)?.join()
    }

    fun playMidi(midi: Midi): Thread? {
        data class ScheduledEvent(
            val timeMs: Long,
            val order: Int,      // 0 = NoteOff, 1 = NoteOn，用于同一时间排序
            val message: ByteArray,
            val logText: String
        )

        val midiAccess = JvmMidiAccess()
        val outputs = midiAccess.outputs.toList()
        if (outputs.isEmpty()) {
            println("没有找到可用的 MIDI 输出设备。")
            return null
        }
        val portDetails = outputs[3]
        val midiOutput = runBlocking { midiAccess.openOutput(portDetails.id) }

        val events = mutableListOf<ScheduledEvent>()

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

                val onTimeMs = midi.msAtTick(note.tick).toLong()
                val offTimeMs = midi.msAtTick(note.tick + note.duration).toLong()

                events.add(ScheduledEvent(offTimeMs, 0, offMsg, "NoteOff ${Note.toString(note.pitch)}"))
                events.add(ScheduledEvent(onTimeMs, 1, onMsg, "Note $note"))
            }

            for (event in track.controllerEvents) {
                val timeMs = midi.msAtTick(event.tick).toLong()
                events.add(ScheduledEvent(timeMs, -1, event.event, "ControllerEvent: $event"))
            }
        }

        events.sortWith(compareBy({ it.timeMs }, { it.order }))

        return Thread.ofVirtual().start {
            val startNano = System.nanoTime()
            for (event in events) {
                val targetNano = startNano + event.timeMs * 1_000_000
                val remainMs = (targetNano - System.nanoTime()) / 1_000_000
                if (remainMs > 0) {
                    Thread.sleep(remainMs)
                }
                midiOutput.send(event.message, 0, 3, targetNano + 1_000_000_000)
                Logger.i { event.logText }
            }
            midiOutput.close()
        }
    }
}