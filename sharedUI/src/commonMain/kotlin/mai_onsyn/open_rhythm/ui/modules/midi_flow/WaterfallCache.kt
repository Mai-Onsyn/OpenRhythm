package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

class WaterfallCache(
    val windowSize: Size
) {
    private val cachedMap: MutableMap<Int, ImageBitmap> = mutableMapOf()

    fun cacheRange(
        visibleTickCount: Int,
        centerTick: Double,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        pxPerTick: Float,
        gridPos: Map<Int, Pair<Float, Float>>,
        screenWidth: Int,
        noteRoundPercent: Float
    ) {
        val rangeStart = indexAt(centerTick)
        val rangeEnd = indexAt(centerTick + visibleTickCount)
        val rangeCount = rangeEnd - rangeStart
        val lo = rangeStart - rangeCount
        val hi = rangeEnd + rangeCount
        val range = lo..hi
        for (i in range) {
            cache(
                i.toDouble() * windowSize.height,
                midi,
                maxNoteDurationList,
                trackColors,
                pxPerTick,
                gridPos,
                screenWidth,
                noteRoundPercent
            )
        }
        cachedMap.keys.removeAll { it !in range }
    }

    fun cache(
        tick: Double,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        pxPerTick: Float,
        gridPos: Map<Int, Pair<Float, Float>>,
        screenWidth: Int,
        noteRoundPercent: Float
    ) {
        val cachePoint = indexAt(tick)
        if (cachedMap.containsKey(cachePoint)) return

        val notes = filterWindowNotes(midi, tick, windowSize.height.toInt(), maxNoteDurationList, trackColors)
        if (notes.isEmpty()) return

        val height = pxPerTick * windowSize.height

        val bitmap = ImageBitmap(screenWidth, height.toInt())
        val canvas = Canvas(bitmap)

        notes.forEach { note ->
            val (x, w) = gridPos[note.note.pitch] ?: return@forEach
            val pixelPos = ((note.note.tick - tick) * pxPerTick).toFloat()
            val durationPx = note.note.duration * pxPerTick
            val noteRect = Rect(Offset(x, ((height - pixelPos - durationPx))), Size(w, durationPx))
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

    fun getBitmap(
        tick: Double
    ): ImageBitmap? {
        return cachedMap[indexAt(tick)]
    }

    fun getBitmaps(
        tick: Double,
        tickDuration: Double
    ): List<Pair<Float, ImageBitmap>> {
        val result = mutableListOf<Pair<Float, ImageBitmap>>()
        val endTick = tick + tickDuration

        var t = tick
        var iterateCount = 0
        while (t < endTick + windowSize.height) {
            val bitmap = getBitmap(t)
            if (bitmap != null) {
                val offset = getBitmapOffsetTicks(t) - iterateCount * windowSize.height
                result.add(offset to bitmap)
            }

            iterateCount++
            t += windowSize.height
        }
        return result
    }

    fun getBitmapOffsetTicks(
        tick: Double
    ): Float {
        val preciseIndex = tick / (windowSize.height)
        return (preciseIndex - preciseIndex.toInt()).toFloat() * windowSize.height
    }

    private fun indexAt(tick: Double): Int = (tick / (windowSize.height)).toInt()
}