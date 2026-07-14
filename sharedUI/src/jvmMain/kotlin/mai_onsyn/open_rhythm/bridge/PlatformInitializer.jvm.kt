package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.LibreMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import java.awt.Cursor

actual fun getMidiAccess(): MidiAccess {
    return JvmMidiAccess()//LibreMidiAccess(0)
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
    actual val verticalResize: PointerIcon
        get() = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
}