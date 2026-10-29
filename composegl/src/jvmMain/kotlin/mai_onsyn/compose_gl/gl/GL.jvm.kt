package mai_onsyn.compose_gl.gl

import org.lwjgl.opengl.GL46C
import java.nio.FloatBuffer

actual fun glCreateShader(type: Int): Int = GL46C.glCreateShader(type)
actual fun glShaderSource(shader: Int, string: CharSequence) = GL46C.glShaderSource(shader, string)
actual fun glCompileShader(shader: Int) = GL46C.glCompileShader(shader)
actual fun glAttachShader(program: Int, shader: Int) = GL46C.glAttachShader(program, shader)
actual fun glLinkProgram(program: Int) = GL46C.glLinkProgram(program)
actual fun glCreateProgram(): Int = GL46C.glCreateProgram()
actual fun glDeleteShader(shader: Int) = GL46C.glDeleteShader(shader)

actual fun glDeleteBuffers(buffer: Int) = GL46C.glDeleteBuffers(buffer)
actual fun glDeleteBuffers(buffers: IntArray) = GL46C.glDeleteBuffers(buffers)
actual fun glDeleteVertexArrays(array: Int) = GL46C.glDeleteVertexArrays(array)
actual fun glDeleteVertexArrays(arrays: IntArray) = GL46C.glDeleteVertexArrays(arrays)
actual fun glGenVertexArrays(): Int = GL46C.glGenVertexArrays()
actual fun glGenVertexArrays(buffers: IntArray) = GL46C.glGenVertexArrays(buffers)
actual fun glGenBuffers(): Int = GL46C.glGenBuffers()
actual fun glGenBuffers(buffers: IntArray) = GL46C.glGenBuffers(buffers)
actual fun glBindVertexArray(vertexArray: Int) = GL46C.glBindVertexArray(vertexArray)
actual fun glBindBuffer(target: Int, buffer: Int) = GL46C.glBindBuffer(target, buffer)
actual fun glBufferData(target: Int, data: FloatBuffer, usage: Int) = GL46C.glBufferData(target, data, usage)
actual fun glEnableVertexAttribArray(index: Int) = GL46C.glEnableVertexAttribArray(index)
actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = GL46C.glVertexAttribPointer(index, size, type, normalized, stride, offset.toLong())
actual fun glVertexAttribDivisor(index: Int, divisor: Int) = GL46C.glVertexAttribDivisor(index, divisor)
actual fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) = GL46C.glClearColor(red, green, blue, alpha)
actual fun glClear(mask: Int) = GL46C.glClear(mask)
actual fun glUseProgram(program: Int) = GL46C.glUseProgram(program)
actual fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) = GL46C.glDrawArraysInstanced(mode, first, count, instanceCount)