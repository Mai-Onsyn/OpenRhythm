package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.MidiTrack
import mai_onsyn.open_rhythm.core.settings.addSettings
import mai_onsyn.open_rhythm.core.settings.removeSettings
import mai_onsyn.open_rhythm.ui.pages.track_edit.table_items.*
import mai_onsyn.open_rhythm.ui.theme.TrackColorDefaults

@Composable
fun TrackTableRow(
    modifier: Modifier = Modifier,
    index: Int,
    midiPath: String,
    track: MidiTrack,
    midiTickRange: IntRange,
    isPlaying: Boolean,
    onPlayPauseRequest: (Boolean) -> Unit
) {
    val defaultColor = Global.settings.trackColors.let {
        if (it.isEmpty()) TrackColorDefaults.colors().let { d -> d[index % d.size] }
        else it[index % it.size]
    }
    var rowColor by remember { mutableStateOf(defaultColor) }
    TablePlaceRow(
        modifier = modifier,
        header = { OrderTableItem(index) },
        inst = {
            InstrumentTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.inst ?: track.trackInst,
                onChanged = {
                    if (it == track.trackInst) removeSettings(midiPath, index, inst = true)
                    else addSettings(midiPath, index, inst = it)
                    Global.player.pc(it, track.trackChannel)
                },
                isDrum = track.trackChannel == 9
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
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.color ?: rowColor,
                onChanged = {
                    rowColor = it
                    if (it == defaultColor)
                        removeSettings(midiPath, index, color = true)
                    else addSettings(midiPath, index, color = it)
                }
            )
        },
        volume = {
            VolumeTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.volume ?: 0,
                onChanged = {
                    if (it == 0) removeSettings(midiPath, index, volume = true)
                    else addSettings(midiPath, index, volume = it)
                }
            )
        },
        enable = {
            EnabledTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.let { it.audible ?: true && it.visible ?: true } ?: true,
                onChanged = {
                    if (it) removeSettings(midiPath, index, visible = true, audible = true)
                    else addSettings(midiPath, index, visible = it, audible = it)
                }
            )
        },
        play = {
            PlayPauseButton(
                isPlaying = isPlaying,
                onChanged = onPlayPauseRequest
            )
        }
    )
}

@Composable
fun TrackTableCard(
    modifier: Modifier = Modifier,
    index: Int,
    midiPath: String,
    track: MidiTrack,
    midiTickRange: IntRange,
    isPlaying: Boolean,
    onPlayPauseRequest: (Boolean) -> Unit
) {
    val defaultColor = Global.settings.trackColors.let {
        if (it.isEmpty()) TrackColorDefaults.colors().let { d -> d[index % d.size] }
        else it[index % it.size]
    }
    var rowColor by remember { mutableStateOf(defaultColor) }
    TablePlaceCard(
        modifier = modifier,
        bgColor = defaultColor,
        header = { OrderTableItem(index) },
        inst = {
            InstrumentTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.inst ?: track.trackInst,
                onChanged = {
                    if (it == track.trackInst) removeSettings(midiPath, index, inst = true)
                    else addSettings(midiPath, index, inst = it)
                    Global.player.pc(it, track.trackChannel)
                },
                isDrum = track.trackChannel == 9
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
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.color ?: rowColor,
                onChanged = {
                    rowColor = it
                    if (it == defaultColor)
                        removeSettings(midiPath, index, color = true)
                    else addSettings(midiPath, index, color = it)
                }
            )
        },
        volume = {
            VolumeTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.volume ?: 0,
                onChanged = {
                    if (it == 0) removeSettings(midiPath, index, volume = true)
                    else addSettings(midiPath, index, volume = it)
                }
            )
        },
        enable = {
            EnabledTableItem(
                initial = Global.settings.midiFileSettings[midiPath]?.trackSettings[index]?.let { it.audible ?: true && it.visible ?: true } ?: true,
                onChanged = {
                    if (it) removeSettings(midiPath, index, visible = true, audible = true)
                    else addSettings(midiPath, index, visible = it, audible = it)
                }
            )
        },
        play = {
            PlayPauseButton(
                isPlaying = isPlaying,
                onChanged = onPlayPauseRequest
            )
        }
    )
}