package mai_onsyn.compose_gl.internal.android.framebuffer

import android.opengl.GLES20
import android.opengl.GLES30
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException

/**
 * 离屏帧缓冲：一张 RGBA8 颜色纹理 + 可选的深度/模板 renderbuffer。
 *
 * `config.msaa > 1` 时走多重采样路径：颜色/深度挂在多重采样 renderbuffer 上，
 * 每帧末尾 blit 到单采样纹理再回读（GLES 不能直接回读多重采样缓冲）。
 *
 * 只负责创建、重建、绑定、解析、销毁，不碰像素。
 */
internal class GlesFramebuffer {

    private var drawFbo = 0
    private var resolveFbo = 0
    private var colorTexture = 0
    private var msaaColor = 0
    private var depthStencil = 0
    private var samples = 1

    /** 已解析的单采样目标（非 MSAA 时就是绘制目标本身）。窗口模式用它 blit 到默认帧缓冲。 */
    val presentedFbo: Int
        get() = if (resolveFbo != 0) resolveFbo else drawFbo

    var width = 0
        private set
    var height = 0
        private set

    fun resize(config: GLConfig, newWidth: Int, newHeight: Int) {
        if (drawFbo != 0 && newWidth == width && newHeight == height) return
        dispose()
        width = newWidth
        height = newHeight
        samples = resolveSampleCount(config.msaa)

        createColorTexture(newWidth, newHeight)

        if (samples > 1) {
            resolveFbo = createResolveFbo()
            drawFbo = createMsaaFbo(config, newWidth, newHeight)
            if (!isComplete(drawFbo)) {
                // 部分设备不支持多重采样 renderbuffer：退回单采样
                dispose()
                width = newWidth
                height = newHeight
                samples = 1
                createColorTexture(newWidth, newHeight)
                drawFbo = createSingleSampleFbo(config, newWidth, newHeight)
                if (!isComplete(drawFbo)) {
                    dispose()
                    throw GLException("Framebuffer 不完整（MSAA 回退后仍然失败）")
                }
            }
        } else {
            drawFbo = createSingleSampleFbo(config, newWidth, newHeight)
            if (!isComplete(drawFbo)) {
                dispose()
                throw GLException("Framebuffer 不完整")
            }
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    /** 绑定绘制目标并把视口设成目标尺寸。库在每帧开头调用，但**不清屏**。 */
    fun bind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, drawFbo)
        GLES20.glViewport(0, 0, width, height)
    }

    /** 把多重采样结果解析到单采样纹理。非 MSAA 时是空操作。 */
    fun resolve() {
        if (resolveFbo == 0) return
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, drawFbo)
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, resolveFbo)
        GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0)
        GLES30.glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GLES30.GL_COLOR_BUFFER_BIT, GLES20.GL_NEAREST,
        )
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, resolveFbo)
        GLES20.glViewport(0, 0, width, height)
    }

    fun dispose() {
        if (msaaColor != 0) {
            GLES30.glDeleteRenderbuffers(1, intArrayOf(msaaColor), 0)
            msaaColor = 0
        }
        if (depthStencil != 0) {
            GLES30.glDeleteRenderbuffers(1, intArrayOf(depthStencil), 0)
            depthStencil = 0
        }
        if (colorTexture != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(colorTexture), 0)
            colorTexture = 0
        }
        if (resolveFbo != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(resolveFbo), 0)
            resolveFbo = 0
        }
        if (drawFbo != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(drawFbo), 0)
            drawFbo = 0
        }
        width = 0
        height = 0
        samples = 1
    }

    private fun createColorTexture(w: Int, h: Int) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        colorTexture = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, colorTexture)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            w, h, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null,
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private fun createSingleSampleFbo(config: GLConfig, w: Int, h: Int): Int {
        val framebuffers = IntArray(1)
        GLES30.glGenFramebuffers(1, framebuffers, 0)
        val fbo = framebuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, colorTexture, 0,
        )
        if (config.depthBits > 0 || config.stencilBits > 0) {
            val renderbuffers = IntArray(1)
            GLES30.glGenRenderbuffers(1, renderbuffers, 0)
            depthStencil = renderbuffers[0]
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthStencil)
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, depthStencilFormat(config), w, h)
            attachDepthStencil(config)
        }
        return fbo
    }

    private fun createResolveFbo(): Int {
        val framebuffers = IntArray(1)
        GLES30.glGenFramebuffers(1, framebuffers, 0)
        val fbo = framebuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, colorTexture, 0,
        )
        return fbo
    }

    private fun createMsaaFbo(config: GLConfig, w: Int, h: Int): Int {
        val framebuffers = IntArray(1)
        GLES30.glGenFramebuffers(1, framebuffers, 0)
        val fbo = framebuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)

        val colors = IntArray(1)
        GLES30.glGenRenderbuffers(1, colors, 0)
        msaaColor = colors[0]
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, msaaColor)
        GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, samples, GLES30.GL_RGBA8, w, h)
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, msaaColor,
        )

        if (config.depthBits > 0 || config.stencilBits > 0) {
            val renderbuffers = IntArray(1)
            GLES30.glGenRenderbuffers(1, renderbuffers, 0)
            depthStencil = renderbuffers[0]
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthStencil)
            GLES30.glRenderbufferStorageMultisample(
                GLES30.GL_RENDERBUFFER, samples, depthStencilFormat(config), w, h,
            )
            attachDepthStencil(config)
        }
        return fbo
    }

    private fun attachDepthStencil(config: GLConfig) {
        val target = when {
            config.depthBits > 0 && config.stencilBits > 0 -> GLES30.GL_DEPTH_STENCIL_ATTACHMENT
            config.depthBits > 0 -> GLES30.GL_DEPTH_ATTACHMENT
            else -> GLES30.GL_STENCIL_ATTACHMENT
        }
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER, target, GLES30.GL_RENDERBUFFER, depthStencil,
        )
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0)
    }

    private fun depthStencilFormat(config: GLConfig): Int = when {
        config.depthBits > 0 && config.stencilBits > 0 -> GLES30.GL_DEPTH24_STENCIL8
        config.depthBits > 0 -> GLES30.GL_DEPTH_COMPONENT24
        else -> GLES30.GL_STENCIL_INDEX8
    }

    private fun isComplete(fbo: Int): Boolean {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        return GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) == GLES30.GL_FRAMEBUFFER_COMPLETE
    }

    private fun resolveSampleCount(requested: Int): Int {
        if (requested <= 1) return 1
        val max = IntArray(1)
        GLES20.glGetIntegerv(GLES30.GL_MAX_SAMPLES, max, 0)
        val supported = if (max[0] >= 2) max[0] else 1
        return requested.coerceAtMost(supported)
    }
}
