package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher

@Composable
fun VisualKeyboard(
    modifier: Modifier = Modifier,
    eventDispatcher: GlobalKeyEventDispatcher,
    activeKeys: MutableMap<Long, Color>,
    drawControl: Boolean = true,
    drawNumpad: Boolean = true
) {
    val density = LocalDensity.current

    var keyboardWidth by remember { mutableStateOf(0.dp) }
    val areaPadding by derivedStateOf { keyboardWidth * 0.01f }
    Column(
        modifier = modifier
            .onSizeChanged {
                keyboardWidth = with(density) { it.width.toDp() }
            },
    ) {

    }
}