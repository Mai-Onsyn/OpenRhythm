package mai_onsyn.open_rhythm.bridge

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.MidiPlayer
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.MidiInputDevice

object Singleton {
    val settings: UserSetting = UserSetting(createSetting())
    val midiAccess = getMidiAccess()
    val player: MidiPlayer = createMidiPlayer(midiAccess)
    val globalKeyEventDispatcher: GlobalKeyEventDispatcher = GlobalKeyEventDispatcher()

    val midiInputDevices: MutableMap<String, MidiInputDevice> = mutableMapOf()

    init {
        midiInputDevices["Virtual Keyboard"] = KeyboardVirtualMidiInputDevice(globalKeyEventDispatcher)
        registerGlobalKeyEventDispatcher(globalKeyEventDispatcher)
    }
}

private fun createMidiPlayer(access: MidiAccess): MidiPlayer {
    val outputs = access.outputs.toList()
    if (outputs.isEmpty()) {
        println("没有找到可用的 MIDI 输出设备。")
        return MidiPlayer(null)
    }

    outputs.firstOrNull { it.name?.contains("Microsoft") ?: false }?.let {
        Logger.i { "Use Midi device ${it.name}" }
        Singleton.settings.SelectedOutputDeviceName = it.name ?: "Unknown Device"
        return MidiPlayer(runBlocking { access.openOutput(it.id) })
    }

    outputs.firstOrNull()?.let {
        Logger.i { "Use Midi device ${it.name}" }
        Singleton.settings.SelectedOutputDeviceName = it.name ?: "Unknown Device"
        return MidiPlayer(runBlocking { access.openOutput(it.id) })
    }

    return MidiPlayer(null)
}