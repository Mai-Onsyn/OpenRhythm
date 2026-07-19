package mai_onsyn.open_rhythm.ui.pages.library

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.MorphingPlayPauseButton
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.utility.UiState

@Composable
fun FileManageRail(
    modifier: Modifier = Modifier,
    path: String,
    onFileCountAvailable: (Int) -> Unit
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
                        }
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
    onPlayButtonClick: (Boolean) -> Unit
) {
    Surface(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.medium,
        onClick = {}
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
            OpacitySurface {
                MorphingPlayPauseButton(
                    modifier = Modifier.size(24.dp),
                    isPlaying = isPlaying,
                    onToggle = onPlayButtonClick,
                    fill = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}