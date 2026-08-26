package mai_onsyn.open_rhythm.ui.pages.track_edit

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

@Composable
fun TrackTable(
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
                text = "Track",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            inst = { Text(
                text = "Instrument",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            preview = { Text(
                text = "Preview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            color = { Text(
                text = "Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) },
            volume = { Text(
                text = "Volume",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            ) },
            enable = { Text(
                text = "Enabled",
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
            var playingIndex by remember { mutableStateOf(-1) }
            LaunchedEffect(playingIndex) {
                Global.player.stop()
                if (playingIndex != -1) {
                    Global.player.setMidi(
                        midi.take(playingIndex),
                        Global.settings.midiFileSettings[midiPath]?.take(playingIndex)
                    )
                    Global.player.seek(midi.tracks[playingIndex].tickRange.first.toLong())
                    Global.player.play()
                    Global.player.onCompletion = { playingIndex = -1 }
                }
//                Logger.d { "$playingIndex" }
            }
            DisposableEffect(Unit) {
                onDispose {
                    Global.player.stop()
                }
            }
            key(midi) {
                midi.tracks.forEachIndexed { index, track ->
                    TrackTableItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        index = index,
                        track = track,
                        midiPath = midiPath,
                        totalTracks = midi.tracks.size,
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