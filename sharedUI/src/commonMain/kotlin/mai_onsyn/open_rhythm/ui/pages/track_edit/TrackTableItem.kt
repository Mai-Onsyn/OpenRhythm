package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.MidiTrack
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.ColorTableItem
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.EnabledTableItem
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.InstrumentTableItem
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.OrderTableItem
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.PlayPauseButton
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.PreviewTableItem
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.VolumeTableItem

@Composable
fun TrackTableItem(
    modifier: Modifier = Modifier,
    index: Int,
    track: MidiTrack,
    totalTracks: Int,
    midiTickRange: IntRange
) {
    var rowColor by remember { mutableStateOf(Global.settings.trackColors[index % totalTracks]) }
    TablePlaceRow(
        modifier = modifier,
        header = { OrderTableItem(index) },
        inst = {
            InstrumentTableItem(
                initial = track.trackInst,
                onChanged = {}
            )
        },
        preview = {
            PreviewTableItem(
                track = track,
                color = rowColor,
                midiTickRange = midiTickRange
            )
        },
        color = {
            ColorTableItem(
                initial = rowColor,
                onChanged = { rowColor = it }
            )
        },
        volume = {
            VolumeTableItem(
                initial = 0f,
                onChanged = {}
            )
        },
        enable = {
            EnabledTableItem(
                initial = true,
                onChanged = {}
            )
        },
        play = {
            PlayPauseButton(
                initial = false,
                onChanged = {}
            )
        }
    )
}