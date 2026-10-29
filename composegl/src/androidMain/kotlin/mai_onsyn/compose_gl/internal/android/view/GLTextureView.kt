package mai_onsyn.compose_gl.internal.android.view

import android.content.Context
import android.view.MotionEvent
import android.view.TextureView

/**
 * 零拷贝模式用的 TextureView。
 *
 * 自身不消费触摸，但**这改变不了上层 `AndroidView` 会吞掉全部触摸的事实**
 * （见 docs/DESIGN.md 7.5）。想解决需要在外层容器上挂 handler。
 */
internal class GLTextureView(context: Context) : TextureView(context) {

    init {
        isOpaque = false
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false
}
