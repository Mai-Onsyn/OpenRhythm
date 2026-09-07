package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachReversed
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.countWhiteKeys
import mai_onsyn.open_rhythm.ui.utility.drawOctaveLines
import mai_onsyn.open_rhythm.ui.utility.drawSectionLines
import mai_onsyn.open_rhythm.ui.utility.reMeasure
import mai_onsyn.open_rhythm.ui.utility.rememberTextLayoutResult

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

    val pxPerTick = with(density) { hpb.toPx() / midi.ppq }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmapCache = remember(midi, canvasSize, hpb) {
        WaterfallCache(canvasSize.toSize(), pxPerTick)
    }
//    var cacheVersion by remember { mutableStateOf(-1) }

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

    Box {
        Canvas(
            modifier = modifier
                .clip(RectangleShape)
                .onSizeChanged { size ->
                    whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount
                    canvasSize = size
                    reMeasure(gridPos, size, spacingPx, minPitch, maxPitch, blackHorizontalPercentage)
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

        LaunchedEffect(currTick, midi, canvasSize, hpb) {
            if (canvasSize.width > 0 && canvasSize.height > 0) {
                withContext(Dispatchers.Default) {
                    imageBitmapCache.cacheRange(
                        currTick,
                        midi,
                        maxNoteDurationList,
                        trackColors,
                        gridPos,
                        noteRoundPercent
                    )
                }
            }
        }
        imageBitmapCache.getBitmaps(currTick).fastForEachReversed { (offset, bitmap) ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationY = offset * pxPerTick
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