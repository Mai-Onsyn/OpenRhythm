package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess

actual fun getMidiAccess(): MidiAccess {
    TODO()
//    return AndroidMidiAccess()
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon.Default
    actual val verticalResize: PointerIcon
        get() = PointerIcon.Default
}