package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import dev.atsushieno.ktmidi.MidiAccess

expect fun getMidiAccess(): MidiAccess

expect object AppCursors {
    val horizontalResize: PointerIcon
    val verticalResize: PointerIcon
}