package mai_onsyn.compose_gl.gl

import android.opengl.GLES32
import java.nio.FloatBuffer

actual fun glCreateShader(type: Int): Int = GLES32.glCreateShader(type)
actual fun glShaderSource(shader: Int, string: CharSequence) = GLES32.glShaderSource(shader, string.toString())
actual fun glCompileShader(shader: Int) = GLES32.glCompileShader(shader)
actual fun glAttachShader(program: Int, shader: Int) = GLES32.glAttachShader(program, shader)
actual fun glLinkProgram(program: Int) = GLES32.glLinkProgram(program)
actual fun glCreateProgram(): Int = GLES32.glCreateProgram()
actual fun glDeleteShader(shader: Int) = GLES32.glDeleteShader(shader)

actual fun glDeleteBuffers(buffer: Int) = GLES32.glDeleteBuffers(1, intArrayOf(buffer), 0)
actual fun glDeleteBuffers(buffers: IntArray) = GLES32.glDeleteBuffers(buffers.size, buffers, 0)
actual fun glDeleteVertexArrays(array: Int) = GLES32.glDeleteVertexArrays(1, intArrayOf(array), 0)
actual fun glDeleteVertexArrays(arrays: IntArray) = GLES32.glDeleteVertexArrays(arrays.size, arrays, 0)
actual fun glGenVertexArrays(): Int = IntArray(1).apply { GLES32.glGenVertexArrays(1, this, 0) }[0]
actual fun glGenVertexArrays(buffers: IntArray) = GLES32.glGenVertexArrays(buffers.size, buffers, 0)
actual fun glGenBuffers(): Int = IntArray(1).apply { GLES32.glGenBuffers(1, this, 0) }[0]
actual fun glGenBuffers(buffers: IntArray) = GLES32.glGenBuffers(buffers.size, buffers, 0)
actual fun glBindVertexArray(vertexArray: Int) = GLES32.glBindVertexArray(vertexArray)
actual fun glBindBuffer(target: Int, buffer: Int) = GLES32.glBindBuffer(target, buffer)
actual fun glBufferData(target: Int, data: FloatBuffer, usage: Int) = GLES32.glBufferData(target, data.remaining() * 4, data, usage)
actual fun glEnableVertexAttribArray(index: Int) = GLES32.glEnableVertexAttribArray(index)
actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = GLES32.glVertexAttribPointer(index, size, type, normalized, stride, offset)
actual fun glVertexAttribDivisor(index: Int, divisor: Int) = GLES32.glVertexAttribDivisor(index, divisor)
actual fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) = GLES32.glClearColor(red, green, blue, alpha)
actual fun glClear(mask: Int) = GLES32.glClear(mask)
actual fun glUseProgram(program: Int) = GLES32.glUseProgram(program)
actual fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) = GLES32.glDrawArraysInstanced(mode, first, count, instanceCount)