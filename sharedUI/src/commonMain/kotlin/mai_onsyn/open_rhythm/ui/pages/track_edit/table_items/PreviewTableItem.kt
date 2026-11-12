package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.MidiTrack

private fun IntRange.lerp(frac: Double): Double {
    return frac * (last - first) + first
}

@Composable
fun BoxScope.PreviewTableItem(
    track: MidiTrack,
    color: Color,
    midiTickRange: IntRange
) {
    val colorScheme = MaterialTheme.colorScheme
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
    var frame by remember { mutableStateOf(0L) }
    LaunchedEffect(Global.player.isPlaying) {
        while (true) {
            withFrameNanos {
                frame++
            }
        }
    }
    val valuableTicks = midiTickRange.last - midiTickRange.first
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
                            Global.player.seek(midiTickRange.lerp((change.position.x.toDouble() / size.width).coerceIn(0.0, 1.0)), false)
//                            Logger.d { "seek ${change.position.x / size.width}" }
                        }
                    }
                }
            }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        frame

        val heightPerNote = size.height / pitchSpan
        track.notes.forEach { note ->
            val startX = ((note.tick - midiTickRange.first).toDouble() / valuableTicks * size.width).toFloat()
            val endX = ((note.tick + note.duration - midiTickRange.first).toDouble() / valuableTicks * size.width).toFloat()
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

        if (Global.player.isPlaying) {
            drawRect(
                color = colorScheme.primary,
                topLeft = Offset(((Global.player.preciseTick - midiTickRange.first) / valuableTicks).toFloat() * size.width, 0f),
                size = Size(1.dp.toPx(), size.height)
            )
        }
    }
}