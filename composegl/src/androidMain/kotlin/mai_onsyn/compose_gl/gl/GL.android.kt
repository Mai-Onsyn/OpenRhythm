package mai_onsyn.compose_gl.gl

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.FloatBuffer

actual fun glCreateShader(type: Int): Int = withGuard { GLES32.glCreateShader(type) }
actual fun glShaderSource(shader: Int, string: CharSequence) = withGuard { GLES32.glShaderSource(shader, string.toString()) }
actual fun glCompileShader(shader: Int) = withGuard { GLES32.glCompileShader(shader) }
actual fun glAttachShader(program: Int, shader: Int) = withGuard { GLES32.glAttachShader(program, shader) }
actual fun glLinkProgram(program: Int) = withGuard { GLES32.glLinkProgram(program) }
actual fun glCreateProgram(): Int = withGuard { GLES32.glCreateProgram() }
actual fun glDeleteShader(shader: Int) = withGuard { GLES32.glDeleteShader(shader) }

actual fun glDeleteBuffers(buffer: Int) = withGuard { GLES32.glDeleteBuffers(1, intArrayOf(buffer), 0) }
actual fun glDeleteBuffers(buffers: IntArray) = withGuard { GLES32.glDeleteBuffers(buffers.size, buffers, 0) }
actual fun glDeleteVertexArrays(array: Int) = withGuard { GLES32.glDeleteVertexArrays(1, intArrayOf(array), 0) }
actual fun glDeleteVertexArrays(arrays: IntArray) = withGuard { GLES32.glDeleteVertexArrays(arrays.size, arrays, 0) }
actual fun glGenVertexArrays(): Int = IntArray(1).apply { GLES32.glGenVertexArrays(1, this, 0) }[0]
actual fun glGenVertexArrays(buffers: IntArray) = withGuard { GLES32.glGenVertexArrays(buffers.size, buffers, 0) }
actual fun glGenBuffers(): Int = IntArray(1).apply { GLES32.glGenBuffers(1, this, 0) }[0]
actual fun glGenBuffers(buffers: IntArray) = withGuard { GLES32.glGenBuffers(buffers.size, buffers, 0) }
actual fun glBindVertexArray(vertexArray: Int) = withGuard { GLES32.glBindVertexArray(vertexArray) }
actual fun glBindBuffer(target: Int, buffer: Int) = withGuard { GLES32.glBindBuffer(target, buffer) }
actual fun glBufferData(target: Int, data: FloatBuffer, usage: Int) = withGuard { GLES32.glBufferData(target, data.remaining(), data, usage) }
actual fun glEnableVertexAttribArray(index: Int) = withGuard { GLES32.glEnableVertexAttribArray(index) }
actual fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = withGuard { GLES32.glVertexAttribPointer(index, size, type, normalized, stride, offset) }
actual fun glVertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) = withGuard { GLES32.glVertexAttribIPointer(index, size, type, stride, offset) }
actual fun glVertexAttribDivisor(index: Int, divisor: Int) = withGuard { GLES32.glVertexAttribDivisor(index, divisor) }
actual fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) = withGuard { GLES32.glClearColor(red, green, blue, alpha) }
actual fun glClear(mask: Int) = withGuard { GLES32.glClear(mask) }
actual fun glUseProgram(program: Int) = withGuard { GLES32.glUseProgram(program) }
actual fun glDrawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int) = withGuard { GLES32.glDrawArraysInstanced(mode, first, count, instanceCount) }
actual fun glEnable(target: Int) = withGuard { GLES32.glEnable(target) }
actual fun glBlendFunc(sfactor: Int, dfactor: Int) = withGuard { GLES32.glBlendFunc(sfactor, dfactor) }
actual fun glBufferData(target: Int, data: ByteBuffer, usage: Int) = withGuard { GLES32.glBufferData(target, data.remaining(), data, usage) }

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
