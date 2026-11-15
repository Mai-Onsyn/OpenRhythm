package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.core.midi.UIMidiData
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.icons.ic_edit_square
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.MorphingPlayPauseButton
import mai_onsyn.open_rhythm.ui.modules.NumberSpinner
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*


@Composable
fun FileRailItem(
    modifier: Modifier = Modifier,
    target: UIMidiData,
    isPlaying: Boolean = false,
    onPlayButtonClick: (Boolean) -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit,
    onEnterTrackEdit: (UIMidiData) -> Unit
) {
    var showModeSelector by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            showModeSelector = true
            Logger.d { "Open mode selector for: ${target.fileName}" }
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpacitySurface(
                contentPadding = 8.dp,
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = ic_music_note,
                    contentDescription = target.path,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = target.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${target.trackCount} track${if (target.trackCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (target.pianoOnly) str(Res.string.lib_modePiano) else str(Res.string.lib_modeEnsemble),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = Time.formatMillisToTime(target.duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isPlayingState by rememberUpdatedState(isPlaying)
            OpacitySurface(
                contentPadding = 0.dp
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onPlayButtonClick(!isPlayingState) }
                ) {
                    MorphingPlayPauseButton(
                        modifier = Modifier.size(24.dp),
                        isPlaying = isPlayingState,
                        fill = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = { onEnterTrackEdit(target) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = ic_edit_square,
                    contentDescription = "Operations for ${target.fileName}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    DialogPopup(
        visible = showModeSelector,
        onDismissRequest = { showModeSelector = false }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            DialogContent(
                data = target,
                onCancel = { showModeSelector = false },
                onConfirm = onConfirm
            )
        }
    }
}


@Composable
private fun DialogContent(
    data: UIMidiData,
    onCancel: () -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = data.fileName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        var selectedMode by remember { mutableStateOf(0) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                str(Res.string.lib_playModeAuto), str(Res.string.lib_playModeFullExercise), str(Res.string.lib_playModeSingleExercise)
            ).forEachIndexed { index, title ->
                PlayModeCard(
                    selected = selectedMode == index,
                    onSelect = { selectedMode = index },
                    title = title
                )
            }
        }

        Text(
            text = when (selectedMode) {
                0 -> str(Res.string.lib_playModeAutoDesc)
                1 -> str(Res.string.lib_playModeFullExerciseDesc)
                2 -> str(Res.string.lib_playModeSingleExerciseDesc)
                else -> "No descriptions"
            },
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var targetTrack by remember { mutableStateOf(-1) }
            if (selectedMode == 2) {
                NumberSpinner(
                    value = targetTrack,
                    onValueChange = { targetTrack = it },
                    range = 0..<data.trackCount,
                    label = str(Res.string.lib_trackLabel)
                )
            }
            PrimaryOperationButton(str(Res.string.common_confirm)) {
                when (selectedMode) {
                    0 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.AUTO, -1))
                    1 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.PRACTICE, -1))
                    2 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.PRACTICE_SINGLE, targetTrack))
                }
                Logger.i { "Confirm play: mode=${selectedMode}, track=$targetTrack, filename=${data.fileName}" }
                onCancel()
            }
            PrimaryOperationButton(str(Res.string.common_cancel), onCancel)
        }
    }
}

@Composable
private fun PlayModeCard(
    selected: Boolean,
    onSelect: () -> Unit,
    title: String
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .size(120.dp, 80.dp)
            .pointerHoverIcon(PointerIcon.Hand),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        border = if (!selected) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}