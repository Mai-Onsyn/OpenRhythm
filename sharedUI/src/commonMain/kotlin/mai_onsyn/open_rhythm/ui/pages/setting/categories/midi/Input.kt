package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.KtMidiInputDevice
import mai_onsyn.open_rhythm.ui.icons.ic_settings_input_svideo
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.toMappingMap
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*


@Composable
fun MidiInputSettings() {
    SettingsCard(
        title = str(Res.string.set_midiInputTitle),
        icon = ic_settings_input_svideo,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item(str(Res.string.set_midiInputDevice)) {
            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showDialog = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    str(Res.string.set_midiConfigure),
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
                        text = str(Res.string.set_midiInputDeviceTitle),
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
                            .border(0.25.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
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
                                    Logger.i { "Opened MIDI input device: Virtual Keyboard" }
                                } else {
                                    runBlocking { Global.midiInputDevices[virtualKeyboard]?.close() }
                                    Global.midiInputDevices.remove(virtualKeyboard)
                                    Global.settings.enabledMidiInputDeviceList.remove(virtualKeyboard)
                                    Logger.i { "Closed MIDI input device: Virtual Keyboard" }
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
                                        Logger.i { "Opened MIDI input device: $name" }
                                    } else {
                                        runBlocking { Global.midiInputDevices[id]?.close() }
                                        Global.midiInputDevices.remove(id)
                                        Global.settings.enabledMidiInputDeviceList.remove(name)
                                        Logger.i { "Closed MIDI input device: $name" }
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
                        PrimaryOperationButton(str(Res.string.set_midiClose), { showDialog = false })
                    }
                }
            }
        }

        fold(str(Res.string.set_midiEventsSettingsTitle)) {
            itemWithSwitch(
                name = str(Res.string.set_midiReceiveNoteEvents),
                description = str(Res.string.set_midiNoteEventsDescription),
                initial = Global.settings.EnableInputMidiNoteEvent,
                onToggled = { Global.settings.EnableInputMidiNoteEvent = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiReceiveCCEvents),
                description = str(Res.string.set_midiCCEventsDescription),
                initial = Global.settings.EnableInputMidiCCEvent,
                onToggled = { Global.settings.EnableInputMidiCCEvent = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiReceivePCEvents),
                description = str(Res.string.set_midiPCEventsDescription),
                initial = Global.settings.EnableInputMidiPCEvent,
                onToggled = { Global.settings.EnableInputMidiPCEvent = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiReceivePBEvents),
                description = str(Res.string.set_midiPBEventsDescription),
                initial = Global.settings.EnableInputMidiPBEvent,
                onToggled = { Global.settings.EnableInputMidiPBEvent = it }
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