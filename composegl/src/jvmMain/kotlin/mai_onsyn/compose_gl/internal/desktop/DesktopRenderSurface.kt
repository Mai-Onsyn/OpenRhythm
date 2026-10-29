package mai_onsyn.compose_gl.internal.desktop

import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.config.contextSignature
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.framePeriodNanos
import mai_onsyn.compose_gl.internal.desktop.compose.ComposeImageHost
import mai_onsyn.compose_gl.internal.desktop.context.GlfwSession
import mai_onsyn.compose_gl.internal.desktop.context.SessionClient
import mai_onsyn.compose_gl.internal.desktop.framebuffer.Framebuffer
import mai_onsyn.compose_gl.internal.desktop.readback.FrameReader
import mai_onsyn.compose_gl.internal.desktop.readback.SkiaFrameBuffer
import mai_onsyn.compose_gl.scope.FrameStats
import mai_onsyn.compose_gl.scope.GLRenderScope
import org.lwjgl.opengl.GL11

/** 用户回调的持有者。用可变字段承接每次重组的 lambda，避免过期闭包。 */
internal class DesktopCallbacks {
    var created: (GLRenderScope) -> Unit = {}
    var resized: (GLRenderScope, IntSize) -> Unit = { _, _ -> }
    var draw: (GLRenderScope) -> Unit = {}
    var destroyed: (GLRenderScope) -> Unit = {}
    var failure: (GLException) -> Unit = {}
}

/**
 * 桌面端编排：起渲染线程 -> 建上下文 -> 循环取帧 -> 画 -> 回读 -> 发布。
 *
 * 本类**不直接调用任何 gl* 函数**，具体动作全部委托给 GlfwContext / Framebuffer / FrameReader。
 */
internal class DesktopRenderSurface(
    private val config: GLConfig,
    sessionKey: String,
    framePolicy: FramePolicy,
    private val host: ComposeImageHost,
    val callbacks: DesktopCallbacks,
    refreshRateHz: Float = 60f,
) : SessionClient {

    /** 显示器刷新率；`Continuous(maxFps = 0)` 时用它定周期。 */
    @Volatile
    var refreshRateHz: Float = refreshRateHz
        set(value) {
            field = value
            scope.refreshRateValue = value
        }

    /**
     * 帧策略可在运行中切换。只改字段不够 —— 线程可能正阻塞在 `lock.wait()` 上，
     * 必须顺带唤醒一次，否则它会一直等下去（表现为"切了连续渲染但画面不动"）。
     */
    @Volatile
    var framePolicy: FramePolicy = framePolicy
        set(value) {
            field = value
            requestRender()
        }

    /**
     * 暂停出帧（保留上下文与资源）。用于窗口不可见/应用退到后台时省电。
     * 只影响"要不要画"，不影响资源生命周期。
     */
    @Volatile
    var paused: Boolean = false
        set(value) {
            field = value
            synchronized(lock) { lock.notifyAll() }
        }

    private val scope = MutableGLRenderScope()
    // wait/notifyAll 是 java.lang.Object 的方法，kotlin.Any 上没有；这里只能用它
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock = Object()

    private var pending = false
    private var stopped = false
    private var pendingWidth = 0
    private var pendingHeight = 0
    private var frameWidth = 0
    private var frameHeight = 0
    private var dropped = 0L

    /** Continuous 模式下的下一帧目标时刻；按"起始时刻 + 周期"计算，避免把耗时叠加到周期上。 */
    private var nextDeadline = 0L

    /** 距上次主动回收累计发布的像素字节数。 */
    private var nativeBytesSinceGc = 0L

    @Volatile
    var density: Float = 1f
        set(value) {
            field = value
            scope.densityValue = value
        }

    /** 常驻会话：上下文活过画布的挂载周期。 */
    private val session = GlfwSession.obtain(
        "$sessionKey|${config.contextSignature()}",
        config,
    )

    /** 本画布的循环是否已经结束（会话线程退出时置位）。 */
    // wait/notifyAll 是 java.lang.Object 的方法，kotlin.Any 上没有
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val finishedLock = Object()
    private var loopFinished = false

    fun start() {
        session.attach(this)
    }

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

    fun dispose() {
        synchronized(lock) {
            stopped = true
            pending = false
            lock.notifyAll()
        }
        // 等会话线程把本画布的循环跑完（GL 资源必须在会话线程上释放）
        synchronized(finishedLock) {
            var waited = 0
            while (!loopFinished && waited < 2000) {
                finishedLock.wait(50)
                waited += 50
            }
        }
    }

    // ---------------------------------------------------------------- 渲染线程

    override fun runOnSessionThread(session: GlfwSession, isFirstContextUse: Boolean) {
        val framebuffer = Framebuffer()
        val reader = FrameReader(SkiaFrameBuffer())
        try {
            session.glInfo?.let { scope.infoValue = it }
            scope.requestHandler = { requestRender() }
            scope.densityValue = density
            scope.refreshRateValue = refreshRateHz
            // 上下文是常驻的：只有真正创建上下文的那一次才触发，
            // 之后重新挂载时用户的着色器等资源依然有效，不用重新编译
            if (isFirstContextUse) {
                callbacks.created(scope)
                session.onContextDestroy = { callbacks.destroyed(scope) }
            }

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
                    framebuffer.resize(config, width, height)
                    lastWidth = width
                    lastHeight = height
                    scope.w = width
                    scope.h = height
                    callbacks.resized(scope, IntSize(width, height))
                }

                scope.frame += 1
                val frameStart = System.nanoTime()
                scope.frameTime = frameStart

                framebuffer.bind()

                val drawStart = System.nanoTime()
                callbacks.draw(scope)
                val drawEnd = System.nanoTime()

                // MSAA 时把多重采样结果解析到单采样纹理，之后的回读才拿得到像素
                framebuffer.resolve()
                GL11.glFlush()
                val image = reader.read(width, height)
                val readEnd = System.nanoTime()

                host.publish(image)
                val presentEnd = System.nanoTime()

                reclaimNativeIfNeeded(reader, width, height)

                if (framePolicy is FramePolicy.Continuous) {
                    // 以"本帧开始时刻"为基准推进，耗时不会累积到周期里
                    nextDeadline = frameStart + periodNanos()
                }

                scope.statsValue = FrameStats(
                    frameIndex = scope.frame,
                    drawNanos = drawEnd - drawStart,
                    readbackNanos = readEnd - drawEnd,
                    presentNanos = presentEnd - readEnd,
                    droppedFrames = dropped,
                )
            }

        } catch (t: Throwable) {
            if (!stopped) {
                callbacks.failure(
                    t as? GLException ?: GLException("桌面渲染线程异常：${t.message}", t),
                )
            }
        } finally {
            reader.dispose()
            framebuffer.dispose()
            synchronized(finishedLock) {
                loopFinished = true
                finishedLock.notifyAll()
            }
        }
    }

    /** 阻塞直到有帧请求；返回 [width, height]，线程被要求停止时返回 null。 */
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
            while (!pending && !stopped) {
                lock.wait()
            }
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

    /**
     * 主动回收每帧产生的原生内存。
     *
     * 为什么必须这么做：Compose 桌面端画一帧更新过的位图，内部会为这一帧新建一个 Skia Image
     * 并把像素作为纹理上传（D3D12 后端）。这部分内存是**原生**的，JVM 堆完全看不见，
     * 所以 GC 不会被触发；连续渲染时实测 RSS 以 ~66MB/s 线性增长，几分钟就吃掉几个 GB，
     * 最终 malloc 失败或拖垮显卡驱动。
     *
     * 这里按"累计发布字节数"计费，超过 `GLConfig.nativeCopyBudgetMb` 就主动回收一次，
     * 把峰值内存钳在预算量级（默认 64MB，60fps 下约每秒一次）。
     * 实测对帧率影响约 1fps（58.5 -> 57.3）。
     */
    private fun reclaimNativeIfNeeded(reader: FrameReader, width: Int, height: Int) {
        val budgetMb = config.nativeCopyBudgetMb
        // resize 丢弃的位图也要计入，否则拖拽窗口会把原生内存一路顶上去
        nativeBytesSinceGc += reader.drainReleasedBytes()
        if (budgetMb <= 0) return
        nativeBytesSinceGc += width.toLong() * height * 4
        if (nativeBytesSinceGc >= budgetMb.toLong() * 1024 * 1024) {
            nativeBytesSinceGc = 0
            System.gc()
        }
    }

}
