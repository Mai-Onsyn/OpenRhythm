package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.MidiTrack

@Composable
fun BoxScope.PreviewTableItem(
    track: MidiTrack,
    color: Color,
    midiTickRange: IntRange
) {
    val (minPitch, maxPitch) = remember(track) {
        var min = 108
        var max = 21
        for (note in track.notes) {
            if (note.pitch < min) min = note.pitch
            if (note.pitch > max) max = note.pitch
        }
        if (max < min) Pair(Global.settings.MinPitch, Global.settings.MaxPitch)
        else Pair(min, max)
    }
    val pitchSpan = maxPitch - minPitch + 1
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (
                            event.type == PointerEventType.Move && change.pressed ||
                            event.type == PointerEventType.Press
                        ) {
                            Global.player.seek((change.position.x / size.width).toDouble(), true)
//                            Logger.d { "seek ${change.position.x / size.width}" }
                        }
                    }
                }
            }
    ) {
        val heightPerNote = size.height / pitchSpan
        track.notes.forEach { note ->
            val startX = ((note.tick - midiTickRange.first).toDouble() / midiTickRange.last * size.width).toFloat()
            val endX = ((note.tick + note.duration - midiTickRange.first).toDouble() / midiTickRange.last * size.width).toFloat()
            drawRect(
                topLeft = Offset(
                    startX,
                    (maxPitch - note.pitch) * heightPerNote
                ),
                size = Size(
                    endX - startX,
                    heightPerNote
                ),
                color = color
            )
        }
    }
}