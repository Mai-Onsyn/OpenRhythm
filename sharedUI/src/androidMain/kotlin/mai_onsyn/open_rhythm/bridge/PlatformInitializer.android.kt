package mai_onsyn.open_rhythm.bridge

import android.app.Activity
import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import android.content.Context
import androidx.activity.ComponentActivity
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

internal lateinit var appContext: Context

fun initAndroid(context: ComponentActivity) {
    appContext = context.applicationContext
    FileKit.init(context)
}

actual fun getMidiAccess(): MidiAccess {
    return AndroidMidiAccess(appContext)
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon.Default
    actual val verticalResize: PointerIcon
        get() = PointerIcon.Default
}

actual fun createSetting(): Settings {
    return SharedPreferencesSettings(
        appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    )
}