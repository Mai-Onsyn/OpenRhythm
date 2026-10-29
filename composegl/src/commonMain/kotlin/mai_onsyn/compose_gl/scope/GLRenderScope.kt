package mai_onsyn.compose_gl.scope

/**
 * 渲染回调里拿到的句柄。
 *
 * 库不携带任何 GL 句柄——桌面用 LWJGL 的静态对象、安卓用 android.opengl 的静态对象，
 * 库只需保证回调执行时上下文在当前线程上 current。
 */
interface GLRenderScope {
    /** render target 的像素宽度（已应用 resolutionScale）。 */
    val width: Int

    /** render target 的像素高度（已应用 resolutionScale）。 */
    val height: Int

    /** Compose 的密度，用于把 dp 换算成像素。 */
    val density: Float

    /** 从 0 开始递增的帧号。 */
    val frameIndex: Long

    /** 本帧起始时间（单调时钟，纳秒）。 */
    val frameTimeNanos: Long

    /** 上下文信息。 */
    val info: GLInfo

    /**
     * 当前显示器的刷新率（Hz）。
     *
     * `FramePolicy.Continuous(maxFps = 0)` 用它定帧周期；也方便使用方核对"为什么没跑满"。
     */
    val displayRefreshRateHz: Float

    /** 上一帧的耗时统计。 */
    val stats: FrameStats

    /** 请求下一帧。这是 OnRequest 模式下唯一的驱动入口。 */
    fun requestRender()
}
