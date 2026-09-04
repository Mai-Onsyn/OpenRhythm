package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

private data class OpenOutputFailure(
    val portId: String,
    val portName: String,
    val exceptionType: String,
    val exceptionMessage: String,
)


@Composable
fun MidiOutputSettings() {
    val colorScheme = MaterialTheme.colorScheme
    var gervillSelected by remember { mutableStateOf(Global.settings.SelectedOutputDeviceName == "Gervill") }
    SettingsCard(
        title = str(Res.string.set_midiOutputTitle),
        icon = ic_graphic_eq,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        val coroutineScope = rememberCoroutineScope()

        item(str(Res.string.set_midiOutputDevice)) {
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
            var openOutputFailure by remember { mutableStateOf<OpenOutputFailure?>(null) }
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
                                Logger.i { "Switched midi output port to ${devices[it].id}: ${devices[it].name}" }
                            } catch (e: Exception) {
                                Logger.e(e) { "Cannot open output port ${devices[it].id}: ${devices[it].name}" }
                                showErrorDialog = true
                                openOutputFailure = OpenOutputFailure(
                                    devices[it].id,
                                    devices[it].name ?: UNKNOWN_DEVICE,
                                    e::class.simpleName ?: "null",
                                    e.message ?: "null"
                                )
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
                            .widthIn(max = 240.dp),
                        border = BorderStroke(0.25.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = deviceNames.getOrElse(selectedDeviceIndex) { "No Output" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            val outputErrorMessage = openOutputFailure?.let { f ->
                str(Res.string.set_midiCannotOpenOutput, f.portId, f.portName, f.exceptionType, f.exceptionMessage)
            } ?: ""
            ConfirmDialog(
                visible = showErrorDialog,
                onDismissRequest = { showErrorDialog = false },
                title = str(Res.string.set_midiErrorTitle),
                message = outputErrorMessage,
                isDangerous = true,
                showCancel = false,
                onConfirm = { showErrorDialog = false },
            )
        }

        animatedItem(
            visible = gervillSelected,
            name = str(Res.string.set_midiSF2Path),
            description = str(Res.string.set_midiSF2PathDescription)
        ) {
            CompactOutlinedTextField(
                modifier = Modifier
                    .widthIn(max = 300.dp),
                value = Global.settings.GervillSF2Path,
                onValueChange = { Global.settings.GervillSF2Path = it },
                onConfirm = {
                    coroutineScope.launch {
                        Global.player.midiOutput?.close()
                        val portDetails = Global.midiAccess.outputs.toList().firstOrNull { it.name == "Gervill" } ?: return@launch
                        Global.player.setOutput(Global.midiAccess.openOutput(portDetails.id).let { output ->
                            setupMidiOutput(output, "Gervill", Global.settings.GervillSF2Path)
                            output
                        })
                        Logger.d { "Reloaded Gervill sf2 from ${Global.settings.GervillSF2Path}" }
                    }
                }
            )
        }

        fold(str(Res.string.set_midiEventsSettingsTitle)) {
            itemWithSwitch(
                name = str(Res.string.set_midiSendNoteEvents),
                description = str(Res.string.set_midiNoteEventsDescription),
                initial = Global.settings.EnableOutputMidiNoteEvent,
                onToggled = {
                    Global.settings.EnableOutputMidiNoteEvent = it
                    Global.player.enableNote = it
                }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiSendCCEvents),
                description = str(Res.string.set_midiCCEventsDescription),
                initial = Global.settings.EnableOutputMidiCCEvent,
                onToggled = {
                    Global.settings.EnableOutputMidiCCEvent = it
                    Global.player.enableCC = it
                }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiSendPCEvents),
                description = str(Res.string.set_midiPCEventsDescription),
                initial = Global.settings.EnableOutputMidiPCEvent,
                onToggled = {
                    Global.settings.EnableOutputMidiPCEvent = it
                    Global.player.enablePC
                }
            )
            itemWithSwitch(
                name = str(Res.string.set_midiSendPBEvents),
                description = str(Res.string.set_midiPBEventsDescription),
                initial = Global.settings.EnableOutputMidiPBEvent,
                onToggled = {
                    Global.settings.EnableOutputMidiPBEvent = it
                    Global.player.enablePB = it
                }
            )
        }
    }
}