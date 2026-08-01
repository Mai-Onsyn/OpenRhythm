package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiKeyBoard
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

@Composable
fun KeyMappingSettings() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val userActiveMidiKeys = remember { mutableStateMapOf<Int, Color>() }
        var selectedKeyCode by remember { mutableStateOf<Long?>(null) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(Singleton.settings.KeyboardAspectRatio)
        ) {
            val appendTexts = mutableMapOf<Int, String>().apply {
                for (mapping in Singleton.settings.userKeyMappings) {
                    put(mapping.pitch, keyCodeToShortName[mapping.keyCode] ?: "Non")
                }
            }
            MidiKeyBoard(
                modifier = Modifier.fillMaxSize(),
                userActiveKey = userActiveMidiKeys,
                appendTexts = appendTexts,
                onPress = { pitch, velocity ->
                    userActiveMidiKeys[pitch] = Singleton.settings.KeyboardUserInteractionDisplayColor
                    Singleton.player.noteOn(pitch, velocity)

                    selectedKeyCode?.let { code ->
//                        Singleton.settings.userKeyMappings.removeAll { it.keyCode == code }
                        Singleton.settings.userKeyMappings.let { list ->
                            val toRemove = list.filter { it.keyCode == code || it.pitch == pitch }
                            toRemove.forEach { list.remove(it) }
                        }

                        Singleton.settings.userKeyMappings.add(KeyMidiMapping(code, pitch))
                        (Singleton.midiInputDevices["Virtual Keyboard"] as? KeyboardVirtualMidiInputDevice)?.updateMappings(
                            Singleton.settings.userKeyMappings.toMappingMap()
                        )
                    }
                },
                onRelease = {
                    userActiveMidiKeys.remove(it)
                    Singleton.player.noteOff(it)
                }
            )

            BindInputDeviceEvents(userActiveMidiKeys)
        }

        Spacer(Modifier.heightIn(16.dp, 32.dp))

        val activeKeys = remember { mutableStateMapOf<Long, Color>() }
        VisualKeyboard(
            modifier = Modifier
                .fillMaxWidth(),
            eventDispatcher = Singleton.globalKeyEventDispatcher,
            activeKeys = activeKeys,
            selectedKey = selectedKeyCode,
            onSelectChanged = {
                selectedKeyCode = if (selectedKeyCode == it) null
                else it
            },
            drawControl = true,
            drawNumpad = true
        )
    }
}