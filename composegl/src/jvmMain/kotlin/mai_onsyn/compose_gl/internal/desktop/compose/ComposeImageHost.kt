package mai_onsyn.compose_gl.internal.desktop.compose

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 渲染线程与 Compose 之间唯一的交接点（回读路径）。
 *
 * 渲染线程调用 [publish] 写入最新一帧；Compose 只在 **draw 阶段** 读 [frame]，
 * 这样只触发重绘、不触发重组。
 */
internal class ComposeImageHost : FrameHost {
    private val state = mutableStateOf<Any?>(null)

    override val frame: State<Any?> get() = state

    fun publish(image: ImageBitmap) {
        state.value = image
    }
}
