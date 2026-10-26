package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.BaseMidiWaterfall
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope

@Composable
fun GLInstancedWaterfall(
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
        OpenGLWaterfall(
            Modifier.matchParentSize(),
            trackColors,
            activeNoteOutput,
            noteRoundPercent
        )
    }
}

@Composable
expect fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float
)