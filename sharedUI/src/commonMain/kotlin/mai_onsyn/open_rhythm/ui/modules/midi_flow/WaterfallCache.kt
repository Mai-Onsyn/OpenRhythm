package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextLayoutResult
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.utility.drawTextCentered
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import kotlin.concurrent.Volatile

data class BitmapChunk(
    var bitmap: ImageBitmap,
    var chunkIndex: Int,
    var needRefresh: Boolean = false
)

class WaterfallCache(
    val windowSize: Size,
    val pxPerTick: Float
) {
    private val tickWindowSize
        get() = windowSize.height / pxPerTick

    private var bitmapArray = Array(4) {
        BitmapChunk(
            ImageBitmap(windowSize.width.toInt(), windowSize.height.toInt()),
            it - 1
        )
    }
    @Volatile
    private var currChunk = Int.MIN_VALUE

    fun cache(
        tick: Double,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        gridPos: Map<Int, Pair<Float, Float>>,
        pitchLabels: Map<Int, TextLayoutResult>?,
        noteRoundPercent: Float
    ): Boolean {
        val cachePoint = chunkAt(tick)

        when (if (currChunk == Int.MAX_VALUE) 0xFF else cachePoint - currChunk) {
            0 -> return false
            1 -> bitmapArray = arrayOf(
                bitmapArray[1],
                bitmapArray[2],
                bitmapArray[3],
                bitmapArray[0]
            ).apply { this[3].needRefresh = true }
            2 -> bitmapArray = arrayOf(
                bitmapArray[2],
                bitmapArray[3],
                bitmapArray[0],
                bitmapArray[1]
            ).apply {
                this[2].needRefresh = true
                this[3].needRefresh = true
            }
            3 -> bitmapArray = arrayOf(
                bitmapArray[3],
                bitmapArray[0],
                bitmapArray[1],
                bitmapArray[2]
            ).apply {
                this[1].needRefresh = true
                this[2].needRefresh = true
                this[3].needRefresh = true
            }
            -1 -> bitmapArray = arrayOf(
                bitmapArray[3],
                bitmapArray[0],
                bitmapArray[1],
                bitmapArray[2]
            ).apply { this[0].needRefresh = true }
            -2 -> bitmapArray = arrayOf(
                bitmapArray[2],
                bitmapArray[3],
                bitmapArray[0],
                bitmapArray[1]
            ).apply {
                this[0].needRefresh = true
                this[1].needRefresh = true
            }
            -3 -> bitmapArray = arrayOf(
                bitmapArray[1],
                bitmapArray[2],
                bitmapArray[3],
                bitmapArray[0],
            ).apply {
                this[0].needRefresh = true
                this[1].needRefresh = true
                this[2].needRefresh = true
            }
            else -> bitmapArray.forEach { it.needRefresh = true }
        }
        currChunk = cachePoint
        bitmapArray.forEachIndexed { index, chunk ->
            chunk.chunkIndex = currChunk - 1 + index
            if (chunk.needRefresh) {
                writeCache(
                    chunk,
                    midi,
                    maxNoteDurationList,
                    trackColors,
                    gridPos,
                    pitchLabels,
                    noteRoundPercent
                )
                chunk.needRefresh = false
            }
        }
        return true
    }

    private val TRANSPARENT_PAINT = Paint().apply {
        color = Color.Transparent
        blendMode = BlendMode.Clear
        isAntiAlias = false
    }
    private fun writeCache(
        chunk: BitmapChunk,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        gridPos: Map<Int, Pair<Float, Float>>,
        pitchLabels: Map<Int, TextLayoutResult>?,
        noteRoundPercent: Float
    ) {
        val canvas = Canvas(chunk.bitmap)
        canvas.drawRect(
            Rect(Offset.Zero, Size(chunk.bitmap.width.toFloat(), chunk.bitmap.height.toFloat())),
            TRANSPARENT_PAINT
        )
        val startTick = chunk.chunkIndex * tickWindowSize
        val notes = filterWindowNotes(midi, startTick.toDouble(), tickWindowSize.toInt(), maxNoteDurationList, trackColors)
        if (notes.isEmpty()) return
        notes.forEach { note ->
            val (x, w) = gridPos[note.note.pitch] ?: return@forEach
            val pixelPos = ((note.note.tick - startTick) * pxPerTick)
            val durationPx = note.note.duration * pxPerTick
            val noteRect = Rect(Offset(x, ((windowSize.height - pixelPos - durationPx))), Size(w, durationPx))
            val blackKey = isBlackKey(note.note.pitch)
            drawNoteGraphics(
                canvas,
                if (blackKey) note.color.darken(1.5f) else note.color,
                noteRect,
                w * 0.5f * noteRoundPercent
            )
            pitchLabels?.get(note.note.pitch)?.let {
                val labelCenterPos = noteRect.bottomCenter.let { offset -> offset.copy(y = offset.y - noteRect.width * 0.5f) }
                drawTextCentered(
                    canvas,
                    it,
                    labelCenterPos,
                )
            }
        }
    }

    fun getBitmaps(
        tick: Double
    ): List<Pair<Float, ImageBitmap>> {
        val result = mutableListOf<Pair<Float, ImageBitmap>>()
        val point = chunkAt(tick)
//        val idx1 = point - currChunk + 1
//        val idx2 = point - currChunk + 2
//        val b1 = bitmapArray.getOrNull(idx1)
//        val b2 = bitmapArray.getOrNull(idx2)
//        println("getBitmaps: point=$point, currChunk=$currChunk, idx1=$idx1, idx2=$idx2, b1=${b1?.bitmap}, b2=${b2?.bitmap}")
        bitmapArray.getOrNull(point - currChunk + 1)?.let {
            result.add(getBitmapOffsetTicks(tick) to it.bitmap)
        }
        bitmapArray.getOrNull(point - currChunk + 2)?.let {
            result.add((getBitmapOffsetTicks(tick) - tickWindowSize) to it.bitmap)
        }

        return result
    }

    fun getBitmapOffsetTicks(
        tick: Double
    ): Float {
        val preciseIndex = tick / tickWindowSize
        return (preciseIndex - preciseIndex.toInt()).toFloat() * tickWindowSize
    }

    fun chunkAt(tick: Double): Int = (tick / tickWindowSize).toInt()
}