package mai_onsyn.compose_gl.internal

import mai_onsyn.compose_gl.scope.FrameStats
import mai_onsyn.compose_gl.scope.GLInfo
import mai_onsyn.compose_gl.scope.GLRenderScope

/**
 * [GLRenderScope] 的唯一实现。
 *
 * 由平台实现（桌面/安卓）在每次回调前填充字段，因此这里只做数据承载，
 * 不含任何平台逻辑。
 */
internal class MutableGLRenderScope : GLRenderScope {
    // ---- 由渲染线程写入、回调读取 ----
    var w: Int = 0
    var h: Int = 0
    var densityValue: Float = 1f
    var frame: Long = 0
    var frameTime: Long = 0
    var infoValue: GLInfo = EMPTY_INFO
    var statsValue: FrameStats = FrameStats()
    var refreshRateValue: Float = 60f

    // ---- 由平台实现注入 ----
    var requestHandler: (() -> Unit)? = null

    override val width: Int get() = w
    override val height: Int get() = h
    override val density: Float get() = densityValue
    override val frameIndex: Long get() = frame
    override val frameTimeNanos: Long get() = frameTime
    override val info: GLInfo get() = infoValue
    override val stats: FrameStats get() = statsValue
    override val displayRefreshRateHz: Float get() = refreshRateValue

    override fun requestRender() {
        requestHandler?.invoke()
    }

    private companion object {
        val EMPTY_INFO = GLInfo(0, 0, "unknown", "unknown", "unknown")
    }
}
