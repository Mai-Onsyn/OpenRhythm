package mai_onsyn.compose_gl.internal.android.readback

import android.graphics.Bitmap
import android.opengl.GLES20
import mai_onsyn.compose_gl.error.GLException
import java.nio.ByteBuffer

/**
 * 把帧缓冲的像素搬进 Android Bitmap。
 *
 * 安卓没有公开的"取 Bitmap 底层内存地址"的 API，所以这里必然有一次 CPU 拷贝
 * （glReadPixels 进直接缓冲 → copyPixelsFromBuffer 进 Bitmap）。
 *
 * 注意：不做 Y 轴翻转。GL 原点在左下、Bitmap 在左上，翻转放到 Compose 绘制阶段做。
 */
internal class AndroidFrameReader {

    private var buffer: ByteBuffer? = null
    private var bitmap: Bitmap? = null
    private var width = 0
    private var height = 0

    fun read(targetWidth: Int, targetHeight: Int): Bitmap {
        ensure(targetWidth, targetHeight)
        val pixels = buffer!!
        pixels.clear()
        GLES20.glReadPixels(
            0, 0, targetWidth, targetHeight,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels,
        )
        pixels.rewind()

        val target = bitmap!!
        target.copyPixelsFromBuffer(pixels)
        return target
    }

    private fun ensure(targetWidth: Int, targetHeight: Int) {
        if (bitmap != null && width == targetWidth && height == targetHeight) return
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw GLException("非法的回读尺寸：${targetWidth}x$targetHeight")
        }
        width = targetWidth
        height = targetHeight
        buffer = ByteBuffer.allocateDirect(targetWidth * targetHeight * 4)
        bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).apply {
            setPremultiplied(true)
        }
    }

    fun dispose() {
        bitmap?.recycle()
        bitmap = null
        buffer = null
        width = 0
        height = 0
    }
}
