package mai_onsyn.open_rhythm.bridge

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiAccess
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.MidiPlayer
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.KtMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.MidiInputDevice
import kotlin.sequences.firstOrNull

object Singleton {
    val settings: UserSetting = UserSetting(createSetting())
    var midiAccess = getMidiAccess()
    val player: MidiPlayer = createMidiPlayer(midiAccess)
    val globalKeyEventDispatcher: GlobalKeyEventDispatcher = GlobalKeyEventDispatcher()

    val midiInputDevices: MutableMap<String, MidiInputDevice> = mutableMapOf()

    init {
        registerGlobalKeyEventDispatcher(globalKeyEventDispatcher)
        val portList = midiAccess.inputs.toList()
        settings.enabledMidiInputDeviceList.toSet().let {
            settings.enabledMidiInputDeviceList.clear()
            settings.enabledMidiInputDeviceList.addAll(it)
        }
        settings.enabledMidiInputDeviceList.forEach { name ->
            if (name == "Virtual Keyboard") midiInputDevices["Virtual Keyboard"] = KeyboardVirtualMidiInputDevice(globalKeyEventDispatcher)
            else try {
                portList.find { it.name == name }?.let {
                    midiInputDevices[it.id] = KtMidiInputDevice(it, midiAccess)
                }
            } catch (e: Exception) {
                Logger.w { "No output device found for $name" }
                settings.enabledMidiInputDeviceList.remove(name)
            }
        }
        midiInputDevices.keys.forEach {
            Logger.d { it }
        }
    }

    fun refreshMidiAccess() {
        midiAccess = getMidiAccess()
    }
}

private fun createMidiPlayer(access: MidiAccess): MidiPlayer {
    val outputs = access.outputs.toList()
    if (outputs.isEmpty()) {
        println("没有找到可用的 MIDI 输出设备。")
        return MidiPlayer(null)
    }

    val candidates = mutableListOf<String>()
    val userSpecified = Singleton.settings.SelectedOutputDeviceName
    if (userSpecified.isNotEmpty()) {
        candidates.add(userSpecified)
    }
    candidates.add("Microsoft MIDI Mapper")
    candidates.add("loopMIDI Port")
    candidates.add("FluidSynth MIDI")

    val selected = candidates.firstNotNullOfOrNull { candidate ->
        outputs.firstOrNull { it.name == candidate }
    } ?: outputs.first()


    Singleton.settings.SelectedOutputDeviceName = selected.name ?: "Unknown Device"
    return MidiPlayer(runBlocking {
        try {
            access.openOutput(selected.id)
        } catch (e: Exception) {
            Logger.e(e) { "Cannot Open Output: ${selected.id}" }
            null
        }
    })
}