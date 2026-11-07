package mai_onsyn.compose_gl.internal.desktop.compose

import androidx.compose.runtime.State

/**
 * 桌面端「最新一帧」的交接点。
 *
 * 回读路径交的是 `ImageBitmap`（raster），零拷贝路径交的是 `org.jetbrains.skia.Image`
 * （GPU-backed）。两者的绘制方式不同：后者**不能**用 `toComposeImageBitmap()` ——
 * Compose 1.11 的实现是 `SkiaBackedImageBitmap(toBitmap())`，而 `toBitmap()` 会把 GPU
 * Image 用 `Canvas(bitmap).drawImage(...)` 整帧光栅化到 CPU 位图，等于又拷了一次。
 *
 * Compose 在 **draw 阶段**读这个 State，只触发重绘、不触发重组。
 */
internal interface FrameHost {
    val frame: State<Any?>
}
