package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.materialkolor.ktx.darken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.roundToInt

private val TRANSPARENT_PAINT = Paint().apply {
    color = Color.Transparent
    blendMode = BlendMode.Clear
    isAntiAlias = false
}
private val EMPTY_PAINT = Paint()

data class Tile(
    val pitch: Int,
    val bitmap: ImageBitmap,
    val canvas: Canvas,
    var x: Float,
    var width: Float,
    var startTick: Float,
    var noteRoundPercent: Float,
    var notes: List<DrawableNote> = mutableListOf()
) {
    val isBlackKey: Boolean
        get() = isBlackKey(pitch)
}

class TiledDrawer (
    val bufferHeight: Float,
    val pxPerTick: Float,
    private val tiles: MutableMap<Int, Tile> = mutableMapOf()
) {
    private val dirty = LinkedHashSet<Int>()
    /**
     * @param width 音符宽度 不带阴影
     */
    fun register(
        pitch: Int,
        x: Float,
        width: Float,
        startTick: Float,
        noteRoundPercent: Float,
    ) {
        tiles.getOrPut(pitch) {
            // 带阴影容差
            val buffer = ImageBitmap((width * 1.2f).roundToInt(), bufferHeight.roundToInt())
            Tile(
                pitch = pitch,
                bitmap = buffer,
                width = width,
                canvas = Canvas(buffer),
                x = x,
                startTick = startTick,
                noteRoundPercent = noteRoundPercent
            )
        }
    }

    fun addDrawCall(
        pitch: Int,
        x: Float,
        width: Float,
        startTick: Float,
        noteRoundPercent: Float,
        notes: List<DrawableNote>
    ) {
        register(pitch, x, width, startTick, noteRoundPercent)
        tiles[pitch]?.apply {
            this.x = x
            this.width = width
            this.startTick = startTick
            this.noteRoundPercent = noteRoundPercent
            this.notes = notes
        }
        dirty += pitch
    }

    fun draw(
        canvas: Canvas,
    ) {
        val batch = dirty.mapNotNull { tiles[it] }
        runBlocking {
            executeAll(batch, 8) { tile ->
                // clear
                tile.canvas.drawRect(
                    Rect(Offset.Zero, Size(tile.bitmap.width.toFloat(), tile.bitmap.height.toFloat())),
                    TRANSPARENT_PAINT
                )

                // note draw
                val blackKey = tile.isBlackKey
                tile.notes.forEach { note ->
                    val pixelPos = ((note.note.tick - tile.startTick) * pxPerTick)
                    val durationPx = note.note.duration * pxPerTick
                    val noteRect = Rect(Offset(0f, bufferHeight - pixelPos - durationPx), Size(tile.width, durationPx))
                    drawNoteGraphics(
                        tile.canvas,
                        if (blackKey) note.color.darken(1.5f) else note.color,
                        noteRect,
                        tile.width * 0.5f * tile.noteRoundPercent
                    )
                }
            }
        }

        // merge
        batch.sortedWith(compareBy( { isBlackKey(it.pitch) }, { it.pitch } )).forEach { tile ->
            canvas.drawImage(
                tile.bitmap,
                Offset(tile.x, 0f),
                EMPTY_PAINT
            )
        }

        dirty.clear()
    }
}

@OptIn(ExperimentalAtomicApi::class)
suspend fun <T> executeAll(
    tasks: List<T>,
    parallelism: Int = 4,
    process: (T) -> Unit
) = coroutineScope {
    val size = tasks.size
    if (size == 0) return@coroutineScope

    val index = AtomicInt(0)
    val workers = minOf(parallelism, size)

    repeat(workers) {
        launch(Dispatchers.Default) {
            while (true) {
                val i = index.fetchAndAdd(1)   // ~10ns
                if (i >= size) break           // 越界即退出
                process(tasks[i])
            }
        }
    }
}