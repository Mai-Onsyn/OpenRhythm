package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import org.jetbrains.compose.resources.DrawableResource

expect fun getMidiAccess(): MidiAccess

expect object AppCursors {
    val horizontalResize: PointerIcon
    val verticalResize: PointerIcon
}

expect fun createSetting(): Settings