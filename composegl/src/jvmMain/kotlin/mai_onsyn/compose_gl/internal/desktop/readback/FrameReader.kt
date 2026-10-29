package mai_onsyn.compose_gl.internal.desktop.readback

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.lwjgl.opengl.GL11

/**
 * 把帧缓冲的像素搬进 Skia 位图，并产出一个**引用同一块像素内存**的 Image。
 *
 * 全链路零拷贝：
 * 1. `glReadPixels` 直接写进 `Slot.pixels`（指向 Skia 位图自己的内存）；
 * 2. 用 `Image.makeRaster(info, data, rowBytes)` 构造 Image —— Skia 只是引用
 *    `Slot.data`（`Data.makeWithoutCopy` 出来的），不拷贝像素；
 * 3. Compose 画这个 Image 时直接走 GPU 上传，不再经过 `makeFromBitmap` 的整帧拷贝。
 *
 * 每帧新建的只有一个很小的 SkImage 壳；大块的原生内存只有开头的两块位图。
 *
 * **绝对不要改成 `Image.makeFromBitmap` 或 `Image.makeFromPixmap`**：两者都会整帧拷贝，
 * 60fps 下会以几十 MB/s 的速度吃光原生内存。详见 [SkiaFrameBuffer] 的注释。
 *
 * 注意：不做 Y 轴翻转。GL 原点在左下、Skia 在左上，翻转放到 Compose 绘制阶段做。
 */
internal class FrameReader(private val frames: SkiaFrameBuffer) {

    fun read(width: Int, height: Int): ImageBitmap {
        frames.ensure(width, height)
        val slot = frames.nextSlot()

        slot.pixels.clear()
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, slot.pixels)

        val image = Image.makeRaster(slot.info, slot.data, slot.rowBytes)
        return image.toComposeImageBitmap()
    }

    /** 取走 resize 丢弃位图产生的字节数，交给上层的内存预算。 */
    fun drainReleasedBytes(): Long = frames.drainReleasedBytes()

    fun dispose() = frames.dispose()
}
