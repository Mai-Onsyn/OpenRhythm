package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.KtMidiInputDevice
import mai_onsyn.open_rhythm.ui.icons.ic_settings_input_svideo
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.toMappingMap
import kotlin.collections.contains
import kotlin.collections.set


@Composable
fun MidiInputSettings() {
    SettingsCard(
        title = "Input",
        icon = ic_settings_input_svideo,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item("Input device") {
            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showDialog = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    "Configure",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            DialogPopup(
                visible = showDialog,
                onDismissRequest = { showDialog = false }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 580.dp)
                ) {
                    Text(
                        text = "MIDI Input Device",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    var inputs by remember { mutableStateOf(Global.midiAccess.inputs.toList()) }
                    Column(
                        modifier = Modifier
                            .heightIn(max = 600.dp)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(rememberScrollState())
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    ) {
                        val virtualKeyboard = "Virtual Keyboard"
                        DeviceRow(
                            id = "OpenRhythm",
                            name = virtualKeyboard,
                            initial = Global.midiInputDevices.contains(virtualKeyboard),
                            onChecked = {
                                if (it) {
                                    Global.midiInputDevices[virtualKeyboard] = KeyboardVirtualMidiInputDevice(
                                        Global.globalKeyEventDispatcher,
                                        Global.settings.userKeyMappings.toMappingMap()
                                    )
                                    Global.settings.enabledMidiInputDeviceList.add(virtualKeyboard)
                                } else {
                                    runBlocking { Global.midiInputDevices[virtualKeyboard]?.close() }
                                    Global.midiInputDevices.remove(virtualKeyboard)
                                    Global.settings.enabledMidiInputDeviceList.remove(virtualKeyboard)
                                }
                            }
                        )

                        inputs.forEach { input ->
                            val name = input.name ?: UNKNOWN_DEVICE
                            val id = input.id

                            DeviceRow(
                                id = id,
                                name = name,
                                initial = Global.midiInputDevices.contains(id),
                                onChecked = {
                                    if (it) {
                                        Global.midiInputDevices[id] = KtMidiInputDevice(input, Global.midiAccess)
                                        Global.settings.enabledMidiInputDeviceList.add(name)
                                    } else {
                                        runBlocking { Global.midiInputDevices[id]?.close() }
                                        Global.midiInputDevices.remove(id)
                                        Global.settings.enabledMidiInputDeviceList.remove(name)
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        RefreshDeviceButton(
                            scope = coroutineScope,
                            onRefresh = { inputs = Global.midiAccess.inputs.toList() }
                        )
                        PrimaryOperationButton("Close", { showDialog = false })
                    }
                }
            }
        }

        fold("Events Settings") {
            itemWithSwitch(
                name = "Receive note events",
                description = "Enable key events input for notes",
                initial = Global.settings.EnableInputMidiNoteEvent,
                onToggled = { Global.settings.EnableInputMidiNoteEvent = it }
            )
            itemWithSwitch(
                name = "Receive PC events",
                description = "Event for controlling instrument changes",
                initial = Global.settings.EnableInputMidiPCEvent,
                onToggled = { Global.settings.EnableInputMidiPCEvent = it }
            )
            itemWithSwitch(
                name = "Receive CC events",
                description = "Performance control events, like pressing the pedal",
                initial = Global.settings.EnableInputMidiCCEvent,
                onToggled = { Global.settings.EnableInputMidiCCEvent = it }
            )
            itemWithSwitch(
                name = "Receive PB events",
                description = "Dynamically adjust pitch to achieve glissando, vibrato, and other effects",
                initial = Global.settings.EnableInputMidiPBEvent,
                onToggled = { Global.settings.EnableInputMidiPBEvent = it }
            )
            itemWithSwitch(
                name = "Receive other events",
                description = "Somewhat rare midi events",
                initial = Global.settings.EnableInputOtherMidiEvent,
                onToggled = { Global.settings.EnableInputOtherMidiEvent = it }
            )
        }
    }
}

@Composable
private fun DeviceRow(
    id: String,
    name: String,
    initial: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Surface(
        onClick = {},
        color = Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text(
                    text = "$id: ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            var checked by remember { mutableStateOf(initial) }
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onChecked(it)
                }
            )
        }
    }
}