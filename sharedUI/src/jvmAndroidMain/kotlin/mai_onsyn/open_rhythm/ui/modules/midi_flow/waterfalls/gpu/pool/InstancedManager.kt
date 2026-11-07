package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.runBlocking
import mai_onsyn.compose_gl.gl.glUseProgram
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.Shader
import openrhythm.sharedui.generated.resources.Res
import kotlin.math.floor

class InstancedManager(
    var chunkTickSize: Int,
    var currentTick: Double,
    var screenHeightTick: Int,
    var chunkBuilder: NoteChunkBuilder
) {
    var screenChunkCount = 0
    private val pool: MutableList<InstanceSlot> = mutableListOf()
    private val quadRect: QuadRect = QuadRect()
    private val uniforms: Uniforms = Uniforms()
    private lateinit var shader: Shader

    /**
     * 输入tick，根据tick来轮换vbo指针
     */
    fun update(tick: Double) {
        val startChunk = floor(tick / chunkTickSize).toInt()
        currentTick = tick

        screenChunkCount = screenHeightTick / chunkTickSize + 2
        (screenChunkCount - pool.size).let { difference ->
            if (difference > 0) repeat(difference) {
                pool.add(InstanceSlot(null))
            }
        }

        // pool一般应该不超过10
        val searchCache = arrayOfNulls<InstanceSlot>(screenChunkCount)
        val usedPoolIndex = mutableSetOf<Int>()
        for (i in 0 until screenChunkCount) {
            val chunkID = i + startChunk
            val idx = pool.indices.firstOrNull {
                pool[it].chunk?.id == chunkID
            } ?: continue
            usedPoolIndex.add(idx)
            searchCache[i] = pool[idx]
        }
        var cursor = 0
        for (i in searchCache.indices) {
            if (searchCache[i] != null) continue
            while (cursor < pool.size && cursor in usedPoolIndex) cursor++
            if (cursor < pool.size) {
                searchCache[i] = pool[cursor]
                searchCache[i]?.let {
                    it.chunk = null
                    it.uploaded = false
                }
                usedPoolIndex.add(cursor)
                cursor++
            }
        }
        val searchResults = searchCache.requireNoNulls()
        if (pool.size > searchCache.size) {
            val remaining = pool - searchResults.toSet()
            pool.clear()
            pool.addAll(searchResults)
            pool.addAll(remaining)
        }
        else {
            pool.clear()
            pool.addAll(searchResults)
        }

        for (i in 0 until screenChunkCount) {
            val slot = pool[i]
            if (slot.chunk == null) {
                val chunkStartTick = (startChunk + i) * chunkTickSize.toDouble()
                slot.chunk = NoteChunk(
                    startTick = chunkStartTick,
                    durationTicks = chunkTickSize.toDouble(),
                    id = startChunk + i,
                    entities = chunkBuilder.buildDrawableNotes(chunkStartTick)
                )
            }
        }
    }

    /**
     * 更新GPU的vbo为当前池 只能在GL线程调用
     */
    fun upload(pxPerTick: Float, windowSizePx: Size, roundedFrac: Float) {
        for (i in 0 until screenChunkCount) {
            val slot = pool[i]
            if (!slot.initialized) {
                slot.initialize(quadRect.vbo)
            }
            if (!slot.uploaded) {
                slot.upload(chunkBuilder)
            }
        }
        uniforms.update(pxPerTick, windowSizePx, chunkTickSize.toFloat(), roundedFrac)
    }

    /**
     * GL线程渲染调用 根据当前数据和tick偏移提交绘制命令
     */
    fun draw() {
        for (i in 0 until screenChunkCount) {
            val slot = pool[i]
            uniforms.updateInChunk((currentTick - slot.chunk!!.id * chunkTickSize).toFloat())
            slot.draw()
        }
    }

    /**
     * GL Canvas的init块调用 初始化GL资源
     */
    fun initGL() {
        shader = Shader(Shader.VANILLA_VERTEX, Shader.VANILLA_FRAGMENT)
        shader.use()
        quadRect.init()
        uniforms.initialize(shader.program)
    }

    fun dispose() {
        pool.forEach(InstanceSlot::dispose)
        quadRect.dispose()
        shader.dispose()
    }
}