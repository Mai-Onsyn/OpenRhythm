package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.toHex
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.device.KeyboardVirtualMidiInputDevice
import mai_onsyn.open_rhythm.ui.icons.ic_add
import mai_onsyn.open_rhythm.ui.icons.ic_delete
import mai_onsyn.open_rhythm.ui.icons.ic_flowchart
import mai_onsyn.open_rhythm.ui.icons.ic_sort
import mai_onsyn.open_rhythm.ui.modules.ColorPickerDialog
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.modules.NumberSpinner
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.modules.getContrastTextColor
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.uuid.Uuid

@Composable
fun TrackAndChannel() {
    SettingsCard(
        title = "Track and channel",
        icon = ic_flowchart,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item("Track default colors") {
            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showDialog = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text(
                    text = "Configure",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            DialogPopup(
                visible = showDialog,
                onDismissRequest = { showDialog = false }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .heightIn(max = 1000.dp)
                        .padding(24.dp)
                ) {
                    DefaultTrackColorDialogContent()
                    Spacer(Modifier.height(8.dp))
                    PrimaryOperationButton("Close") { showDialog = false }
                }
            }
        }

        item("Trigger color", "The color that lights up on the virtual keyboard and rising notes when the mouse or MIDI device is pressed") {
            ColorSelector(
                initialColor = Global.settings.MidiInteractionColor,
                onColorSelected = { Global.settings.MidiInteractionColor = it }
            )
        }

        item("Trigger channel", "The channel for sending user-generated MIDI events") {
            NumberSpinner(
                value = Global.settings.MidiInteractionChannel,
                onValueChange = {
                    Global.settings.MidiInteractionChannel = it
                    Global.player.interactChannel = it
                    Global.midiInputDevices["Virtual Keyboard"]?.let { device ->
                        if (device is KeyboardVirtualMidiInputDevice) device.targetChannel = it
                    }
                },
                range = 0..15
            )
        }

        itemWithSwitch(
            name = "Hidden drum kit by default",
            description = "The track sent to channel 10 will be invisible by default",
            initial = Global.settings.DrumKitHiddenByDefault,
            onToggled = { Global.settings.DrumKitHiddenByDefault = it }
        )
    }
}

@Composable
private fun ColumnScope.DefaultTrackColorDialogContent() {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    data class UIColor(val color: Color, val uuid: Uuid = Uuid.random())
    val list = remember {
        mutableStateListOf<UIColor>().apply {
            Global.settings.trackColors.forEach { color ->
                add(UIColor(color))
            }
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list.apply {
            add(to.index, removeAt(from.index))
            Global.settings.trackColors.let {
                it.add(to.index, it.removeAt(from.index))
            }
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f, false),
        state = lazyListState
    ) {
        itemsIndexed(list, key = { _, item -> item.uuid }) { index, item ->
            ReorderableItem(reorderableLazyListState, key = item.uuid) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                TrackColorRow(
                    color = item.color,
                    order = index,
                    onChanged = {
                        list[index] = UIColor(it)
                        Global.settings.trackColors[index] = it
                    },
                    onDelete = {
                        list.removeAt(index)
                        Global.settings.trackColors.removeAt(index)
                    },
                    shadowElevation = elevation
                )
            }
        }
        item {
            var showAddPicker by remember { mutableStateOf(false) }
            Surface(
                onClick = { showAddPicker = true },
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.small
            ) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = ic_add,
                        contentDescription = "add",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            ColorPickerDialog(
                visible = showAddPicker,
                onDismissRequest = { showAddPicker = false },
                onConfirmRequest = {
                    list.add(UIColor(it))
                    Global.settings.trackColors.add(it)
                }
            )
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.TrackColorRow(
    color: Color,
    order: Int,
    onChanged: (Color) -> Unit,
    onDelete: () -> Unit,
    shadowElevation: Dp = 0.dp
) {
    var showEditDialog by remember { mutableStateOf(false) }
    Surface(
        onClick = { showEditDialog = true },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        shadowElevation = shadowElevation
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(8.dp)
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(40.dp)
            ) {
                Box(Modifier.draggableHandle().size(40.dp)) {
                    Icon(
                        imageVector = ic_sort,
                        contentDescription = "sort",
                        modifier = Modifier
                            .fillMaxSize(0.5f)
                            .align(Alignment.Center)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Track ${order + 1}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        onDrawBehind {
                            withTransform({
                                scale(
                                    scaleX = w / h,
                                    scaleY = 1f,
                                    pivot = Offset(w / 2f, h / 2f)
                                )
                            }) {
                                val brush = Brush.radialGradient(
                                    colorStops = arrayOf(0f to color, 0.5f to color, 1f to Color.Transparent),
                                    center = Offset(w / 2f, h / 2f),
                                    radius = h / 2f
                                )
                                drawRect(brush = brush, topLeft = Offset.Zero, size = size)
                            }
                        }
                    }
            ) {
                Text(
                    text = color.toHex(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = getContrastTextColor(color),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = ic_delete,
                    contentDescription = "delete",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    ColorPickerDialog(
        visible = showEditDialog,
        onDismissRequest = { showEditDialog = false },
        initialColor = color,
        onConfirmRequest = onChanged
    )
}