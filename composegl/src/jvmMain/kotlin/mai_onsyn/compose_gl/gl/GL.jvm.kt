package mai_onsyn.compose_gl.gl

import org.lwjgl.opengl.GL46C
import java.nio.ByteBuffer
import java.nio.FloatBuffer

actual fun glCreateShader(type: Int): Int = withGuard { GL46C.glCreateShader(type) }
actual fun glShaderSource(shader: Int, string: CharSequence) = withGuard { GL46C.glShaderSource(shader, string) }
actual fun glCompileShader(shader: Int) = withGuard { GL46C.glCompileShader(shader) }
actual fun glAttachShader(program: Int, shader: Int) = withGuard { GL46C.glAttachShader(program, shader) }
actual fun glLinkProgram(program: Int) = withGuard { GL46C.glLinkProgram(program) }
actual fun glCreateProgram(): Int = withGuard { GL46C.glCreateProgram() }
actual fun glDeleteShader(shader: Int) = withGuard { GL46C.glDeleteShader(shader) }

actual fun glDeleteBuffers(buffer: Int) = withGuard { GL46C.glDeleteBuffers(buffer) }
actual fun glDeleteBuffers(buffers: IntArray) = withGuard { GL46C.glDeleteBuffers(buffers) }
actual fun glDeleteVertexArrays(array: Int) = withGuard { GL46C.glDeleteVertexArrays(array) }
actual fun glDeleteVertexArrays(arrays: IntArray) = withGuard { GL46C.glDeleteVertexArrays(arrays) }
actual fun glGenVertexArrays(): Int = withGuard { GL46C.glGenVertexArrays() }
actual fun glGenVertexArrays(buffers: IntArray) = withGuard { GL46C.glGenVertexArrays(buffers) }
actual fun glGenBuffers(): Int = withGuard { GL46C.glGenBuffers() }
actual fun glGenBuffers(buffers: IntArray) = withGuard { GL46C.glGenBuffers(buffers) }
actual fun glBindVertexArray(vertexArray: Int) = withGuard { GL46C.glBindVertexArray(vertexArray) }
actual fun glBindBuffer(target: Int, buffer: Int) = withGuard { GL46C.glBindBuffer(target, buffer) }
actual fun glBufferData(target: Int, data: FloatBuffer, usage: Int) = withGuard { GL46C.glBufferData(target, data, usage) }
actual fun glEnableVertexAttribArray(index: Int) = withGuard { GL46C.glEnableVertexAttribArray(index) }
actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = withGuard { GL46C.glVertexAttribPointer(index, size, type, normalized, stride, offset.toLong()) }
actual fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) = withGuard { GL46C.glVertexAttribIPointer(index, size, type, stride, offset.toLong()) }
actual fun glVertexAttribDivisor(index: Int, divisor: Int) = withGuard { GL46C.glVertexAttribDivisor(index, divisor) }
actual fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) = withGuard { GL46C.glClearColor(red, green, blue, alpha) }
actual fun glClear(mask: Int) = withGuard { GL46C.glClear(mask) }
actual fun glUseProgram(program: Int) = withGuard { GL46C.glUseProgram(program) }
actual fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) = withGuard { GL46C.glDrawArraysInstanced(mode, first, count, instanceCount) }
actual fun glEnable(target: Int) = withGuard { GL46C.glEnable(target) }
actual fun glBlendFunc(sfactor: Int, dfactor: Int) = withGuard { GL46C.glBlendFunc(sfactor, dfactor) }
actual fun glBufferData(target: Int, data: ByteBuffer, usage: Int) = withGuard { GL46C.glBufferData(target, data, usage) }
actual fun glDeleteProgram(program: Int) = GL46C.glDeleteProgram(program)

/**
 * 每个 gl* 调用先过这里：确认当前线程是渲染线程。
 *
 * 在没有 GL 上下文的线程（例如 Compose 的 `remember` / `DisposableEffect`、EDT）上调用
 * `glDeleteBuffers` 之类，驱动会解引用空上下文 → `0xC0000005`，没有任何 Java 堆栈。
 * 这里提前抛异常，把"偶发崩溃"变成"一眼能看懂的错误"。
 */
private inline fun <T> withGuard(block: () -> T): T {
    GLThreadGuard.check()
    return block()
}

/** 渲染线程标记。用 ThreadLocal 而不是单个引用：桌面一个会话一条线程，安卓一块画布一条线程。 */
internal object GLThreadGuard {
    private val active = ThreadLocal.withInitial { false }

    fun enter() = active.set(true)

    fun exit() = active.set(false)

    fun check() {
        if (active.get() != true) {
            throw IllegalStateException(
                "GL 调用必须发生在渲染线程上（当前线程：${Thread.currentThread().name}）。" +
                    "在 Compose 的 remember/DisposableEffect 里直接调用 gl* 没有 current 上下文，" +
                    "会触发 0xC0000005。请把资源创建/释放放进 GLCanvas 的 onCanvasCreated / " +
                    "onCanvasDisposed，或 ComposeGL.onContextCreated / onContextDestroyed。",
            )
        }
    }
}

actual fun glGetUniformLocation(program: Int, name: String): Int = GL46C.glGetUniformLocation(program, name)
actual fun glUniform1f(program: Int, value: Float) = GL46C.glUniform1f(program, value)
actual fun glUniform2f(program: Int, v1: Float, v2: Float) = GL46C.glUniform2f(program, v1, v2)
actual fun glGetShaderiv(shader: Int, pname: Int, params: IntArray) = GL46C.glGetShaderiv(shader, pname, params)
actual fun glGetShaderInfoLog(shader: Int): String = GL46C.glGetShaderInfoLog(shader)