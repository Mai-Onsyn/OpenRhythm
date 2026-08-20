import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mai_onsyn.open_rhythm.bridge.keyEventDispatcher
import mai_onsyn.open_rhythm.core.log.LogManager
import mai_onsyn.open_rhythm.ui.App
import java.awt.Dimension

fun main() = application {
    LogManager.initialize()
    val pressedKey = remember { mutableSetOf<Key>() }
    Window(
        title = "OpenRhythm",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
        icon = painterResource("icons/OpenRhythm.png"),
        onPreviewKeyEvent = {
            if (it.type == KeyEventType.KeyDown) {
                if (!pressedKey.contains(it.key)) {
                    keyEventDispatcher?.push(it)
                    pressedKey.add(it.key)
                }
            } else if (it.type == KeyEventType.KeyUp) {
                if (pressedKey.contains(it.key)) {
                    keyEventDispatcher?.push(it)
                    pressedKey.remove(it.key)
                }
            }
            false
        }
    ) {
        window.minimumSize = Dimension(480, 320)
        App()
    }
}