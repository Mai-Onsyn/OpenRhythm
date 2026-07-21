package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.MorphingPlayPauseButton
import mai_onsyn.open_rhythm.ui.modules.NumberSpinner
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.utility.UiState

@Composable
fun FileManageRail(
    modifier: Modifier = Modifier,
    path: String,
    onFileCountAvailable: (Int) -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit
) {
    val uiState by produceState<UiState<List<UIMidiData>>>(
        initialValue = UiState.Loading,
        key1 = path
    ) {
        value = try {
            val result = getFileInfosInFolder(path)
            onFileCountAvailable(result.size)
            UiState.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            UiState.Error(e.message ?: "加载失败")
        }
    }

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = modifier) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier) {
                Text(
                    text = "Failed to load midi from $path because ${(uiState as UiState.Error).message}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is UiState.Success -> {
            val midiFiles = (uiState as UiState.Success<List<UIMidiData>>).data
//            Box(modifier = modifier) {
//                Text(
//                    text = "${midiFiles.size}",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
            var isPlaying by remember { mutableStateOf(false) }
            var playingIdx by remember { mutableStateOf(-1) }
            LaunchedEffect(path) {
                isPlaying = false
                playingIdx = -1
            }
            LaunchedEffect(isPlaying, playingIdx, path) {
                if (playingIdx == -1) {
                    Singleton.player.stop()
                    return@LaunchedEffect
                }

                if (isPlaying) {
                    loadMidiFile(midiFiles[playingIdx].path)?.let {
                        Singleton.player.stop()
                        Singleton.player.setMidi(it)
                        Singleton.player.play()
                        Singleton.player.onCompleted = { isPlaying = false }
                    }
                } else {
                    Singleton.player.stop()
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    Singleton.player.stop()
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(midiFiles) { index, midiData ->
                    FileRailItem(
                        modifier = Modifier.fillMaxWidth(),
                        target = midiData,
                        isPlaying = isPlaying && playingIdx == index,
                        onPlayButtonClick = {
                            if (it) {
                                isPlaying = true
                                playingIdx = index
                            } else {
                                isPlaying = false
                            }
                            println(it.toString())
                        },
                        onConfirm = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
fun FileRailItem(
    modifier: Modifier = Modifier,
    target: UIMidiData,
    isPlaying: Boolean = false,
    onPlayButtonClick: (Boolean) -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit
) {
    var showModeSelector by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.medium,
        onClick = { showModeSelector = true }
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
                        text = if (target.pianoOnly) "Piano Only" else "Multiple Instruments",
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
//            OpacitySurface(
//                modifier = Modifier.pointerInput(Unit) {
//                    detectTapGestures(
//                        onTap = { onPlayButtonClick(!isPlayingState) }
//                    )
//                }
//            ) {
//                MorphingPlayPauseButton(
//                    modifier = Modifier.size(24.dp),
//                    isPlaying = isPlaying,
//                    fill = MaterialTheme.colorScheme.primary
//                )
//            }
            OpacitySurface(
                contentPadding = 0.dp
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { onPlayButtonClick(!isPlayingState) }
                ) {
                    MorphingPlayPauseButton(
                        modifier = Modifier.size(24.dp),
                        isPlaying = isPlaying,
                        fill = MaterialTheme.colorScheme.primary
                    )
                }
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

data class MidiPlayMethod(
    val data: UIMidiData,
    val playMode: PlayMode,
    val trackNum: Int,
) {
    enum class PlayMode { AUTO, PRACTICE, PRACTICE_SINGLE }
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
                "Auto Play", "Full Exercise", "Single Track Exercise"
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
                0 -> "Appreciation mode, automatically played by Open Rhythm"
                1 -> "The next note only plays after you press the current one"
                2 -> "Like full exercise, but only practice one track in the MIDI"
                else -> "No descriptions"
            },
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var targetTrack by remember { mutableStateOf(0) }
            if (selectedMode == 2) {
                NumberSpinner(
                    value = targetTrack,
                    onValueChange = { targetTrack = it },
                    range = 0..<data.trackCount,
                    label = "Track"
                )
            }
            PrimaryOperationButton("Confirm") {
                when (selectedMode) {
                    0 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.AUTO, -1))
                    1 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.PRACTICE, -1))
                    2 -> onConfirm(MidiPlayMethod(data, MidiPlayMethod.PlayMode.PRACTICE_SINGLE, targetTrack))
                }
                onCancel()
            }
            PrimaryOperationButton("Cancel", onCancel)
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