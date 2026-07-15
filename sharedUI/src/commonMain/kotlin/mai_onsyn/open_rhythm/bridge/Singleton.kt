package mai_onsyn.open_rhythm.bridge

import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.midi.MidiPlayer

object Singleton {
    val player: MidiPlayer? = createMidiPlayer(getMidiAccess())
    val settings: UserSetting = UserSetting(createSetting())
}

private fun createMidiPlayer(access: MidiAccess): MidiPlayer? {
    val outputs = access.outputs.toList()
    if (outputs.isEmpty()) {
        println("没有找到可用的 MIDI 输出设备。")
        return null
    }
    val portDetails = outputs.first { it.name?.contains("Microsoft") ?: false }
    val midiOutput = runBlocking { access.openOutput(portDetails.id) }
    return MidiPlayer(midiOutput)
}