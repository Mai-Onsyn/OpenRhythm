package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess

actual fun getMidiAccess(): MidiAccess {
    TODO()
//    getApplicationContext()
//    return AndroidMidiAccess()
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon.Default
    actual val verticalResize: PointerIcon
        get() = PointerIcon.Default
}

actual fun createSetting(): Settings {
    TODO()
//    return SharedPreferencesSettings()
}