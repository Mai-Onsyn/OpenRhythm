package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import kotlinx.coroutines.runBlocking
import mai_onsyn.compose_gl.gl.*
import openrhythm.sharedui.generated.resources.Res
import org.lwjgl.opengl.GL46C

class Shader(vertexSource: String, fragmentSource: String) {
    val program: Int

    init {
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexSource)
        glCompileShader(vertexShader)

        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentSource)
        glCompileShader(fragmentShader)

        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }

    fun use() = glUseProgram(program)

    fun dispose() {
        GL46C.glDeleteProgram(program)
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
