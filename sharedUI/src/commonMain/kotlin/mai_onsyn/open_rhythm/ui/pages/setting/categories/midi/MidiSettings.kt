package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.core.midi.device.KtMidiInputDevice
import mai_onsyn.open_rhythm.ui.icons.ic_audio_file
import mai_onsyn.open_rhythm.ui.icons.ic_graphic_eq
import mai_onsyn.open_rhythm.ui.icons.ic_refresh
import mai_onsyn.open_rhythm.ui.icons.ic_settings_input_svideo
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiKeyBoard
import mai_onsyn.open_rhythm.ui.pages.library.cachedMidiFiles
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.toMappingMap
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

private const val UNKNOWN_DEVICE = "Unknown Device"

@Composable
fun MidiSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MidiInputSettings()
            MidiOutputSettings()
            MidiFileSettings()

            TestKeyboard()
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

                    var inputs by remember { mutableStateOf(Singleton.midiAccess.inputs.toList()) }
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
                            initial = Singleton.midiInputDevices.contains(virtualKeyboard),
                            onChecked = {
                                if (it) {
                                    Singleton.midiInputDevices[virtualKeyboard] = KeyboardVirtualMidiInputDevice(
                                        Singleton.globalKeyEventDispatcher,
                                        Singleton.settings.userKeyMappings.toMappingMap()
                                    )
                                    Singleton.settings.enabledMidiInputDeviceList.add(virtualKeyboard)
                                } else {
                                    runBlocking { Singleton.midiInputDevices[virtualKeyboard]?.close() }
                                    Singleton.midiInputDevices.remove(virtualKeyboard)
                                    Singleton.settings.enabledMidiInputDeviceList.remove(virtualKeyboard)
                                }
                            }
                        )

                        inputs.forEach { input ->
                            val name = input.name ?: UNKNOWN_DEVICE
                            val id = input.id

                            DeviceRow(
                                id = id,
                                name = name,
                                initial = Singleton.midiInputDevices.contains(id),
                                onChecked = {
                                    if (it) {
                                        Singleton.midiInputDevices[id] = KtMidiInputDevice(input, Singleton.midiAccess)
                                        Singleton.settings.enabledMidiInputDeviceList.add(name)
                                    } else {
                                        runBlocking { Singleton.midiInputDevices[id]?.close() }
                                        Singleton.midiInputDevices.remove(id)
                                        Singleton.settings.enabledMidiInputDeviceList.remove(name)
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
                            onRefresh = { inputs = Singleton.midiAccess.inputs.toList() }
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
            itemWithSwitch(
                name = "Receive other events",
                description = "Somewhat rare midi events",
                initial = Singleton.settings.EnableInputOtherMidiEvent,
                onToggled = { Singleton.settings.EnableInputOtherMidiEvent = it }
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

const val VIRTUAL_DEVICE_NAME = "OpenRhythm Virtual Device"
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
                                Singleton.player.setOutput(Singleton.midiAccess.openOutput(devices[it].id))
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

@Composable
fun RefreshDeviceButton(
    scope: CoroutineScope,
    onRefresh: () -> Unit,
) {
    var iconRotation by remember { mutableStateOf(0f) }
    val iconRotationValue by animateFloatAsState(iconRotation, tween(durationMillis = 1000))
    IconButton(
        onClick = {
            iconRotation += 360
            scope.launch {
                Singleton.refreshMidiAccess()
                onRefresh()
            }
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = ic_refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .rotate(iconRotationValue)
        )
    }
}

@Composable
private fun MidiFileSettings() {
    SettingsCard(
        title = "File",
        icon = ic_audio_file,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = "Don't parse midi",
            description = "NOT RECOMMENDED: Only you want the original track",
            initial = Singleton.settings.UseParserV1,
            onToggled = {
                Singleton.settings.UseParserV1 = it
                cachedMidiFiles.clear()
            }
        )
    }
}

@Composable
private fun TestKeyboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(Singleton.settings.KeyboardAspectRatio)
    ) {
        val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }
        MidiKeyBoard(
            modifier = Modifier
                .fillMaxSize(),
            minPitch = 21,
            maxPitch = 108,
            userActiveKey = userActiveKeys,
            onPress = { pitch, velocity ->
                userActiveKeys[pitch] = Singleton.settings.KeyboardUserInteractionDisplayColor
                Singleton.player.noteOn(pitch, velocity)
            },
            onRelease = {
                userActiveKeys.remove(it)
                Singleton.player.noteOff(it)
            }
        )

        BindInputDeviceEvents(userActiveKeys)
    }
}