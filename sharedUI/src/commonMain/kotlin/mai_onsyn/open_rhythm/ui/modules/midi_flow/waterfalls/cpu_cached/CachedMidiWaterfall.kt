package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.cpu_cached

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.BaseMidiWaterfall
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes


@Composable
fun CachedMidiWaterfall(
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
    var renderedTimes by remember { mutableStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    BaseMidiWaterfall(
        modifier = modifier.onSizeChanged { canvasSize = it },
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
        val imageBitmapCache = remember(midi, canvasSize, hpb) {
            if (canvasSize.width == 0 || canvasSize.height == 0) null
            else WaterfallCache(canvasSize.toSize(), pxPerTick).apply {
                cache(currTick, midi, maxNoteDurationList, trackColors, gridPos, pitchLabels, noteRoundPercent)
            }
        }

        LaunchedEffect(currTick, canvasSize, midi, hpb) {
            if (withContext(Dispatchers.Default) {
                    imageBitmapCache?.cache(currTick, midi, maxNoteDurationList, trackColors, gridPos, pitchLabels, noteRoundPercent)
                } != false
            ) {
                renderedTimes++
            }
        }

        val bitmaps = remember(renderedTimes, currTick, canvasSize, hpb, midi) {
            imageBitmapCache?.getBitmaps(currTick) ?: emptyList()
        }

        bitmaps.forEachIndexed { idx, (offset, bitmap) ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.matchParentSize().graphicsLayer {
                    translationY = offset * pxPerTick + idx
                }
            )
        }

        val notes = filterWindowNotes(midi, currTick, 0, maxNoteDurationList, trackColors)
        activeNoteOutput.clear()
        notes.forEach { note ->
            activeNoteOutput[note.note.pitch] = note.color
        }
        status.activeNoteCount = activeNoteOutput.size
    }
}