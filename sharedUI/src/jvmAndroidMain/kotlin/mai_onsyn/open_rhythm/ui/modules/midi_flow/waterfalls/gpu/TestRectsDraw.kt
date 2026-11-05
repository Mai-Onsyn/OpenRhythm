package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.gl.*
import org.lwjgl.opengl.GL30
import java.nio.ByteBuffer
import java.nio.ByteOrder

var initialized = false
var instancedVao = 0
var instancedVbo = 0
var quadVbo = 0

@Composable
fun TestRectsDraw() {
    val rects = listOf(
        _ToDrawRect(Rect(0.1f, 0.1f, 2f, 0.3f), Color.Green.copy(0.9f)),
        _ToDrawRect(Rect(-0.5f, 0.5f, 0.4f, -0.4f), Color.Red.copy(0.8f)),
    )
    GLCanvas(
        modifier = Modifier.fillMaxSize(),
        onSurfaceDestroyed = {
            glDeleteBuffers(intArrayOf(instancedVbo, quadVbo))
            glDeleteVertexArrays(instancedVao)
            initialized = false
            Logger.d { "GLCanvas#destroy" }
        },
        onSurfaceCreated = {
            GL30.glEnable(GL30.GL_BLEND)
            GL30.glEnable(GL30.GL_DEPTH_TEST)
            GL30.glBlendFunc(GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA)

//            GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        },
        onSurfaceResized = { old, new -> println("Resized to $new") },
        framePolicy = FramePolicy.Continuous(60),
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

//            glClearColor(0f, 0f, 0f, 0f)
//            glClear(GL_COLOR_BUFFER_BIT)

            val clearAlpha = 0.1f
            GL30.glClearColor(1f * clearAlpha, 0f * clearAlpha, 0.5f * clearAlpha, clearAlpha)
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT)

            glUseProgram(program)
            glBindVertexArray(instancedVao)
            glDrawArraysInstanced(GL_TRIANGLES, 0, 6, rects.size)
            glBindVertexArray(0)
        }
    )
}
