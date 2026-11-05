package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.DrawableNote
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.ToDrawRect
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

class NoteChunkBuilder(
    val chunkTickSize: Int,
    val midi: Midi,
    val maxNoteDurationList: List<Long>,
    val trackColors: List<Color>,
    val gridPos: Map<Int, Pair<Float, Float>>,
    var windowSizePx: Size
) {
    fun buildDrawableNotes(tick: Double): List<DrawableNote> {
        return filterWindowNotes(midi, tick, chunkTickSize, maxNoteDurationList, trackColors)
    }

    fun buildGLRect(chunkID: Int, dn: DrawableNote): ToDrawRect? {
        val (x, w) = gridPos[dn.note.pitch] ?: return null
        return ToDrawRect(
            x / windowSizePx.width,
            w / windowSizePx.width,
            (dn.note.tick.toDouble() - (chunkID * chunkTickSize).toDouble()).toFloat(),
            dn.note.duration.toFloat(),
            if (isBlackKey(dn.note.pitch)) dn.color.darken(1.5f).toArgb() else dn.color.toArgb(),
            dn.note.depth.toInt()
        )
    }
}