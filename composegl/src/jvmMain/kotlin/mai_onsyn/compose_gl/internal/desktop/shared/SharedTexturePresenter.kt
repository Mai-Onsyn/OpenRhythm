package mai_onsyn.compose_gl.internal.desktop.shared

import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.internal.desktop.compose.SharedTextureHost
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32
import java.nio.ByteBuffer

/**
 * 零拷贝路径的「渲染目标 + 发布」。
 *
 * 与回读路径的差别：
 * - 不 `glReadPixels`、不建 Skia 位图、不跑 GC 泵；
 * - 每帧新建一张 GL 纹理，渲染完成后插入 fence，把「纹理 id + fence」交给
 *   [SharedTextureHost] 在 EDT 上 adopt 成 SkiaImage。
 *
 * 为什么每帧都要新纹理：Compose 的 display list 按底层 SkiaImage 缓存内容，
 * 复用同一个 Image 时画面永远停在首帧（实测 6/6 截图完全冻结）。
 *
 * v1 边界（下一阶段优化，先打通链路）：
 * - 纹理由 `adoptTextureFrom` 接管，旧帧在 EDT 上延迟 3 帧显式 close（内存有界）；
 *   EDT 忙时本帧直接丢弃并删纹理，形成背压；
 * - 暂不支持 MSAA 与深度/模板附件；
 * - FBO 复用，只有颜色附件每帧换。
 */
internal class SharedTexturePresenter(private val host: SharedTextureHost) {

    private var fbo = 0
    private var width = 0
    private var height = 0
    private var currentTexture = 0

    /** 绑定本帧的绘制目标并设好视口，必须在用户 [onDraw] 之前调用。 */
    fun beginFrame(w: Int, h: Int) {
        width = w
        height = h
        if (fbo == 0) fbo = GL30.glGenFramebuffers()

        currentTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0,
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?,
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            GL11.GL_TEXTURE_2D, currentTexture, 0,
        )
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw GLException("SharedTexture framebuffer incomplete")
        }
        GL11.glViewport(0, 0, w, h)
    }

    /** 用户画完之后：flush + fence，然后把帧交给 EDT。 */
    fun endFrame() {
        if (host.unusable) {
            discardUnpublished(0L)
            return
        }
        GL11.glFlush()
        val fence = GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
        // A fence only becomes signaled once the commands are actually submitted.
        GL11.glFlush()
        val published = host.tryPublish(currentTexture, fence, width, height)
        if (!published) discardUnpublished(fence)
    }

    /**
     * EDT 没接这一帧（背压或后端不对）：Skia 从未接管纹理所有权，
     * 必须由渲染线程自己删掉，否则这些「无人认领」的纹理会一直堆在显存里。
     */
    private fun discardUnpublished(fence: Long) {
        if (fence != 0L) GL32.glDeleteSync(fence)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
        GL30.glFramebufferTexture2D(
            GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, 0,
        )
        GL11.glDeleteTextures(currentTexture)
        currentTexture = 0
    }

    fun dispose() {
        if (fbo != 0) {
            GL30.glDeleteFramebuffers(fbo)
            fbo = 0
        }
    }
}
