package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import mai_onsyn.compose_gl.gl.*
import openrhythm.sharedui.generated.resources.Res

class Shader(vertexSource: String, fragmentSource: String) {
    val program: Int

    init {
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexSource)
        glCompileShader(vertexShader)

        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentSource)
        glCompileShader(fragmentShader)

        val vertexStatus = IntArray(1)
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, vertexStatus)
        Logger.e { vertexStatus[0].let { "Vertex shader compiled: $it" } }
        if (vertexStatus[0] == 0) {
            val errorLog1 = glGetShaderInfoLog(vertexShader)
            Logger.e { "Vertex shader compile failed: $errorLog1" }
        }
        val fragmentStatus = IntArray(1)
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, fragmentStatus)
        Logger.e { fragmentStatus[0].let { "Fragment shader compiled: $it" } }
        if (fragmentStatus[0] == 0) {
            val errorLog2 = glGetShaderInfoLog(fragmentShader)
            Logger.e { "Fragment shader compile failed: $errorLog2" }
        }

        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }

    fun use() = glUseProgram(program)

    fun dispose() {
        glDeleteProgram(program)
        glUseProgram(0)
    }

    companion object {
        @Deprecated("TestOnly")
        val VANILLA = runBlocking { Shader(
            String(Res.readBytes("files/shaders/vanilla-vertex.glsl")),
            String(Res.readBytes("files/shaders/vanilla-fragment.glsl"))
        ) }

        val VANILLA_VERTEX = runBlocking { String(Res.readBytes("files/shaders/vanilla-vertex.glsl")) }
        val VANILLA_FRAGMENT = runBlocking { String(Res.readBytes("files/shaders/vanilla-fragment.glsl")) }
    }
}
