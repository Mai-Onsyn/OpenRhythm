package mai_onsyn.compose_gl.config

/** 帧从哪来。 */
sealed interface FramePolicy {
    /**
     * 只有调用 [mai_onsyn.compose_gl.scope.GLRenderScope.requestRender] 才渲染一帧。
     *
     * 这是默认策略，符合「改了状态才需要重画」的心智模型，空闲时零开销。
     */
    data object OnRequest : FramePolicy

    /**
     * 持续按显示器刷新率转帧。
     *
     * @param maxFps 0 表示跟随显示器刷新率（安卓取面板支持的最高档）；大于 0 时按该帧率限速。
     */
    data class Continuous(val maxFps: Int = 0) : FramePolicy
}
