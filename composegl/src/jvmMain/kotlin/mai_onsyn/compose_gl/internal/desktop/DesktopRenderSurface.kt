package mai_onsyn.compose_gl.internal.desktop

import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.ComposeGL
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.config.contextSignature
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.gl.GLThreadGuard
import mai_onsyn.compose_gl.internal.MutableGLRenderScope
import mai_onsyn.compose_gl.internal.framePeriodNanos
import mai_onsyn.compose_gl.internal.desktop.compose.ComposeImageHost
import mai_onsyn.compose_gl.internal.desktop.compose.FrameHost
import mai_onsyn.compose_gl.internal.desktop.compose.SharedTextureHost
import mai_onsyn.compose_gl.internal.desktop.context.GlfwSession
import mai_onsyn.compose_gl.internal.desktop.context.SessionClient
import mai_onsyn.compose_gl.internal.desktop.framebuffer.Framebuffer
import mai_onsyn.compose_gl.internal.desktop.readback.FrameReader
import mai_onsyn.compose_gl.internal.desktop.readback.SkiaFrameBuffer
import mai_onsyn.compose_gl.internal.desktop.shared.SharedTexturePresenter
import mai_onsyn.compose_gl.scope.FrameStats
import mai_onsyn.compose_gl.scope.GLRenderScope
import org.lwjgl.opengl.GL11

/** 用户回调的持有者。用可变字段承接每次重组的 lambda，避免过期闭包。 */
internal class DesktopCallbacks {
    /** 每次挂载调用，与 [canvasDisposed] 严格配对。 */
    var canvasCreated: (GLRenderScope) -> Unit = {}
    var resized: (GLRenderScope, IntSize) -> Unit = { _, _ -> }
    var draw: (GLRenderScope) -> Unit = {}
    /** 每次卸载调用（上下文仍然有效）。 */
    var canvasDisposed: (GLRenderScope) -> Unit = {}
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
    private val host: FrameHost,
    val callbacks: DesktopCallbacks,
    refreshRateHz: Float = 60f,
    private val sharedTexture: Boolean = false,
) : SessionClient {

    private val composeHost = host as? ComposeImageHost
    private val sharedHost = host as? SharedTextureHost

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

    /** GC 泵用的临时数组，见 [pumpGcIfNeeded]。 */
    @Volatile
    private var gcPump: ByteArray? = null

    /** 距上次主动回收累计发布的像素字节数。 */

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
        useSharedContext = sharedTexture,
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
        val presenter = if (sharedTexture) {
            SharedTexturePresenter(requireNotNull(sharedHost) { "SharedTexture host missing" })
        } else null
        // 只有 canvasCreated 成功执行过，卸载时才回调 canvasDisposed，保证严格配对
        var canvasCreated = false
        GLThreadGuard.enter()
        try {
            session.glInfo?.let { scope.infoValue = it }
            scope.requestHandler = { requestRender() }
            scope.densityValue = density
            scope.refreshRateValue = refreshRateHz
            // 上下文级：每个上下文只一次，早于任何画布的 canvasCreated
            if (isFirstContextUse) {
                ComposeGL.onContextCreated(scope)
                // 上下文销毁时的回调交给会话线程收尾时执行（那时上下文仍然有效）
                session.onContextDestroy = { ComposeGL.onContextDestroyed(scope) }
            }
            // 画布级：每次挂载都调，与 canvasDisposed 配对
            callbacks.canvasCreated(scope)
            canvasCreated = true

            var lastWidth = -1
            var lastHeight = -1

            while (true) {
                if (!awaitFrame()) break
                if (paused) {
                    dropped++
                    // 暂停期间必须真的挂起：Continuous 模式下 pending 恒为 true，
                    // 直接 continue 会退化成忙转（CPU 100%）。100ms 只是兜底，
                    // paused setter 和 resize/requestRender 都会立刻唤醒。
                    if (framePolicy is FramePolicy.Continuous) {
                        synchronized(lock) { lock.wait(100) }
                    }
                    continue
                }
                val width = frameWidth
                val height = frameHeight
                if (width <= 0 || height <= 0) {
                    dropped++
                    continue
                }

                if (width != lastWidth || height != lastHeight) {
                    // 零拷贝路径每帧都会按当前尺寸新建纹理，这里不需要额外重建。
                    if (!sharedTexture) framebuffer.resize(config, width, height)
                    lastWidth = width
                    lastHeight = height
                    scope.w = width
                    scope.h = height
                    callbacks.resized(scope, IntSize(width, height))
                }

                scope.frame += 1
                val frameStart = System.nanoTime()
                scope.frameTime = frameStart

                if (presenter != null) presenter.beginFrame(width, height) else framebuffer.bind()

                val drawStart = System.nanoTime()
                callbacks.draw(scope)
                val drawEnd = System.nanoTime()

                val readEnd: Long
                if (presenter != null) {
                    // 零拷贝：只 flush + fence，把纹理 id 交给 EDT 上的 Skia adopt。
                    presenter.endFrame()
                    readEnd = System.nanoTime()
                } else {
                    // MSAA 时把多重采样结果解析到单采样纹理，之后的回读才拿得到像素
                    framebuffer.resolve()
                    GL11.glFlush()
                    val image = reader.read(width, height)
                    readEnd = System.nanoTime()
                    composeHost?.publish(image)
                }
                val presentEnd = System.nanoTime()

                if (presenter == null) {
                    // resize 丢弃的位图也要计入
                    reader.drainReleasedBytes()
                    pumpGcIfNeeded(width, height)
                }

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
            if (canvasCreated) {
                // 卸载回调：上下文仍然 current，用户在这里删 VBO/VAO 是安全的
                runCatching { callbacks.canvasDisposed(scope) }
            }
            presenter?.dispose()
            reader.dispose()
            framebuffer.dispose()
            GLThreadGuard.exit()
            synchronized(finishedLock) {
                loopFinished = true
                finishedLock.notifyAll()
            }
        }
    }

    /** 会话线程建上下文失败：把错误交给调用方，避免零拷贝模式静默白屏。 */
    override fun onSessionFailure(error: Throwable) {
        callbacks.failure(
            error as? GLException
                ?: GLException("桌面 GL 上下文创建失败：${error.message}", error),
        )
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

        // 会话被 invalidate 时也要退出，否则 close() 之后会话线程还在跑
        if (stopped || session.isClosed) return false
        pending = false
        // 复用字段而不是每帧新建 IntArray：帧循环里不该有堆分配
        frameWidth = pendingWidth
        frameHeight = pendingHeight
        true
    }

    /**
     * 每帧制造一点堆垃圾，用来**驱动 GC 周期性运行**。
     *
     * 为什么需要：每帧发布都会新建一个 Skia Image，Skia 为它上传一份显卡纹理
     * （约 w*h*4 字节原生内存）。这份内存的释放挂在 Image 的 Java 包装器上，
     * 只有 GC 跑到才会归还。而堆里都是几十字节的壳，分配速率几乎为零 →
     * GC 不跑 → 原生内存以 ~120MB/s 一路涨。
     *
     * 以前靠 `System.gc()` 兜底，但 ZGC 下 `System.gc()` 会**同步等整个回收周期完成**，
     * 生产环境实测几百毫秒一次，正是"卡得没法用"的根因。
     *
     * 改成每帧分配一块和帧同样大小的临时数组：ZGC 会因此持续做**并发**回收，
     * 停顿在亚毫秒级，Image 的壳被收走、纹理随之归还。
     */
    private fun pumpGcIfNeeded(width: Int, height: Int) {
        if (!config.gcPumpPerFrame) return
        // 必须"逃逸"出去（写进字段），否则 JIT 会把整个分配优化掉
        gcPump = ByteArray(width * height * 4)
    }

    private fun periodNanos(): Long {
        val policy = framePolicy as? FramePolicy.Continuous ?: return 0
        val fps = if (policy.maxFps > 0) policy.maxFps.toFloat() else refreshRateHz
        return framePeriodNanos(fps)
    }


}
