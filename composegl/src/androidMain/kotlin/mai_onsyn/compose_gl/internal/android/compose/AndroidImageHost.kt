package mai_onsyn.compose_gl.internal.android.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 渲染线程与 Compose 之间唯一的交接点。
 *
 * 渲染线程调用 [publish] 写入最新一帧；Compose 只在 **draw 阶段** 读取，只触发重绘。
 */
internal class AndroidImageHost {
    private val state = mutableStateOf<ImageBitmap?>(null)

    val frame = state

    fun publish(image: ImageBitmap) {
        state.value = image
    }
}
