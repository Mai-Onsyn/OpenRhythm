package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.UIMidiData
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.icons.ic_edit_square
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.*
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.Res
import mai_onsyn.open_rhythm.ui.utility.UiState
import openrhythm.sharedui.generated.resources.*

@Composable
fun FileManageRail(
    modifier: Modifier = Modifier,
    path: String,
    onFileCountAvailable: (Int) -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit,
    onEnterTrackEdit: (UIMidiData) -> Unit,
    refresher: Int
) {
    val uiState by produceState<UiState<List<UIMidiData>>>(
        initialValue = UiState.Loading,
        key1 = path,
        key2 = refresher
    ) {
        value = UiState.Loading
        value = try {
            val result = Global.fileLoader.loadFolder(path)
            Logger.i { "Folder loaded: $path, files=${result.size}" }
            ensureActive()
            onFileCountAvailable(result.size)
            UiState.Success(result)
        } catch (_: CancellationException) {
            value
        } catch (e: Exception) {
            Logger.e(e) { "Failed to load folder: $path" }
            UiState.Error(e.message ?: "Failed to load files")
        }
    }

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = modifier) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LoadingSpinner(Modifier.size(20.dp), strokeWidth = 4.dp)
                        Text(
                            text = str(Res.string.lib_loadingMidi, Global.fileLoader.loadedFileCount, Global.fileLoader.remandingFileCount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    val colorScheme = MaterialTheme.colorScheme
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .drawBehind {
                                drawRoundRect(
                                    cornerRadius = CornerRadius(size.height * 0.5f),
                                    size = size,
                                    color = colorScheme.surfaceContainerHighest
                                )
                                if (Global.fileLoader.remandingFileCount > 0) drawRoundRect(
                                    cornerRadius = CornerRadius(size.height * 0.5f),
                                    size = size.copy(width = size.width * Global.fileLoader.loadedFileCount / Global.fileLoader.remandingFileCount),
                                    color = colorScheme.primary
                                )
                            }
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier) {
                Text(
                    text = str(Res.string.lib_loadFailed, path, (uiState as UiState.Error).message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is UiState.Success -> {
            val midiFiles = (uiState as UiState.Success<List<UIMidiData>>).data
//            var isPlaying by remember { mutableStateOf(false) }
            var playingIdx by remember { mutableStateOf(-1) }
            LaunchedEffect(path) {
//                isPlaying = false
                playingIdx = -1
            }
            LaunchedEffect(playingIdx, path) {
                if (playingIdx == -1) {
                    if (Global.player.isPlaying) {
                        Global.player.stop()
                        Logger.d { "Stopped playback: idx=-1 (stop flag)" }
                    }
                    return@LaunchedEffect
                }
                try {
                    Global.fileLoader.loadFile(midiFiles[playingIdx].path).let {
                        Global.player.stop()
                        Global.player.setMidi(it, Global.settings.midiFileSettings[midiFiles[playingIdx].path])
                        Global.player.seek(it.startTick.toLong())
                        Global.player.play()
                        Logger.i { "Start playing: idx=$playingIdx, file=${midiFiles[playingIdx].fileName}" }
                        Global.player.onCompletion = { playingIdx = -1 }
                    }
                } catch (e: Exception) {
                    Logger.e(e) { "Error loading \"${midiFiles[playingIdx].fileName}\" from $path" }
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    if (playingIdx != -1) {
                        Global.player.stop()
                        Logger.d { "Stop playback: exit page" }
                    }
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
                        isPlaying = playingIdx == index,
                        onPlayButtonClick = {
                            playingIdx = if (it) index else -1
                        },
                        onConfirm = onConfirm,
                        onEnterTrackEdit = onEnterTrackEdit
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
    onConfirm: (MidiPlayMethod) -> Unit,
    onEnterTrackEdit: (UIMidiData) -> Unit
) {
    var showModeSelector by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            showModeSelector = true
            Logger.d { "Open mode selector for ${target.fileName}" }
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
//                    Text(
//                        text = "•",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "${target.noteCount} notes",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
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
//            val contextItems = remember { listOf(
//                ContextDropDownMenuItem(
//                    label = "Edit track",
//                    icon = ic_edit_square
//                )
//            ) }
//            var contextMenuExpanded by remember { mutableStateOf(false) }
//            ContextDropdownMenu(
//                expanded = contextMenuExpanded,
//                onDismissRequest = { contextMenuExpanded = false },
//                selectedIndex = -1,
//                onSelect = {
//                    contextMenuExpanded = false
//                    when (it) {
//                        0 -> onEnterTrackEdit(target)
//                    }
//                },
//                items = contextItems
//            ) {
//                IconButton(
//                    onClick = { contextMenuExpanded = true },
//                    shape = MaterialTheme.shapes.small,
//                    modifier = Modifier.size(24.dp, 32.dp)
//                ) {
//                    Icon(
//                        imageVector = ic_more_vert,
//                        contentDescription = "Operations for ${target.fileName}",
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
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