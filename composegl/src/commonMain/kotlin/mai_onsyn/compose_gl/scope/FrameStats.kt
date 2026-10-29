package mai_onsyn.compose_gl.scope

/** 最近一帧的耗时统计。单位均为纳秒。 */
data class FrameStats(
    val frameIndex: Long = 0,
    val drawNanos: Long = 0,
    val readbackNanos: Long = 0,
    val presentNanos: Long = 0,
    val droppedFrames: Long = 0,
) {
    val totalNanos: Long get() = drawNanos + readbackNanos + presentNanos
    val totalMillis: Double get() = totalNanos / 1_000_000.0
}
