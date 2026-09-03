package mai_onsyn.open_rhythm.bridge

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.core.net.toUri
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.atsushieno.ktmidi.AndroidMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiOutput
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import mai_onsyn.open_rhythm.core.util.GlobalKeyEventDispatcher

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

actual suspend fun FileKit.pickFileWithPermission(): PlatformFile? {
    val platformFile = FileKit.openFilePicker() ?: return null

    val uri = platformFile.path.toUri()
    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    try {
        appContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return platformFile
}

var keyEventDispatcher: GlobalKeyEventDispatcher? = null
actual fun registerGlobalKeyEventDispatcher(keyEventDispatcher: GlobalKeyEventDispatcher) {
    mai_onsyn.open_rhythm.bridge.keyEventDispatcher = keyEventDispatcher
}

actual fun setupMidiOutput(output: MidiOutput, name: String, context: Any) {}