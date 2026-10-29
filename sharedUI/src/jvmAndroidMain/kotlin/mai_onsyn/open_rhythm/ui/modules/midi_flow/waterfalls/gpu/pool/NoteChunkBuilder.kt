package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.graphics.Color
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.DrawableNote
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes

class NoteChunkBuilder(
    val chunkTickSize: Int,
    val midi: Midi,
    val maxNoteDurationList: List<Long>,
    val trackColors: List<Color>
) {
    fun buildDrawableNotes(tick: Double): List<DrawableNote> {
        return filterWindowNotes(midi, tick, chunkTickSize, maxNoteDurationList, trackColors)
    }
}