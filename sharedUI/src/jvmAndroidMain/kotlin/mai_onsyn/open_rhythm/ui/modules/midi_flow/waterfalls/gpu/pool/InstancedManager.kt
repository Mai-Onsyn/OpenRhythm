package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import kotlin.math.floor

class InstancedManager(
    val pool: MutableList<InstanceSlot>,
    val chunkTickSize: Int,
    var currentTick: Double,
    var screenHeightTick: Int,
    val pxPerTick: Float
) {
    var pixelOffset: Float = 0f
    /**
     * 输入tick，根据tick来轮换vbo指针，只在GL线程调用
     */
    fun update(tick: Double) {
        val oldStartChunk = floor(currentTick / chunkTickSize).toInt()
        val newStartChunk = floor(tick / chunkTickSize).toInt()
        currentTick = tick
        val chunkOffset = newStartChunk - oldStartChunk

        val screenChunkCount = screenHeightTick / chunkTickSize + 2
        (screenChunkCount - pool.size).let { difference ->
            if (difference > 0) repeat(difference) {
                pool.add(InstanceSlot(0, null, false))
            }
        }
    }

    /**
     * GL线程渲染调用 根据当前数据和tick偏移提交绘制命令
     */
    fun draw() {
        TODO()
    }
}