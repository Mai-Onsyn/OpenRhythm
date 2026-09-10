package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.take
import mai_onsyn.open_rhythm.core.settings.take
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun TrackWideTable(
    modifier: Modifier = Modifier,
    midiPath: String,
    midi: Midi
) {
    Column(modifier = modifier) {
        TablePlaceRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            header = { Text(
                text = str(Res.string.track_columnTrack),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            inst = { Text(
                text = str(Res.string.track_columnInstrument),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            preview = { Text(
                text = str(Res.string.track_columnPreview),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            color = { Text(
                text = str(Res.string.track_columnColor),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) },
            volume = { Text(
                text = str(Res.string.track_columnVolume),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            enable = { Text(
                text = str(Res.string.track_columnEnabled),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            play = {}
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            var playingIndex by bindPlayable(midi, midiPath)
            key(midi) {
                midi.tracks.forEachIndexed { index, track ->
                    TrackTableRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        index = index,
                        track = track,
                        midiPath = midiPath,
                        midiTickRange = midi.startTick..midi.endTick,
                        isPlaying = index == playingIndex,
                        onPlayPauseRequest = {
                            playingIndex = if (it) index else -1
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun bindPlayable(midi: Midi, midiPath: String): MutableState<Int> {
    val playingIndex = remember { mutableStateOf(-1) }
    LaunchedEffect(playingIndex) {
        Global.player.stop()
        if (playingIndex.value != -1) {
            Global.player.setMidi(
                midi.take(playingIndex.value),
                Global.settings.midiFileSettings[midiPath]?.take(playingIndex.value)
            )
            Global.player.seek(midi.tracks[playingIndex.value].tickRange.first.toLong())
            Global.player.play()
            Global.player.onCompletion = { playingIndex.value = -1 }
            Logger.i { "Playing track ${playingIndex.value} in MIDI ${midi.name}" }
        } else Logger.d { "Stopped playback: idx=-1 (stop flag)" }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (playingIndex.value != -1) {
                Global.player.stop()
                Logger.d { "Stopped playback: idx=-1 (stop flag)" }
            }
        }
    }
    return playingIndex
}

@Composable
fun TrackNarrowTable(
    modifier: Modifier = Modifier,
    midiPath: String,
    midi: Midi
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        var playingIndex by bindPlayable(midi, midiPath)
        key(midi) {
            midi.tracks.forEachIndexed { index, track ->
                TrackTableCard(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .height(144.dp),
                    index = index,
                    track = track,
                    midiPath = midiPath,
                    midiTickRange = midi.startTick..midi.endTick,
                    isPlaying = index == playingIndex,
                    onPlayPauseRequest = {
                        playingIndex = if (it) index else -1
                    }
                )
            }
        }
    }
}