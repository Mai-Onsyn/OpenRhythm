package mai_onsyn.open_rhythm.ui.modules.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

class PopupHostState {
    val popups: List<@Composable () -> Unit>
        field = mutableStateListOf<@Composable () -> Unit>()

    fun add(popup: @Composable () -> Unit) {
        popups.add(popup)
    }

    fun remove(popup: @Composable () -> Unit) {
        popups.remove(popup)
    }
}

val LocalPopupHostState = staticCompositionLocalOf<PopupHostState> {
    error("No PopupHostState provided")
}

@Composable
fun RootPopupHost(content: @Composable () -> Unit) {
    val popupState = LocalPopupHostState.current
    Box(Modifier.fillMaxSize()) {
        content()

        for (popup in popupState.popups) {
            popup()
        }
    }
}

@Composable
fun RootPopup(content: @Composable () -> Unit) {
    val popupState = LocalPopupHostState.current

    val currentContent by rememberUpdatedState(content)

    DisposableEffect(popupState) {
        val wrappedContent: @Composable () -> Unit = { currentContent() }
        popupState.add(wrappedContent)
        onDispose {
            popupState.remove(wrappedContent)
        }
    }
}