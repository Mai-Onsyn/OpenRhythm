package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.getContrastTextColor
import mai_onsyn.open_rhythm.ui.utility.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.seconds

interface NoteRenderScope : BoxScope {
    val currTick: Double
    val visibleTickCount: Int
    val pxPerTick: Float
    val gridPos: Map<Int, Pair<Float, Float>>
    val pitchLabels: Map<Int, TextLayoutResult>?
    val maxNoteDurationList: List<Long>
    val whiteKeyWidth: Float
    val status: WaterfallRenderingStatus
}

class WaterfallRenderingStatus(
    var currentNoteCount: Int = -1,
    var activeNoteCount: Int = -1
)

@Composable
fun BaseMidiWaterfall(
    modifier: Modifier = Modifier,
    currTick: Double = 0.0,
    midi: Midi,
    minPitch: Int = 21,
    maxPitch: Int = 108,
    hpb: Dp = 120.dp,
    spacing: Dp = 1.dp,
    blackHorizontalPercentage: Float = 0.75f,
    drawSectionLine: Boolean = true,
    drawOctaveLine: Boolean = true,
    drawPitchLabel: Boolean = false,
    onVerticalDragged: (Float) -> Unit = {},
    noteContent: @Composable NoteRenderScope.() -> Unit
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val gridPos = remember { mutableMapOf<Int, Pair<Float, Float>>() }

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
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val pitchLabels = if (drawPitchLabel) {
        val pitchNames = remember(minPitch, maxPitch) {
            mutableStateMapOf<Int, String>().apply {
                for (i in minPitch..maxPitch) put(i, Note.toString(i))
            }
        }
        rememberTextLayoutResult(
            texts = pitchNames,
            fontSize = (whiteKeyWidth * 0.3f).sp,
            color = { Color.White }
        )
    } else null

    val pxPerTick = with(density) { hpb.toPx() / midi.ppq }
    val visibleTickCount = if (canvasSize.height > 0) (canvasSize.height / with(density) { hpb.toPx() } * midi.ppq).toInt() else 0

    // FPS 与调试状态统计
    var lastDrawTime by remember { mutableStateOf(0L) }
    var frameTime by remember { mutableStateOf(0L) }
    var fps by remember { mutableStateOf(0) }
    var countedFrames by remember { mutableStateOf(0) }
    var lastClearFrameCounterTime by remember { mutableStateOf(0L) }

    if (Global.settings.ShowFps) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(1.seconds)
                val now = Time.micros
                fps = (1_000_000f * countedFrames / (now - lastClearFrameCounterTime)).roundToInt()
                countedFrames = 0
                lastClearFrameCounterTime = now
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged { size ->
                canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount
                reMeasure(gridPos, size, spacingPx, minPitch, maxPitch, blackHorizontalPercentage)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (change.pressed && change.positionChanged()) {
                            onVerticalDragged(change.position.y - change.previousPosition.y)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (drawOctaveLine) drawOctaveLines(minPitch, maxPitch, gridPos)
            if (drawSectionLine) drawSectionLines(midi, currTick, currTick + visibleTickCount, pxPerTick)

            // 统计 FPS
            val now = Time.millis
            frameTime = now - lastDrawTime
            lastDrawTime = now
            countedFrames++
        }

        val renderingStatus = remember { WaterfallRenderingStatus() }
        val renderScope = remember(currTick, canvasSize, hpb, midi) {
            object : NoteRenderScope, BoxScope by this {
                override val currTick: Double = currTick
                override val visibleTickCount: Int = visibleTickCount
                override val pxPerTick: Float = pxPerTick
                override val gridPos: Map<Int, Pair<Float, Float>> = gridPos
                override val pitchLabels: Map<Int, TextLayoutResult>? = pitchLabels
                override val maxNoteDurationList: List<Long> = maxNoteDurationList
                override val whiteKeyWidth: Float = whiteKeyWidth
                override val status: WaterfallRenderingStatus = renderingStatus
            }
        }
        renderScope.noteContent()

        Box(Modifier.matchParentSize()) {
            val colorScheme = MaterialTheme.colorScheme
            val color by Global.settings.WaterfallBackgroundColor.let {
                remember(it) { mutableStateOf(if (it.isSpecified) getContrastTextColor(it) else colorScheme.onSurface) }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                @Composable
                fun ShowText(str: String) {
                    Text(text = str, style = MaterialTheme.typography.bodyMedium, color = color)
                }
                if (Global.settings.ShowCurrentTick) ShowText("Tick=${currTick.roundToLong()}")
                if (Global.settings.ShowFps) ShowText("FPS=${fps}")
                if (Global.settings.ShowFrameTime) ShowText("Frame Time=${frameTime}")
                if (Global.settings.ShowRenderingNoteCount && renderingStatus.currentNoteCount != -1) ShowText("Rendering Notes=${renderingStatus.currentNoteCount}")
                if (Global.settings.ShowActiveNoteCount && renderingStatus.activeNoteCount != -1) ShowText("Active Notes=${renderingStatus.activeNoteCount}")
            }
        }
    }
}