package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

data class WaterfallChunk(
    val chunkIndex: Int,
    val offsetTicks: Float,
    val bitmap: ImageBitmap
)

class WaterfallCache(
    val windowSize: Size,
    val pxPerTick: Float
) {
    private val cachedMap = mutableStateMapOf<Int, ImageBitmap>()
    private val tickWindowSize
        get() = windowSize.height / pxPerTick

    fun cacheRange(
        centerTick: Double,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        gridPos: Map<Int, Pair<Float, Float>>,
        noteRoundPercent: Float
    ): Int {
        val rangeStart = indexAt(centerTick)
        val range = (rangeStart - 1) .. (rangeStart + 2)
        for (i in range) {
            cache(
                i.toDouble() * tickWindowSize,
                midi,
                maxNoteDurationList,
                trackColors,
                gridPos,
                noteRoundPercent
            )
        }
        cachedMap.keys.removeAll { it !in range }
        return rangeStart
    }

    fun cache(
        tick: Double,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        gridPos: Map<Int, Pair<Float, Float>>,
        noteRoundPercent: Float
    ) {
        val cachePoint = indexAt(tick)
        if (cachedMap.containsKey(cachePoint)) return

        val notes = filterWindowNotes(midi, tick, tickWindowSize.toInt(), maxNoteDurationList, trackColors)
        if (notes.isEmpty()) return
        if (windowSize.width.toInt() == 0 || windowSize.height.toInt() == 0) return

        val bitmap = ImageBitmap(windowSize.width.toInt(), windowSize.height.toInt())
        val canvas = Canvas(bitmap)

        notes.forEach { note ->
            val (x, w) = gridPos[note.note.pitch] ?: return@forEach
            val pixelPos = ((note.note.tick - tick) * pxPerTick).toFloat()
            val durationPx = note.note.duration * pxPerTick
            val noteRect = Rect(Offset(x, ((windowSize.height - pixelPos - durationPx))), Size(w, durationPx))
            val blackKey = isBlackKey(note.note.pitch)
            drawNoteGraphics(
                canvas,
                if (blackKey) note.color.darken(1.5f) else note.color,
                noteRect,
                w * 0.5f * noteRoundPercent
            )
        }
        cachedMap[cachePoint] = bitmap
    }

    fun getBitmaps(
        tick: Double
    ): List<Pair<Float, ImageBitmap>> {
        val result = mutableListOf<Pair<Float, ImageBitmap>>()
        val point = indexAt(tick)
        cachedMap[point]?.let { result.add(getBitmapOffsetTicks(tick) to it) }
        cachedMap[point + 1]?.let { result.add((getBitmapOffsetTicks(tick) - tickWindowSize) to it) }
        return result
    }

    fun getChunks(tick: Double): List<WaterfallChunk> {
        val result = mutableListOf<WaterfallChunk>()
        val point = indexAt(tick)

        cachedMap[point]?.let {
            result.add(WaterfallChunk(point, getBitmapOffsetTicks(tick), it))
        }
        cachedMap[point + 1]?.let {
            result.add(WaterfallChunk(point + 1, getBitmapOffsetTicks(tick) - tickWindowSize.toFloat(), it))
        }
        return result
    }

    fun getBitmapOffsetTicks(
        tick: Double
    ): Float {
        val preciseIndex = tick / tickWindowSize
        return (preciseIndex - preciseIndex.toInt()).toFloat() * tickWindowSize
    }

    fun indexAt(tick: Double): Int = (tick / tickWindowSize).toInt()
}