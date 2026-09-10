package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastForEachReversed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.ui.utility.*

@Composable
fun CachedMidiWaterfall(
    modifier: Modifier = Modifier,
    currTick: Double = 0.0,
    midi: Midi,
    minPitch: Int = 21,     // A0 default
    maxPitch: Int = 108,    // C8 default
    trackColors: List<Color> = emptyList(),
    hpb: Dp = 120.dp, // height(dp) per beat
    spacing: Dp = 1.dp,
    blackHorizontalPercentage: Float = 0.75f,
    activeNoteOutput: MutableMap<Int, Color> = mutableMapOf(),
    drawSectionLine: Boolean = true,
    drawOctaveLine: Boolean = true,
    noteRoundPercent: Float = 0.2f,
    drawPitchLabel: Boolean = false,
    onVerticalDragged: (Float) -> Unit = {}
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val gridPos = remember { mutableMapOf<Int, Pair<Float, Float>>() }  // Map<key, Pair<x, width>>
    val maxNoteDurationList = remember(midi) {
        val result = mutableListOf<Long>()
        for (midiTrack in midi.tracks) {
            if (midiTrack.visible && midiTrack.enable)
                result.add(midiTrack.notes.maxOfOrNull { it.duration } ?: 0)
            else result.add(0L)
        }
        result
    }

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)
    var whiteKeyWidth by remember { mutableStateOf(0f) }

    val pitchLabels = if (drawPitchLabel) {
        val pitchNames = remember(minPitch, maxPitch) {
            mutableStateMapOf<Int, String>().apply {
                for (i in minPitch..maxPitch)
                    put(i, Note.toString(i))
            }
        }
        rememberTextLayoutResult(
            texts = pitchNames,
            fontSize = (whiteKeyWidth * 0.3f).sp,
            color = { Color.White }
        )
    } else null

    var renderedTimes by remember { mutableStateOf(0) }
    val pxPerTick = with(density) { hpb.toPx() / midi.ppq }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmapCache = remember(midi, canvasSize, hpb) {
        if (canvasSize.width == 0 || canvasSize.height == 0) null
        else WaterfallCache(canvasSize.toSize(), pxPerTick).apply {
            cache(
                currTick,
                midi,
                maxNoteDurationList,
                trackColors,
                gridPos,
                pitchLabels,
                noteRoundPercent
            )
        }
    }

    Box(Modifier.clip(RectangleShape)) {
        Canvas(
            modifier = modifier
                .onSizeChanged { size ->
                    whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount
                    reMeasure(gridPos, size, spacingPx, minPitch, maxPitch, blackHorizontalPercentage)
                    canvasSize = size
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()
                            if (change.pressed && change.positionChanged()) {
                                val deltaPx = change.position.y - change.previousPosition.y
                                onVerticalDragged(deltaPx)
                            }
                        }
                    }
                }
        ) {
            if (drawOctaveLine) drawOctaveLines(minPitch, maxPitch, gridPos)
            val height = size.height
            val visibleTickCount = (height / hpb.toPx() * midi.ppq).toInt()
            if (drawSectionLine) drawSectionLines(midi, currTick, currTick + visibleTickCount, pxPerTick)
        }

        LaunchedEffect(currTick, canvasSize, midi, hpb) {
            if (withContext(Dispatchers.Default) {
                imageBitmapCache?.cache(
                    currTick,
                    midi,
                    maxNoteDurationList,
                    trackColors,
                    gridPos,
                    pitchLabels,
                    noteRoundPercent
                ) } != false
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
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationY = offset * pxPerTick + idx
                    }
            )
        }
        val notes = filterWindowNotes(midi, currTick, 0, maxNoteDurationList, trackColors)
        activeNoteOutput.clear()
        notes.forEach { note ->
            activeNoteOutput[note.note.pitch] = note.color
        }
    }
}