package mai_onsyn.compose_gl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.concurrent.Volatile

/**
 * 从**回调之外**驱动一块 [GLCanvas] 的句柄。
 *
 * ## 为什么需要它
 *
 * 默认的 [mai_onsyn.compose_gl.config.FramePolicy.OnRequest] 只有被请求时才画一帧，
 * 而 [mai_onsyn.compose_gl.scope.GLRenderScope] 只活在回调内部 —— 回调外面没有入口。
 * 想知道"状态变了，该重画了"的往往是回调外面的代码（Compose 状态变化、后台线程、
 * 动画、鼠标事件），这时候就需要把句柄从组合里 `remember` 出来。
 *
 * `FramePolicy.Continuous` 自己会按刷新率转帧，不需要句柄，所以 [GLCanvas.handle] 是可选的。
 *
 * ## 生命周期
 *
 * 句柄活在 composition 里，与画布的挂载周期**完全解耦**：画布卸载后调用依然安全（静默忽略），
 * 重新挂载后自动接上。所以它可以放心地被 ViewModel、协程、事件回调长期持有。
 *
 * ## 线程
 *
 * 所有成员都可以在任意线程调用，不需要切到渲染线程或 EDT。
 *
 * ```kotlin
 * val gl = rememberGLCanvasHandle()
 *
 * GLCanvas(handle = gl) {
 *     renderer.draw(width, height)
 * }
 *
 * // 状态变了：任意线程、任意位置
 * gl.requestRender()
 * ```
 */
class GLCanvasHandle internal constructor() {

    /** 挂载期间指向"让当前 surface 排一帧"的闭包，卸载后清空。 */
    @Volatile
    private var renderRequest: (() -> Unit)? = null

    /** 挂载期间指向"把暂停状态应用到当前 surface"的闭包。 */
    @Volatile
    private var pauseApplier: ((Boolean) -> Unit)? = null

    /** 用户设定的暂停状态。per-surface 的字段会随挂载重建，所以句柄自己留一份。 */
    @Volatile
    private var pausedValue: Boolean = false

    /** 当前是否已有一块画布挂在这个句柄上。 */
    val isAttached: Boolean get() = renderRequest != null

    /**
     * 暂停出帧，但**保留上下文与全部 GL 资源**。
     *
     * 语义是"这一帧先不画"：着色器、VBO、FBO 都还在，置回 `false` 时自动补一帧
     * （否则 OnRequest 模式下画面会停在暂停前那一帧）。
     * 要真正丢弃资源、让区域留空，请用 [GLCanvas.enabled]。
     *
     * 画布未挂载时设置同样有效：挂载瞬间就会应用。
     */
    var paused: Boolean
        get() = pausedValue
        set(value) {
            if (pausedValue == value) return
            pausedValue = value
            pauseApplier?.invoke(value)
            // Continuous 下一轮自然会画；OnRequest 必须显式请求，否则恢复后停在旧画面
            if (!value) renderRequest?.invoke()
        }

    /**
     * 请求渲染一帧。
     *
     * 任意线程可调；画布当前未挂载时静默忽略（挂载时本来就会画第一帧）。
     * 在 [mai_onsyn.compose_gl.scope.GLRenderScope.onDraw] 里调用它是"请求下一帧"的
     * 标准写法，用于逐帧推进的动画。
     */
    fun requestRender() {
        renderRequest?.invoke()
    }

    internal fun attach(request: () -> Unit, applyPause: (Boolean) -> Unit) {
        renderRequest = request
        pauseApplier = applyPause
        // 挂载前就设过的 paused 要补上，否则会先画一帧再停
        applyPause(pausedValue)
    }

    internal fun detach() {
        renderRequest = null
        pauseApplier = null
    }
}

/**
 * 记住一个 [GLCanvasHandle]，用来从回调之外驱动画布。
 *
 * 同一块 [GLCanvas] 请只传一个句柄；一个句柄也只对应一块画布。
 */
@Composable
fun rememberGLCanvasHandle(): GLCanvasHandle = remember { GLCanvasHandle() }
