package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.UIMidiData
import mai_onsyn.open_rhythm.ui.modules.LoadingSpinner
import mai_onsyn.open_rhythm.ui.utility.UiState
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.Res
import openrhythm.sharedui.generated.resources.lib_loadFailed
import openrhythm.sharedui.generated.resources.lib_loadingMidi

@Composable
fun FileManageRail(
    modifier: Modifier = Modifier,
    path: String,
    onFileCountAvailable: (Int) -> Unit,
    onConfirm: (MidiPlayMethod) -> Unit,
    onEnterTrackEdit: (UIMidiData) -> Unit,
    refresher: Int
) {
    var fileRailRefresher by remember { mutableStateOf(0) }
    val uiState by produceState<UiState<List<UIMidiData>>>(
        initialValue = UiState.Loading,
        key1 = path,
        key2 = refresher,
        fileRailRefresher
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
                        Global.player.setMidi(null)
                        Logger.d { "Stop playback: exit page" }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                itemsIndexed(midiFiles) { index, midiData ->
                    if (!midiData.loaded) FileRailItemBM(
                        modifier = Modifier.fillMaxSize(),
                        target = midiData,
                        onRefresh = { fileRailRefresher++ }
                    )
                    else FileRailItem(
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

data class MidiPlayMethod(
    val data: UIMidiData,
    val playMode: PlayMode,
    val trackNum: Int,
) {
    enum class PlayMode { AUTO, PRACTICE, PRACTICE_SINGLE }
}