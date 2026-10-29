package mai_onsyn.compose_gl.internal.android.thread

import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.framePeriodNanos
import mai_onsyn.compose_gl.internal.android.AndroidCallbacks
import mai_onsyn.compose_gl.scope.FrameStats

/**
 * 两种安卓承载模式共用的帧循环骨架。
 *
 * 只负责：帧请求的合并与等待、尺寸变化的分发、帧号与耗时的统计。
 * 上下文怎么建、这一帧怎么呈现，全部交给子类。
 */
internal abstract class FrameLoopThread(
    @Volatile
    var framePolicy: FramePolicy,
    protected val callbacks: AndroidCallbacks,
    protected val scope: MutableGLRenderScope,
    initialWidth: Int,
    initialHeight: Int,
) : Thread("ComposeGL-Render") {

    // wait/notifyAll 是 java.lang.Object 的方法，kotlin.Any 上没有；这里只能用它
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock = Object()
    private var pending = true
    private var stopped = false
    private var pendingWidth = initialWidth
    private var pendingHeight = initialHeight
    private var frameWidth = 0
    private var frameHeight = 0
    private var dropped = 0L

    /** Continuous 模式下的下一帧目标时刻；按"起始时刻 + 周期"计算，避免把耗时叠加到周期上。 */
    private var nextDeadline = 0L

    private var readbackNanosOfLastFrame = 0L

    /** 暂停出帧（保留上下文与资源）。 */
    @Volatile
    var paused: Boolean = false
        set(value) {
            field = value
            synchronized(lock) { lock.notifyAll() }
        }

    /** 最近一帧里"把像素搬回 CPU"所花的时间，由子类通过 [reportReadback] 写入。 */
    protected val lastReadbackNanos: Long get() = readbackNanosOfLastFrame

    protected fun reportReadback(nanos: Long) {
        readbackNanosOfLastFrame = nanos
    }

    @Volatile
    var density: Float = 1f

    /** 屏幕刷新率；`Continuous(maxFps = 0)` 时用它定周期，120Hz 屏不会被钉在 60。 */
    @Volatile
    var refreshRateHz: Float = 60f

    protected val isStopped: Boolean
        get() = synchronized(lock) { stopped }

    fun resize(width: Int, height: Int) = synchronized(lock) {
        pendingWidth = width
        pendingHeight = height
        pending = true
        lock.notifyAll()
    }

    fun requestRender() = synchronized(lock) {
        pending = true
        lock.notifyAll()
    }

    fun stopGracefully() {
        synchronized(lock) {
            stopped = true
            pending = false
            lock.notifyAll()
        }
        join(1500)
    }

    final override fun run() {
        try {
            onStart()

            var lastWidth = -1
            var lastHeight = -1

            while (true) {
                if (!awaitFrame()) break
                if (paused) {
                    dropped++
                    continue
                }
                val width = frameWidth
                val height = frameHeight
                if (width <= 0 || height <= 0) {
                    dropped++
                    continue
                }

                if (width != lastWidth || height != lastHeight) {
                    lastWidth = width
                    lastHeight = height
                    scope.w = width
                    scope.h = height
                    onResize(width, height)
                    callbacks.resized(scope, IntSize(width, height))
                }

                scope.frame += 1
                val frameStart = System.nanoTime()
                scope.frameTime = frameStart

                beforeDraw(width, height)

                val drawStart = System.nanoTime()
                callbacks.draw(scope)
                val drawEnd = System.nanoTime()

                present(width, height)
                val presentEnd = System.nanoTime()

                if (framePolicy is FramePolicy.Continuous) {
                    // 以"本帧开始时刻"为基准推进，耗时不会累积到周期里
                    nextDeadline = frameStart + periodNanos()
                }

                val presentTotal = presentEnd - drawEnd
                val readbackNanos = lastReadbackNanos

                scope.statsValue = FrameStats(
                    frameIndex = scope.frame,
                    drawNanos = drawEnd - drawStart,
                    readbackNanos = readbackNanos,
                    presentNanos = (presentTotal - readbackNanos).coerceAtLeast(0),
                    droppedFrames = dropped,
                )
            }

            // 上下文是常驻的（活过画布的挂载周期），这里不触发 destroyed：
            // 用户的 GL 资源依然有效，删了反而下次挂载要重新编译。
        } catch (t: Throwable) {
            if (!isStopped) {
                callbacks.failure(
                    t as? GLException ?: GLException("安卓渲染线程异常：${t.message}", t),
                )
            }
        } finally {
            try {
                onStop()
            } catch (_: Throwable) {
            }
        }
    }

    /** 建上下文。执行前 scope 尚未就绪，执行完必须填好 [MutableGLRenderScope.infoValue] 再回调 `created`。 */
    protected abstract fun onStart()

    /** 尺寸变化时重建与尺寸相关的资源。 */
    protected open fun onResize(width: Int, height: Int) = Unit

    /** 每帧绘制之前调用：绑定渲染目标、设置视口。**不得清屏**。 */
    protected open fun beforeDraw(width: Int, height: Int) = Unit

    /** 把这一帧送到屏幕（或回读成位图）。 */
    protected abstract fun present(width: Int, height: Int)

    /** 释放资源。 */
    protected abstract fun onStop()

    private fun awaitFrame(): Boolean = synchronized(lock) {
        if (framePolicy is FramePolicy.Continuous) {
            if (!stopped && !pending) {
                val waitNanos = nextDeadline - System.nanoTime()
                if (waitNanos > 0) {
                    lock.wait(waitNanos / 1_000_000, (waitNanos % 1_000_000).toInt())
                }
            }
            pending = true
        } else {
            while (!pending && !stopped) lock.wait()
        }
        if (stopped) return false
        pending = false
        // 复用字段而不是每帧新建 IntArray：帧循环里不该有堆分配
        frameWidth = pendingWidth
        frameHeight = pendingHeight
        true
    }

    private fun periodNanos(): Long {
        val policy = framePolicy as? FramePolicy.Continuous ?: return 0
        val fps = if (policy.maxFps > 0) policy.maxFps.toFloat() else refreshRateHz
        return framePeriodNanos(fps)
    }

}
