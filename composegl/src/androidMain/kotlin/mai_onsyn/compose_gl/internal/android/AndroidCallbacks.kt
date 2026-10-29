package mai_onsyn.compose_gl.internal.android

import androidx.compose.ui.unit.IntSize
import mai_onsyn.compose_gl.error.GLException
import mai_onsyn.compose_gl.scope.GLRenderScope

/** 用户回调的持有者。用可变字段承接每次重组的 lambda，避免过期闭包。 */
internal class AndroidCallbacks {
    var created: (GLRenderScope) -> Unit = {}
    var resized: (GLRenderScope, IntSize) -> Unit = { _, _ -> }
    var draw: (GLRenderScope) -> Unit = {}
    var destroyed: (GLRenderScope) -> Unit = {}
    var failure: (GLException) -> Unit = {}
}
