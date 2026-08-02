package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.drawPitchLines
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import mai_onsyn.open_rhythm.ui.utility.reMeasure
import kotlin.time.Duration.Companion.milliseconds

data class LiveNote(
    val pitch: Int,
    val startNanos: Long,
    var endNanos: Long? = null,
)

@Composable
fun MidiUpFlow(
    modifier: Modifier = Modifier,
    hps: Dp = 200.dp,     // height moved per seconds
    notes: MutableList<LiveNote> = mutableListOf(),
    minPitch: Int = 21,
    maxPitch: Int = 108,
    spacing: Dp = 1.dp,
    drawPitchLine: Boolean = true,
    blackHorizontalPercentage: Float = 0.75f,
    color: Color = Color.Yellow,
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val gridPos = remember { mutableMapOf<Int, Pair<Float, Float>>() }  // Map<key, Pair<x, width>>

    var maxCapacityNanos by remember { mutableStateOf(Long.MAX_VALUE) }

    var now by remember { mutableStateOf(0L) }

    Canvas(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged { size ->
                reMeasure(gridPos, size, spacingPx, minPitch, maxPitch, blackHorizontalPercentage)
            }
    ) {
        if (drawPitchLine) drawPitchLines(minPitch, maxPitch, gridPos)
        val hpsPx = hps.toPx()
        maxCapacityNanos = (1_000_000_000L / hpsPx * size.height).toLong()

        val toDrawBlacks = mutableListOf<DrawScope.() -> Unit>()
        for (note in notes) {
            val (x, w) = gridPos[note.pitch] ?: continue

            val startElapsed = now - note.startNanos
            val endElapsed = now - (note.endNanos ?: now)

            val startY = size.height - startElapsed / 1_000_000_000f * hpsPx
            val endY = size.height - endElapsed / 1_000_000_000f * hpsPx

            val isBlackKey = isBlackKey(note.pitch)
            val lambda: DrawScope.() -> Unit = {
                drawRoundRect(
                    color = if (isBlackKey)
                        Color(color.red, color.green, color.blue, 0.8f).compositeOver(Color.Black)
                    else color,
                    topLeft = Offset(x, startY),
                    size = Size(w, endY - startY),
                    cornerRadius = CornerRadius(w * 0.1f)
                )
            }
            if (isBlackKey) toDrawBlacks.add(lambda)
            else lambda()
        }

        toDrawBlacks.forEach { it() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)

            val oldest = now - maxCapacityNanos
            notes.filter {
                it.endNanos != null && it.endNanos!! < oldest || it.endNanos == null && it.startNanos < now - (maxCapacityNanos shl 4)
            }.forEach {
                notes.remove(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis {
                now = Time.nanos
            }
        }
    }
}