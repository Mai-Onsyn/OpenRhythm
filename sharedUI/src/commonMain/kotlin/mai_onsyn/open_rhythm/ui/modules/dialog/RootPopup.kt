package mai_onsyn.open_rhythm.ui.modules.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset

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
        Box(
            Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            for (popup in popupState.popups) {
                Box(Modifier.fillMaxSize()) {
                    popup()
                }
            }
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