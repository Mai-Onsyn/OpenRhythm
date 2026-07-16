package mai_onsyn.open_rhythm.bridge

import dev.atsushieno.ktmidi.MidiAccess
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.midi.MidiPlayer

object Singleton {
    val player: MidiPlayer = createMidiPlayer(getMidiAccess())
    val settings: UserSetting = UserSetting(createSetting())
}

private fun createMidiPlayer(access: MidiAccess): MidiPlayer {
    val outputs = access.outputs.toList()
    if (outputs.isEmpty()) {
        println("没有找到可用的 MIDI 输出设备。")
        return MidiPlayer(null)
    }

    outputs.firstOrNull { it.name?.contains("Microsoft") ?: false }?.let {
        val midiOutput = runBlocking { access.openOutput(it.id) }
        return MidiPlayer(midiOutput)
    }

    outputs.firstOrNull()?.let {
        return MidiPlayer(runBlocking { access.openOutput(it.id) })
    }

    return MidiPlayer(null)
}