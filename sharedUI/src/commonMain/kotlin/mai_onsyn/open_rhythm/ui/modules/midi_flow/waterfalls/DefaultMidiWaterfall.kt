package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.drawNoteGraphics
import mai_onsyn.open_rhythm.ui.utility.drawTextCentered
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

@Composable
fun DefaultMidiWaterfall(
    modifier: Modifier = Modifier,
    currTick: Double = 0.0,
    midi: Midi,
    minPitch: Int = 21,
    maxPitch: Int = 108,
    trackColors: List<Color> = emptyList(),
    hpb: Dp = 120.dp,
    spacing: Dp = 1.dp,
    blackHorizontalPercentage: Float = 0.75f,
    activeNoteOutput: MutableMap<Int, Color> = mutableMapOf(),
    drawSectionLine: Boolean = true,
    drawOctaveLine: Boolean = true,
    noteRoundPercent: Float = 0.2f,
    drawPitchLabel: Boolean = false,
    onVerticalDragged: (Float) -> Unit = {}
) {
    BaseMidiWaterfall(
        modifier = modifier,
        currTick = currTick,
        midi = midi,
        minPitch = minPitch,
        maxPitch = maxPitch,
        hpb = hpb,
        spacing = spacing,
        blackHorizontalPercentage = blackHorizontalPercentage,
        drawSectionLine = drawSectionLine,
        drawOctaveLine = drawOctaveLine,
        drawPitchLabel = drawPitchLabel,
        onVerticalDragged = onVerticalDragged
    ) {
        var renderingNoteCount by remember { mutableStateOf(0) }
        var activeNoteCount by remember { mutableStateOf(0) }
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasHeight = size.height
            val toDrawNotes = filterWindowNotes(midi, currTick, visibleTickCount, maxNoteDurationList, trackColors)
            renderingNoteCount = toDrawNotes.size

            activeNoteOutput.clear()
            fun drawNote(pack: DrawableNote, blackKey: Boolean) {
                val (x, w) = gridPos[pack.note.pitch] ?: return
                val pixelPos = (pack.note.tick - currTick) * pxPerTick
                val durationPx = pack.note.duration * pxPerTick
                val noteRect = Rect(Offset(x, (canvasHeight - pixelPos - durationPx).toFloat()), Size(w, durationPx))

                drawNoteGraphics(
                    if (blackKey) pack.color.darken(1.5f) else pack.color,
                    noteRect,
                    w * 0.5f * noteRoundPercent
                )
                pitchLabels?.get(pack.note.pitch)?.let {
                    val labelCenterPos = noteRect.bottomCenter.let { offset -> offset.copy(y = offset.y - whiteKeyWidth * 0.4f) }
                    drawTextCentered(it, labelCenterPos)
                }
                if (pack.note.tick <= currTick && pack.note.tick + pack.note.duration >= currTick) {
                    activeNoteOutput[pack.note.pitch] = pack.color
                    activeNoteCount++
                }
            }

            activeNoteCount = 0
            for (pack in toDrawNotes) { if (!isBlackKey(pack.note.pitch)) drawNote(pack, false) }
            for (pack in toDrawNotes) { if (isBlackKey(pack.note.pitch)) drawNote(pack, true) }
            status.currentNoteCount = toDrawNotes.size
            status.activeNoteCount = activeNoteCount
        }
    }
}