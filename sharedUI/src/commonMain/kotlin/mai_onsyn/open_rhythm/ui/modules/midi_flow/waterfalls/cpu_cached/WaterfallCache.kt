package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.cpu_cached

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextLayoutResult
import co.touchlab.kermit.Logger
import com.materialkolor.ktx.darken
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.midi_flow.drawNoteGraphics
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
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
                val cacheStart = Time.nanos
                writeCacheMultiThread(
                    chunk,
                    midi,
                    maxNoteDurationList,
                    trackColors,
                    gridPos,
                    pitchLabels,
                    noteRoundPercent
                )
                chunk.needRefresh = false
                Logger.v { "Cache chunk ${chunk.chunkIndex} cost ${(Time.nanos - cacheStart) * 0.000001}ms" }
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
        val notes =
            filterWindowNotes(midi, startTick.toDouble(), tickWindowSize.toInt(), maxNoteDurationList, trackColors)
        if (notes.isEmpty()) return
        notes.forEach { note ->
            val (x, w) = gridPos[note.note.pitch] ?: return@forEach
            val pixelPos = ((note.note.tick - startTick) * pxPerTick)
            val durationPx = note.note.duration * pxPerTick
            val noteRect = Rect(Offset(x, windowSize.height - pixelPos - durationPx), Size(w, durationPx))
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


    private val tiledDrawer = TiledDrawer(windowSize.height, pxPerTick)
    private fun writeCacheMultiThread(
        chunk: BitmapChunk,
        midi: Midi,
        maxNoteDurationList: List<Long>,
        trackColors: List<Color>,
        gridPos: Map<Int, Pair<Float, Float>>,
        pitchLabels: Map<Int, TextLayoutResult>?,
        noteRoundPercent: Float
    ) {
        val initStart = Time.nanos
        val canvas = Canvas(chunk.bitmap)
        canvas.drawRect(
            Rect(Offset.Zero, Size(chunk.bitmap.width.toFloat(), chunk.bitmap.height.toFloat())),
            TRANSPARENT_PAINT
        )
        val canvasDone = Time.nanos

        val startTick = chunk.chunkIndex * tickWindowSize
        val notes =
            filterWindowNotes(midi, startTick.toDouble(), tickWindowSize.toInt(), maxNoteDurationList, trackColors)
        val filterDone = Time.nanos
        if (notes.isEmpty()) return

        val pitchTiles = notes.groupConsecutiveBy { it.note.pitch }
        val groupDone = Time.nanos
        pitchTiles.forEach { (pitch, subNotes) ->
            val (x, w) = gridPos[pitch] ?: return@forEach
            tiledDrawer.addDrawCall(
                pitch = pitch,
                x = x,
                width = w,
                startTick = startTick,
                noteRoundPercent = noteRoundPercent,
                notes = subNotes
            )
        }
        val addCallDone = Time.nanos

        val drawStart = Time.nanos
        tiledDrawer.draw(canvas)
        val drawDone = Time.nanos

        Logger.v {
            "Init cost ${(canvasDone - initStart) * 0.000001f}ms; " +
                    "Filter cost ${(filterDone - canvasDone) * 0.000001f}ms; " +
                    "Group cost ${(groupDone - filterDone) * 0.000001f}ms; " +
                    "AddCall cost ${(addCallDone - groupDone) * 0.000001f}ms; " +
                    "Draw cost ${(drawDone - drawStart) * 0.000001f}ms"
        }
    }

    fun getBitmaps(
        tick: Double
    ): List<Pair<Float, ImageBitmap>> {
        val result = mutableListOf<Pair<Float, ImageBitmap>>()
        val point = chunkAt(tick)
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

private fun <A, K> List<A>.groupConsecutiveBy(keySelector: (A) -> K): Map<K, List<A>> {
    val result = mutableMapOf<K, List<A>>()
    if (isEmpty()) return result

    var start = 0
    var currentKey = keySelector(this[0])

    for (i in 1 until size) {
        val key = keySelector(this[i])
        if (key != currentKey) {
            result[currentKey] = subList(start, i) // 零开销视图
            start = i
            currentKey = key
        }
    }
    result[currentKey] = subList(start, size) // 最后一段
    return result
}