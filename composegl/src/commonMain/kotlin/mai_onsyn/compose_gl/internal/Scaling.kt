package mai_onsyn.compose_gl.internal

/** 按 `GLConfig.resolutionScale` 把显示尺寸换算成渲染目标尺寸，最小 1。 */
internal fun scaledTargetSize(displaySize: Int, resolutionScale: Float): Int {
    val scale = if (resolutionScale.isFinite() && resolutionScale > 0f) resolutionScale else 1f
    return (displaySize * scale).toInt().coerceAtLeast(1)
}
