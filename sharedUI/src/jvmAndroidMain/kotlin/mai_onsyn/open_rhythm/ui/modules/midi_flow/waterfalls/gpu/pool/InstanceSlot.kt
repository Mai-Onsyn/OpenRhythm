package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import mai_onsyn.compose_gl.gl.*
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.ToDrawRect
import java.nio.ByteBuffer
import java.nio.ByteOrder

class InstanceSlot(
    var chunk: NoteChunk?
) {
    var vao: Int = 0
    var vbo: Int = 0
    var uploaded: Boolean = false
    var initialized: Boolean = false

    companion object {
        val INSTANCE_STRIDE = 4 * 5

        val NULL_RECT = ToDrawRect(0f, 0f, 0f, 0f, 0, 0)
    }

    // 创建vbo和vao
    fun initialize(quadVbo: Int) {
        if (quadVbo == 0) error("quad vbo not initialized — initGL missed?")

        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        glBindVertexArray(vao)

        // 单位四边形
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0)

        // [xFrac, wFrac, tickInChunk, durationTick]
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 4, GL_FLOAT, false, INSTANCE_STRIDE, 0)
        glVertexAttribDivisor(1, 1)

        // 颜色argb
        glEnableVertexAttribArray(2)
        glVertexAttribIPointer(2, 4, GL_UNSIGNED_BYTE, INSTANCE_STRIDE, 16)
        glVertexAttribDivisor(2, 1)

        // 深度
//        glEnableVertexAttribArray(3)
//        glVertexAttribIPointer(3, 1, GL_UNSIGNED_INT, INSTANCE_STRIDE, 20)
//        glVertexAttribDivisor(3, 1)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        initialized = true
    }

    // 复用已有vbo
    fun upload(builder: NoteChunkBuilder) {
        if (uploaded || chunk == null) return

        val buffer = ByteBuffer
            .allocateDirect(chunk!!.entities.size * INSTANCE_STRIDE)
            .order(ByteOrder.nativeOrder())

        for (e in chunk!!.entities) {
            val (fracX, fracW, tickInChunk, durationTick, color, depth) = builder.buildGLRect(chunk!!.id, e) ?: NULL_RECT
            buffer.putFloat(fracX)
            buffer.putFloat(fracW)
            buffer.putFloat(tickInChunk)
            buffer.putFloat(durationTick)
            buffer.putInt(color)
//            buffer.putInt(depth)
        }
        buffer.flip()

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        uploaded = true
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, chunk!!.entities.size)
        glBindVertexArray(0)
    }

    // 完全删除vbo 删除后这个变量必须再次initialize
    fun dispose() {
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }

    override fun toString(): String {
        return "InstanceSlot($chunk, uploaded=$uploaded, vbo=$vbo)"
    }
}