package mai_onsyn.open_rhythm.bridge

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.path
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher

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

actual suspend fun FileKit.pickDirectoryWithPermission(): PlatformFile? {
    FileKit.openDirectoryPicker()?.let {
        val uri = it.path.toUri()

        appContext.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return it
    }
    return null
}

actual fun registerGlobalKeyEventDispatcher(keyEventDispatcher: GlobalKeyEventDispatcher) {
    Logger.w { "Global key event dispatcher not supported on Android yet" }
}