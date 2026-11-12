package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.geometry.Size
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.Shader
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.Shader.Companion.inject
import kotlin.math.ceil
import kotlin.math.floor

private const val DRAW_SHADOW = "#define DRAW_SHADOW"

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
     * 把覆盖整首 MIDI 的所有 chunk 追加到 pool 末尾，并立即初始化 + 上传。
     * 只能在 GL 线程调用，且必须在 initGL() 之后调用。
     * 幂等：重复调用不会重复创建/重复上传已存在的 chunk。
     *
     * @param totalTick MIDI 的总 tick 长度，用于算出需要预热的 chunk 总数
     */
    fun uploadAll(totalTick: Double) {
        if (totalTick <= 0.0) return
        if (!::shader.isInitialized) return

        val totalChunks = ceil(totalTick / chunkTickSize).toInt().coerceAtLeast(1)

        // 先按 id 建索引，避免 O(totalChunks * pool.size) 的线性查找
        val slotById = HashMap<Int, InstanceSlot>(totalChunks * 2)
        for (slot in pool) {
            slot.chunk?.let { slotById[it.id] = slot }
        }

        for (chunkID in 0 until totalChunks) {
            val slot = slotById.getOrPut(chunkID) {
                val chunkStartTick = chunkID * chunkTickSize.toDouble()
                val newSlot = InstanceSlot(null)
                newSlot.chunk = NoteChunk(
                    startTick = chunkStartTick,
                    durationTicks = chunkTickSize.toDouble(),
                    id = chunkID,
                    entities = chunkBuilder.buildDrawableNotes(chunkStartTick)
                )
                pool.add(newSlot)          // 追加到末尾备用
                newSlot
            }
            if (!slot.initialized) slot.initialize(quadRect.vbo)
            if (!slot.uploaded) slot.upload(chunkBuilder)
            slot.chunk?.entities = null
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
        shader = if (Global.settings.DrawNoteShadow) {
            Shader(Shader.VANILLA_VERTEX.inject(DRAW_SHADOW), Shader.VANILLA_FRAGMENT.inject(DRAW_SHADOW))
        } else {
            Shader(Shader.VANILLA_VERTEX, Shader.VANILLA_FRAGMENT)
        }
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