package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import mai_onsyn.compose_gl.gl.GL_ARRAY_BUFFER
import mai_onsyn.compose_gl.gl.GL_STATIC_DRAW
import mai_onsyn.compose_gl.gl.glBindBuffer
import mai_onsyn.compose_gl.gl.glBufferData
import mai_onsyn.compose_gl.gl.glDeleteBuffers
import mai_onsyn.compose_gl.gl.glGenBuffers
import java.nio.ByteBuffer
import java.nio.ByteOrder

class QuadRect {
    var vbo = 0
        private set

    fun init() {
        if (vbo != 0) throw IllegalStateException("QuadRect vbo is already initialized")
        val quad = floatArrayOf(
            0f, 0f,  1f, 0f,  0f, 1f,
            0f, 1f,  1f, 0f,  1f, 1f
        )
        val qBuf = ByteBuffer
            .allocateDirect(quad.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(quad)
            .apply { position(0) }

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, qBuf, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun dispose() {
        glDeleteBuffers(vbo)
        vbo = 0
    }
}