package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.bridge.setupMidiOutput
import mai_onsyn.open_rhythm.ui.icons.ic_graphic_eq
import mai_onsyn.open_rhythm.ui.modules.CompactOutlinedTextField
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard


@Composable
fun MidiOutputSettings() {
    val colorScheme = MaterialTheme.colorScheme
    var gervillSelected by remember { mutableStateOf(Singleton.settings.SelectedOutputDeviceName == "Gervill") }
    SettingsCard(
        title = "Output",
        icon = ic_graphic_eq,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()

        item("Output device") {
            var devices by remember {
                mutableStateOf(Singleton.midiAccess.outputs.toList())
            }
            val deviceNames = remember(devices) { devices.map { it.name ?: UNKNOWN_DEVICE } }
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
            var showErrorDialog by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("No Message") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RefreshDeviceButton(
                    scope = coroutineScope,
                    onRefresh = { devices = Singleton.midiAccess.outputs.toList() }
                )
                ContextDropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = { dropDownMenuExpanded = false },
                    selectedIndex = selectedDeviceIndex,
                    onSelect = {
                        coroutineScope.launch {
                            try {
                                Singleton.player.midiOutput?.close()
                                Singleton.player.setOutput(Singleton.midiAccess.openOutput(devices[it].id).let { output ->
                                    if (devices[it].name == "Gervill") {
                                        setupMidiOutput(output, "Gervill", Singleton.settings.GervillSF2Path)
                                        gervillSelected = true
                                    } else gervillSelected = false
                                    output
                                })
                                selectedDeviceIndex = it
                                Singleton.settings.SelectedOutputDeviceName = deviceNames.getOrElse(selectedDeviceIndex) { UNKNOWN_DEVICE }
                                Logger.i { "Switched midi port to ${devices[it].id}" }
                            } catch (e: Exception) {
                                Logger.e(e) { "Cannot Open Output: ${devices[it].id}" }
                                showErrorDialog = true
                                errorMessage = "Cannot Open Output: ${devices[it].id}(${devices[it].name ?: UNKNOWN_DEVICE}) because: ${e::class.simpleName}: ${e.message}"
                            }
                        }
                    },
                    items = choices
                ) {
                    OutlinedButton(
                        onClick = { dropDownMenuExpanded = true },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .widthIn(max = 240.dp)
                    ) {
                        Text(
                            text = deviceNames.getOrElse(selectedDeviceIndex) { "No Output" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            ConfirmDialog(
                visible = showErrorDialog,
                onDismiss = { showErrorDialog = false },
                title = "Error",
                message = errorMessage,
                isDangerous = true,
                showCancel = false,
                onConfirm = { showErrorDialog = false },
            )
        }

        animatedItem(
            visible = gervillSelected,
            name = "SF2 path",
            description = "Set the SF2 file to replace the default Gervill soundfont"
        ) {
            fun reload() {
                coroutineScope.launch {
                    Singleton.player.midiOutput?.close()
                    val portDetails = Singleton.midiAccess.outputs.toList().firstOrNull { it.name == "Gervill" } ?: return@launch
                    Singleton.player.setOutput(Singleton.midiAccess.openOutput(portDetails.id).let { output ->
                        setupMidiOutput(output, "Gervill", Singleton.settings.GervillSF2Path)
                        output
                    })
                }
            }

            CompactOutlinedTextField(
                modifier = Modifier
                    .widthIn(max = 300.dp),
                value = Singleton.settings.GervillSF2Path,
                onValueChange = { Singleton.settings.GervillSF2Path = it },
                onConfirm = {
                    reload()
                }
            )
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
            itemWithSwitch(
                name = "Send other events",
                description = "Somewhat rare midi events",
                initial = Singleton.settings.EnableOutputOtherMidiEvent,
                onToggled = { Singleton.settings.EnableOutputOtherMidiEvent = it }
            )
        }
    }
}