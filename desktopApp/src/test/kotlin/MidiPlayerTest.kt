import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.zwander.kotlin.file.FileUtils
import kotlinx.coroutines.runBlocking
import kotlinx.io.readByteArray
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.MidiPlayer
import kotlin.test.Test

class MidiPlayerTest {
    @Test
    fun testPlay() {
        val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\最终鬼畜妹.mid", false)
//        val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\蓬莱伝説 v1.11.mid", false)
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

        val midiAccess = JvmMidiAccess()
        val outputs = midiAccess.outputs.toList()
        if (outputs.isEmpty()) {
            println("没有找到可用的 MIDI 输出设备。")
            return
        }
        val portDetails = outputs[3]
        val midiOutput = runBlocking { midiAccess.openOutput(portDetails.id) }
        val player = MidiPlayer(midiOutput)

        player.setMidi(midi)

        player.seek(0.3)
        player.play()
        Thread.sleep(5_000)
//        player.seek(0.3)

        Thread.sleep(5_000)
        player.pause()

        Thread.sleep(5_000)
        player.play()

//        player.seek(0.5)
        Thread.sleep(5_000)
//        player.seek(0.8)
        Thread.sleep(5_000)

        player.pause()
        Thread.sleep(5_000)
        player.play()

        Thread.sleep(150_000)
    }
}