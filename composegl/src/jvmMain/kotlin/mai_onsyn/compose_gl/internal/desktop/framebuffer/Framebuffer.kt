package mai_onsyn.compose_gl.internal.desktop.framebuffer

import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.error.GLException
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30

/**
 * 离屏帧缓冲：一张 RGBA8 颜色纹理 + 可选的深度/模板 renderbuffer。
 *
 * `config.msaa > 1` 时走多重采样路径：颜色/深度挂在多重采样 renderbuffer 上，
 * 每帧末尾 blit 到单采样纹理再回读（GL 不能直接回读多重采样缓冲）。
 *
 * 只负责创建、重建、绑定、解析、销毁，不碰像素。
 */
internal class Framebuffer {

    /** 绘制目标。开 MSAA 时是多重采样 FBO，否则就是解析后的 FBO。 */
    private var drawFbo = 0

    /** 仅 MSAA 时存在：单采样目标，颜色纹理挂在它上面，回读从这里取。 */
    private var resolveFbo = 0

    private var colorTexture = 0
    private var msaaColor = 0
    private var depthStencil = 0
    private var samples = 1

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
                // 多重采样不可用：退回单采样，保证功能可用
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

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    /** 绑定绘制目标并把视口设成目标尺寸。库在每帧开头调用，但**不清屏**。 */
    fun bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, drawFbo)
        GL11.glViewport(0, 0, width, height)
    }

    /**
     * 把多重采样结果解析到单采样纹理，并让 [drawFbo] 之外的回读目标就绪。
     *
     * 非 MSAA 时是空操作（[bind] 已经绑好了带颜色纹理的 FBO）。
     * 调用后当前绑定的 FBO 一定是可回读的单采样目标。
     */
    fun resolve() {
        if (resolveFbo == 0) return
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFbo)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, resolveFbo)
        GL30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0)
        GL30.glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST,
        )
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, resolveFbo)
        GL11.glViewport(0, 0, width, height)
    }

    fun dispose() {
        if (msaaColor != 0) {
            GL30.glDeleteRenderbuffers(msaaColor)
            msaaColor = 0
        }
        if (depthStencil != 0) {
            GL30.glDeleteRenderbuffers(depthStencil)
            depthStencil = 0
        }
        if (colorTexture != 0) {
            GL11.glDeleteTextures(colorTexture)
            colorTexture = 0
        }
        if (resolveFbo != 0) {
            GL30.glDeleteFramebuffers(resolveFbo)
            resolveFbo = 0
        }
        if (drawFbo != 0) {
            GL30.glDeleteFramebuffers(drawFbo)
            drawFbo = 0
        }
        width = 0
        height = 0
        samples = 1
    }

    private fun createColorTexture(w: Int, h: Int) {
        colorTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
            w, h, 0,
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as java.nio.ByteBuffer?,
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }

    /** 单采样 FBO：颜色直接挂纹理，深度/模板挂 renderbuffer。 */
    private fun createSingleSampleFbo(config: GLConfig, w: Int, h: Int): Int {
        val fbo = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D, colorTexture, 0,
        )
        if (config.depthBits > 0 || config.stencilBits > 0) {
            depthStencil = GL30.glGenRenderbuffers()
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthStencil)
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, depthStencilFormat(config), w, h)
            attachDepthStencil(config)
        }
        return fbo
    }

    /** 仅承载解析结果的 FBO：只挂颜色纹理。 */
    private fun createResolveFbo(): Int {
        val fbo = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D, colorTexture, 0,
        )
        return fbo
    }

    /** 多重采样 FBO：颜色与深度都是多重采样 renderbuffer。 */
    private fun createMsaaFbo(config: GLConfig, w: Int, h: Int): Int {
        val fbo = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

        msaaColor = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, msaaColor)
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL11.GL_RGBA8, w, h)
        GL30.glFramebufferRenderbuffer(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_RENDERBUFFER, msaaColor,
        )

        if (config.depthBits > 0 || config.stencilBits > 0) {
            depthStencil = GL30.glGenRenderbuffers()
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthStencil)
            GL30.glRenderbufferStorageMultisample(
                GL30.GL_RENDERBUFFER, samples, depthStencilFormat(config), w, h,
            )
            attachDepthStencil(config)
        }
        return fbo
    }

    private fun attachDepthStencil(config: GLConfig) {
        val target = when {
            config.depthBits > 0 && config.stencilBits > 0 -> GL30.GL_DEPTH_STENCIL_ATTACHMENT
            config.depthBits > 0 -> GL30.GL_DEPTH_ATTACHMENT
            else -> GL30.GL_STENCIL_ATTACHMENT
        }
        GL30.glFramebufferRenderbuffer(
            GL30.GL_FRAMEBUFFER, target, GL30.GL_RENDERBUFFER, depthStencil,
        )
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0)
    }

    private fun depthStencilFormat(config: GLConfig): Int = when {
        config.depthBits > 0 && config.stencilBits > 0 -> GL30.GL_DEPTH24_STENCIL8
        config.depthBits > 0 -> GL30.GL_DEPTH_COMPONENT24
        else -> GL30.GL_STENCIL_INDEX8
    }

    private fun isComplete(fbo: Int): Boolean {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE
    }

    /** 把请求的采样数收敛到驱动支持的范围。 */
    private fun resolveSampleCount(requested: Int): Int {
        if (requested <= 1) return 1
        val max = GL11.glGetInteger(GL30.GL_MAX_SAMPLES)
        return requested.coerceIn(2, max.coerceAtLeast(2))
    }
}
