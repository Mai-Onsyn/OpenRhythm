package mai_onsyn.compose_gl.internal.desktop.readback

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Data
import org.jetbrains.skia.ImageInfo
import java.nio.ByteBuffer

/**
 * 双缓冲的 Skia 位图对。
 *
 * 每块位图只分配一次，`glReadPixels` 直接写进它自己的内存（零 CPU 拷贝），
 * 并用 [Data.makeWithoutCopy] 把这块内存包成一个**不拷贝**的 SkData，
 * 供每帧构造 Image 时引用（见 [FrameReader]）。
 *
 * **这里踩过两个坑，记录清楚免得重犯：**
 *
 * 1. `Image.makeFromBitmap` 与 Compose 桌面端 `SkiaBackedCanvas.drawImageRect` 内部
 *    都会对**可变**位图做整帧拷贝。1080p / 60fps ≈ 500MB/s 的原生内存垃圾。
 * 2. `Image.makeFromPixmap` 看似共享像素，但 Skiko 传给 Skia 的 releaseProc 是 null，
 *    Skia 无法确认内存寿命，于是**仍然整帧拷贝**。实测默认窗口下 RSS 以 ~66MB/s 线性增长，
 *    20 秒涨 1.6GB（原生内存不受 JVM 堆管辖，GC 不会被触发，最终 malloc 失败）。
 *
 * 唯一真正零拷贝的路径是 `Image.makeRaster(info, data, rowBytes)` + `Data.makeWithoutCopy`。
 */
internal class SkiaFrameBuffer {

    /** 一块可复用的位图，连同指向其像素内存的各种视图。 */
    class Slot(
        val bitmap: Bitmap,
        val info: ImageInfo,
        val data: Data,
        val rowBytes: Int,
        val pixels: ByteBuffer,
    )

    private var slots: List<Slot> = emptyList()
    private var width = 0
    private var height = 0
    private var nextIndex = 0

    /**
     * 尺寸变化时丢弃的旧位图。
     *
     * **不能立刻 close**：它的像素内存可能还被上一帧的 Image 引用 —— Image 持有 Data，
     * Data 又把位图记为 `underlyingMemoryOwner`，close 掉会让那个 Image 指向已释放内存。
     *
     * **也不能无限留着**：拖拽窗口会连续触发几十上百次 resize，每次都丢两块整帧位图，
     * 实测内存会一路涨上去且回不到基线（用户报的就是这个）。
     *
     * 因此保留最近 [RETAINED_GENERATIONS] 代，更早的直接 close —— 那时至少已经发布了
     * 同样多的帧，旧 Image 早已被 Compose 丢弃。
     */
    private val retired = mutableListOf<Bitmap>()

    private var releasedBytes = 0L

    /** 取走自上次调用以来因 resize 丢弃的位图字节数。 */
    fun drainReleasedBytes(): Long {
        val released = releasedBytes
        releasedBytes = 0
        return released
    }

    fun ensure(targetWidth: Int, targetHeight: Int) {
        if (slots.isNotEmpty() && width == targetWidth && height == targetHeight) return
        if (slots.isNotEmpty()) {
            releasedBytes += slots.size.toLong() * width * height * 4
            retired += slots.map { it.bitmap }
            while (retired.size > RETAINED_GENERATIONS * SLOT_COUNT) {
                retired.removeAt(0).close()
            }
        }
        slots = List(SLOT_COUNT) { createSlot(targetWidth, targetHeight) }
        width = targetWidth
        height = targetHeight
        nextIndex = 0
    }

    /** 取下一块可写的位图；两块交替，避免与上一帧的绘制撞车。 */
    fun nextSlot(): Slot {
        val slot = slots[nextIndex]
        nextIndex = (nextIndex + 1) % slots.size
        return slot
    }

    fun dispose() {
        slots.forEach { it.bitmap.close() }
        retired.forEach { it.close() }
        retired.clear()
        slots = emptyList()
        width = 0
        height = 0
        nextIndex = 0
        releasedBytes = 0
    }

    private fun createSlot(w: Int, h: Int): Slot {
        val bitmap = Bitmap()
        val info = ImageInfo(w, h, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
        check(bitmap.allocPixels(info)) { "位图分配失败：${w}x$h" }

        val rowBytes = w * 4
        val pixmap = checkNotNull(bitmap.peekPixels()) { "无法取得位图内存地址" }
        val addr = pixmap.addr
        pixmap.close()

        // 不拷贝地包住位图内存；underlyingMemoryOwner 保证位图活着时 Data 才有效
        val data = Data.makeWithoutCopy(addr, rowBytes * h, bitmap)
        val pixels = org.lwjgl.system.MemoryUtil.memByteBuffer(addr, rowBytes * h)

        return Slot(bitmap, info, data, rowBytes, pixels)
    }

    private companion object {
        const val SLOT_COUNT = 2

        /** resize 后保留几代旧位图不 close。2 代约等于 2 帧，足够让在途的 Image 画完。 */
        const val RETAINED_GENERATIONS = 2
    }
}
