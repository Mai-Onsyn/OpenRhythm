package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import mai_onsyn.compose_gl.GLCanvas
//import org.lwjgl.opengl.GL46C.*
import mai_onsyn.compose_gl.gl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

var initialized = false
var instancedVao = 0
var instancedVbo = 0
var quadVbo = 0

@Composable
fun TestRectsDraw() {
    val rects = listOf(
        ToDrawRect(Rect(-0.5f, 0.5f, 0.4f, 0.4f), Color.Red),
        ToDrawRect(Rect(0.1f, 0.1f, 2f, 0.3f), Color.Green)
    )
    GLCanvas(
        modifier = Modifier.fillMaxSize().background(Color(0x2f00ff20)),
        onSurfaceDestroyed = {
            glDeleteBuffers(intArrayOf(instancedVbo, quadVbo))
            glDeleteVertexArrays(instancedVao)
            initialized = false
            Logger.d { "GLCanvas#destroy" }
        },
        onSurfaceResized = { old, new -> println("Resized to $new") },
        onDraw = {
            val program = Shader.VANILLA.program
            if (!initialized) {
                val quad = floatArrayOf(
                    0f, 0f,  1f, 0f,  0f, 1f,
                    0f, 1f,  1f, 0f,  1f, 1f
                )

                val vao = glGenVertexArrays()
                val vbo = IntArray(2).apply { glGenBuffers(this) }

                glBindVertexArray(vao)
                val qBuf = ByteBuffer
                    .allocateDirect(quad.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer().put(quad)
                    .apply { position(0) }

                // 矩形 (Location = 0)
                glBindBuffer(GL_ARRAY_BUFFER, vbo[0])
                glBufferData(GL_ARRAY_BUFFER, qBuf, GL_STATIC_DRAW)
                glEnableVertexAttribArray(0)
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0)

                glBindBuffer(GL_ARRAY_BUFFER, vbo[1])

                val stride = 8 * 4

                // [x, y, w, h] (Location = 1)
                glEnableVertexAttribArray(1)
                glVertexAttribPointer(1, 4, GL_FLOAT, false, stride, 0)
                glVertexAttribDivisor(1, 1)

                // [r, g, b, a] (Location = 2)
                glEnableVertexAttribArray(2)
                glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, 4 * 4)
                glVertexAttribDivisor(2, 1)

                glBindVertexArray(0)
                instancedVao = vao
                quadVbo = vbo[0]
                instancedVbo = vbo[1]
            }

            val buffer = ByteBuffer
                .allocateDirect(rects.size * 8 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            for (item in rects) {
                buffer.put(item.rect.left)
                buffer.put(item.rect.top)
                buffer.put(item.rect.width)
                buffer.put(item.rect.height)
                buffer.put(item.color.red)
                buffer.put(item.color.green)
                buffer.put(item.color.blue)
                buffer.put(item.color.alpha)
            }
            buffer.flip()

            glBindBuffer(GL_ARRAY_BUFFER, instancedVbo)
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW)

            glClearColor(0f, 0f, 0f, 0f)
            glClear(GL_COLOR_BUFFER_BIT)

            glUseProgram(program)
            glBindVertexArray(instancedVao)
            glDrawArraysInstanced(GL_TRIANGLES, 0, 6, rects.size)
            glBindVertexArray(0)
        }
    )
}
