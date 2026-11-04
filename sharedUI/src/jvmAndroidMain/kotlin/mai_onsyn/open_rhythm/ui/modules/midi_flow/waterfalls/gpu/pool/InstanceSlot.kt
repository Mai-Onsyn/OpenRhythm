package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import mai_onsyn.compose_gl.gl.GL_ARRAY_BUFFER
import mai_onsyn.compose_gl.gl.GL_FLOAT
import mai_onsyn.compose_gl.gl.GL_UNSIGNED_INT
import mai_onsyn.compose_gl.gl.glBindBuffer
import mai_onsyn.compose_gl.gl.glBindVertexArray
import mai_onsyn.compose_gl.gl.glDeleteBuffers
import mai_onsyn.compose_gl.gl.glDeleteVertexArrays
import mai_onsyn.compose_gl.gl.glEnableVertexAttribArray
import mai_onsyn.compose_gl.gl.glGenBuffers
import mai_onsyn.compose_gl.gl.glGenVertexArrays
import mai_onsyn.compose_gl.gl.glVertexAttribDivisor
import mai_onsyn.compose_gl.gl.glVertexAttribIPointer
import mai_onsyn.compose_gl.gl.glVertexAttribPointer

class InstanceSlot(
    var chunk: NoteChunk?
) {
    var vao: Int = 0
    var vbo: Int = 0
    var uploaded: Boolean = false
    var initialized: Boolean = false

    companion object {
        val INSTANCE_STRIDE = 4 * 6
    }

    // 创建vbo和vao
    fun initialize(quadVbo: Int) {
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

        // 颜色rgba
        glEnableVertexAttribArray(2)
        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, INSTANCE_STRIDE, 16)
        glVertexAttribDivisor(2, 1)

        // 深度
        glEnableVertexAttribArray(3)
        glVertexAttribIPointer(3, 1, GL_UNSIGNED_INT, INSTANCE_STRIDE, 20)
        glVertexAttribDivisor(3, 1)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        initialized = true
    }

    // 复用已有vbo
    fun upload() {
        uploaded = true
        TODO()
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