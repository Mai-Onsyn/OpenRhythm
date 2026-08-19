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
import mai_onsyn.open_rhythm.bridge.Global
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
    var gervillSelected by remember { mutableStateOf(Global.settings.SelectedOutputDeviceName == "Gervill") }
    SettingsCard(
        title = "Output",
        icon = ic_graphic_eq,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()

        item("Output device") {
            var devices by remember {
                mutableStateOf(Global.midiAccess.outputs.toList())
            }
            val deviceNames = remember(devices) { devices.map { it.name ?: UNKNOWN_DEVICE } }
            var selectedDeviceIndex by remember { mutableStateOf(deviceNames.indexOf(Global.settings.SelectedOutputDeviceName)) }
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
                    onRefresh = { devices = Global.midiAccess.outputs.toList() }
                )
                ContextDropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = { dropDownMenuExpanded = false },
                    selectedIndex = selectedDeviceIndex,
                    onSelect = {
                        coroutineScope.launch {
                            try {
                                Global.player.midiOutput?.close()
                                Global.player.setOutput(Global.midiAccess.openOutput(devices[it].id).let { output ->
                                    if (devices[it].name == "Gervill") {
                                        setupMidiOutput(output, "Gervill", Global.settings.GervillSF2Path)
                                        gervillSelected = true
                                    } else gervillSelected = false
                                    output
                                })
                                selectedDeviceIndex = it
                                Global.settings.SelectedOutputDeviceName = deviceNames.getOrElse(selectedDeviceIndex) { UNKNOWN_DEVICE }
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
                onDismissRequest = { showErrorDialog = false },
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
                    Global.player.midiOutput?.close()
                    val portDetails = Global.midiAccess.outputs.toList().firstOrNull { it.name == "Gervill" } ?: return@launch
                    Global.player.setOutput(Global.midiAccess.openOutput(portDetails.id).let { output ->
                        setupMidiOutput(output, "Gervill", Global.settings.GervillSF2Path)
                        output
                    })
                }
            }

            CompactOutlinedTextField(
                modifier = Modifier
                    .widthIn(max = 300.dp),
                value = Global.settings.GervillSF2Path,
                onValueChange = { Global.settings.GervillSF2Path = it },
                onConfirm = {
                    reload()
                }
            )
        }

        fold("Events Settings") {
            itemWithSwitch(
                name = "Send note events",
                description = "Enable key events input for notes",
                initial = Global.settings.EnableOutputMidiNoteEvent,
                onToggled = { Global.settings.EnableOutputMidiNoteEvent = it }
            )
            itemWithSwitch(
                name = "Send PC events",
                description = "Event for controlling instrument changes",
                initial = Global.settings.EnableOutputMidiPCEvent,
                onToggled = { Global.settings.EnableOutputMidiPCEvent = it }
            )
            itemWithSwitch(
                name = "Send CC events",
                description = "Performance control events, like pressing the pedal",
                initial = Global.settings.EnableOutputMidiCCEvent,
                onToggled = { Global.settings.EnableOutputMidiCCEvent = it }
            )
            itemWithSwitch(
                name = "Send PB events",
                description = "Dynamically adjust pitch to achieve glissando, vibrato, and other effects",
                initial = Global.settings.EnableOutputMidiPBEvent,
                onToggled = { Global.settings.EnableOutputMidiPBEvent = it }
            )
            itemWithSwitch(
                name = "Send other events",
                description = "Somewhat rare midi events",
                initial = Global.settings.EnableOutputOtherMidiEvent,
                onToggled = { Global.settings.EnableOutputOtherMidiEvent = it }
            )
        }
    }
}