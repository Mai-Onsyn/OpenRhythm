package mai_onsyn.open_rhythm.bridge

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import coil3.compose.rememberAsyncImagePainter
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.jetbrains.compose.resources.DrawableResource

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