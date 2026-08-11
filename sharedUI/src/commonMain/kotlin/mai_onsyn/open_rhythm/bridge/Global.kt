package mai_onsyn.open_rhythm.bridge

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiAccess
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.MidiPlayer2
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.KtMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.MidiInputDevice
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.toMappingMap

object Global {
    val settings: UserSetting = UserSetting(createSetting())
    var midiAccess = getMidiAccess()
    val player: MidiPlayer2 = createMidiPlayer(midiAccess)
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
            if (name == "Virtual Keyboard") midiInputDevices["Virtual Keyboard"] = KeyboardVirtualMidiInputDevice(
                globalKeyEventDispatcher,
                settings.userKeyMappings.toMappingMap()
            )
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

private fun createMidiPlayer(access: MidiAccess): MidiPlayer2 {
    val outputs = access.outputs.toList()
    if (outputs.isEmpty()) {
        println("没有找到可用的 MIDI 输出设备。")
        return MidiPlayer2(null)
    }

    val userSpecified = Global.settings.SelectedOutputDeviceName
    val candidates = mutableListOf<String>()
    if (userSpecified.isNotEmpty()) {
        candidates.add(userSpecified)
    }
    candidates.add("Microsoft MIDI Mapper")
    candidates.add("loopMIDI Port")
    candidates.add("FluidSynth MIDI")

    val selected = candidates.firstNotNullOfOrNull { candidate ->
        outputs.firstOrNull { it.name == candidate }
    } ?: outputs.first()


    Global.settings.SelectedOutputDeviceName = selected.name ?: "Unknown Device"
    return MidiPlayer2(runBlocking {
        try {
            val output = access.openOutput(selected.id)
            if (selected.name == "Gervill") {
                setupMidiOutput(output, "Gervill", Global.settings.GervillSF2Path)
            }
            output
        } catch (e: Exception) {
            Logger.e(e) { "Cannot Open Output: ${selected.id}" }
            null
        }
    })
}