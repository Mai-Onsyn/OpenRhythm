package mai_onsyn.compose_gl.gl

import java.nio.FloatBuffer

expect fun glCreateShader(type: Int): Int
expect fun glShaderSource(shader: Int, string: CharSequence)
expect fun glCompileShader(shader: Int)
expect fun glAttachShader(program: Int, shader: Int)
expect fun glLinkProgram(program: Int)
expect fun glCreateProgram(): Int
expect fun glDeleteShader(shader: Int)

expect fun glDeleteBuffers(buffer: Int)
expect fun glDeleteBuffers(buffers: IntArray)
expect fun glDeleteVertexArrays(array: Int)
expect fun glDeleteVertexArrays(arrays: IntArray)
expect fun glGenVertexArrays(): Int
expect fun glGenVertexArrays(buffers: IntArray)
expect fun glGenBuffers(): Int
expect fun glGenBuffers(buffers: IntArray)
expect fun glBindVertexArray(vertexArray: Int)
expect fun glBindBuffer(target: Int, buffer: Int)
expect fun glBufferData(target: Int, data: FloatBuffer, usage: Int)
expect fun glEnableVertexAttribArray(index: Int)
expect fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int)
expect fun glVertexAttribDivisor(index: Int, divisor: Int)
expect fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)
expect fun glClear(mask: Int)
expect fun glUseProgram(program: Int)
expect fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int)