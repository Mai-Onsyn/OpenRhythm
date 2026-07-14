package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.TraditionalCoreMidiAccess

actual fun getMidiAccess(): MidiAccess {
    return TraditionalCoreMidiAccess()
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon.Default
    actual val verticalResize: PointerIcon
        get() = PointerIcon.Default
}