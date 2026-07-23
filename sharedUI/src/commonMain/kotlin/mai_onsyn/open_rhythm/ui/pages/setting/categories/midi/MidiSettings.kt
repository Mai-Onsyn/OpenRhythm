package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_audio_file
import mai_onsyn.open_rhythm.ui.icons.ic_graphic_eq
import mai_onsyn.open_rhythm.ui.icons.ic_settings_input_svideo
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun MidiSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MidiInputSettings()
            MidiOutputSettings()
            MidiFileSettings()
        }
    }
}

@Composable
private fun MidiInputSettings() {
    SettingsCard(
        title = "Input",
        icon = ic_settings_input_svideo,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item("Input device") {

        }

        fold("Events Settings") {
            itemWithSwitch(
                name = "Receive note events",
                description = "Enable key events input for notes",
                initial = Singleton.settings.EnableInputMidiNoteEvent,
                onToggled = { Singleton.settings.EnableInputMidiNoteEvent = it }
            )
            itemWithSwitch(
                name = "Receive PC events",
                description = "Event for controlling instrument changes",
                initial = Singleton.settings.EnableInputMidiPCEvent,
                onToggled = { Singleton.settings.EnableInputMidiPCEvent = it }
            )
            itemWithSwitch(
                name = "Receive CC events",
                description = "Performance control events, like pressing the pedal",
                initial = Singleton.settings.EnableInputMidiCCEvent,
                onToggled = { Singleton.settings.EnableInputMidiCCEvent = it }
            )
            itemWithSwitch(
                name = "Receive PB events",
                description = "Dynamically adjust pitch to achieve glissando, vibrato, and other effects",
                initial = Singleton.settings.EnableInputMidiPBEvent,
                onToggled = { Singleton.settings.EnableInputMidiPBEvent = it }
            )
        }
    }
}

@Composable
private fun MidiOutputSettings() {
    val colorScheme = MaterialTheme.colorScheme
    SettingsCard(
        title = "Output",
        icon = ic_graphic_eq,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item("Output device") {
            val coroutineScope = rememberCoroutineScope()
            val devices = remember {
                Singleton.midiAccess.outputs.toList()
            }
            val deviceNames = remember(devices) { devices.map { it.name ?: "Unknown Device" } }
            var selectedDeviceIndex by remember { mutableStateOf(deviceNames.indexOf(Singleton.settings.SelectedOutputDeviceName)) }
            val choices = remember(deviceNames, selectedDeviceIndex) {
                deviceNames.map {
                    ContextDropDownMenuItem(
                        label = it,
                        contentColor =  colorScheme.onSurface,
                        selectedContentColor = colorScheme.primary
                    )
                }
            }

            var dropDownMenuExpanded by remember { mutableStateOf(false) }
            ContextDropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false },
                selectedIndex = selectedDeviceIndex,
                onSelect = {
                    selectedDeviceIndex = it
                    Singleton.settings.SelectedOutputDeviceName = deviceNames.getOrElse(selectedDeviceIndex) { "Unknown Device" }
                    coroutineScope.launch {
                        try {
                            Singleton.player.deviceOutput = Singleton.midiAccess.openOutput(devices[it].id)
                            Logger.i { "Switched midi port to ${devices[it].id}" }
                        } catch (e: Exception) {
                            Logger.e(e) { "Cannot Open Output ${devices[it].id}" }
                        }
                    }
                },
                items = choices
            ) {
                OutlinedButton(
                    onClick = { dropDownMenuExpanded = true },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = deviceNames.getOrElse(selectedDeviceIndex) { "No Output" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        fold("Events Settings") {
            itemWithSwitch(
                name = "Send note events",
                description = "Enable key events input for notes",
                initial = Singleton.settings.EnableOutputMidiNoteEvent,
                onToggled = { Singleton.settings.EnableOutputMidiNoteEvent = it }
            )
            itemWithSwitch(
                name = "Send PC events",
                description = "Event for controlling instrument changes",
                initial = Singleton.settings.EnableOutputMidiPCEvent,
                onToggled = { Singleton.settings.EnableOutputMidiPCEvent = it }
            )
            itemWithSwitch(
                name = "Send CC events",
                description = "Performance control events, like pressing the pedal",
                initial = Singleton.settings.EnableOutputMidiCCEvent,
                onToggled = { Singleton.settings.EnableOutputMidiCCEvent = it }
            )
            itemWithSwitch(
                name = "Send PB events",
                description = "Dynamically adjust pitch to achieve glissando, vibrato, and other effects",
                initial = Singleton.settings.EnableOutputMidiPBEvent,
                onToggled = { Singleton.settings.EnableOutputMidiPBEvent = it }
            )
        }
    }
}

@Composable
private fun MidiFileSettings() {
    SettingsCard(
        title = "File",
        icon = ic_audio_file,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {

    }
}