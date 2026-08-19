package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.ui.modules.midi_flow.AppDefaultMidiKeyboard
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

@Composable
fun KeyMappingSettings() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(
                text = "Tip: ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Click a keyboard key with the mouse to select it, click again to deselect it; while selected, click a MIDI keyboard key to bind it to the current keyboard key",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val userActiveMidiKeys = remember { mutableStateMapOf<Int, Color>() }
        var selectedKeyCode by remember { mutableStateOf<Long?>(null) }
        Surface(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
                .aspectRatio(Global.settings.KeyboardAspectRatio),
            shadowElevation = 4.dp
        ) {
            val appendTexts = mutableMapOf<Int, String>().apply {
                for (mapping in Global.settings.userKeyMappings) {
                    put(mapping.pitch, keyCodeToShortName[mapping.keyCode] ?: "Non")
                }
            }
            AppDefaultMidiKeyboard(
                modifier = Modifier.fillMaxSize(),
                userActiveKey = userActiveMidiKeys,
                appendTexts = appendTexts,
                onPress = { pitch, velocity ->
                    userActiveMidiKeys[pitch] = Global.settings.MidiInteractionColor
                    Global.player.noteOn(pitch, velocity)

                    selectedKeyCode?.let { code ->
//                        Singleton.settings.userKeyMappings.removeAll { it.keyCode == code }
                        Global.settings.userKeyMappings.let { list ->
                            val toRemove = list.filter { it.keyCode == code || it.pitch == pitch }
                            toRemove.forEach { list.remove(it) }
                        }

                        Global.settings.userKeyMappings.add(KeyMidiMapping(code, pitch))
                        (Global.midiInputDevices["Virtual Keyboard"] as? KeyboardVirtualMidiInputDevice)?.updateMappings(
                            Global.settings.userKeyMappings.toMappingMap()
                        )
                    }
                },
                onRelease = {
                    userActiveMidiKeys.remove(it)
                    Global.player.noteOff(it)
                }
            )

            BindInputDeviceEvents(userActiveMidiKeys)
        }

        Spacer(Modifier.heightIn(16.dp, 32.dp))

        val activeKeys = remember { mutableStateMapOf<Long, Color>() }
        VisualKeyboard(
            modifier = Modifier
                .fillMaxWidth(),
            eventDispatcher = Global.globalKeyEventDispatcher,
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