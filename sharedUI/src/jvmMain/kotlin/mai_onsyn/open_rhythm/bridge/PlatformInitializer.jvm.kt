package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiOutput
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import java.awt.Cursor
import java.io.File
import java.util.*

actual fun getMidiAccess(): MidiAccess {
//    return LibreMidiAccess(API.Unspecified)
    return JvmMidiAccess()
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
    actual val verticalResize: PointerIcon
        get() = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
}

actual fun createSetting(): Settings {
    val configFile = File(System.getProperty("user.dir"), "settings.properties")

    val props = Properties().apply {
        if (configFile.exists()) {
            load(configFile.inputStream())
        }
    }

    return PropertiesSettings(
        delegate = props,
        onModify = {
            configFile.outputStream().use { outputStream ->
                props.store(outputStream, "App Settings")
            }
        }
    )
}

actual suspend fun FileKit.pickDirectoryWithPermission(): PlatformFile? {
    return FileKit.openDirectoryPicker()
}

actual suspend fun FileKit.pickFileWithPermission(): PlatformFile? {
    return FileKit.openFilePicker()
}

var keyEventDispatcher: GlobalKeyEventDispatcher? = null
actual fun registerGlobalKeyEventDispatcher(keyEventDispatcher: GlobalKeyEventDispatcher) {
    mai_onsyn.open_rhythm.bridge.keyEventDispatcher = keyEventDispatcher
}

actual fun setupMidiOutput(output: MidiOutput, name: String, context: Any) {
    if (context is String && name == "Gervill") {
        val file = File(context)
        if (file.exists()) {
            loadSoundbankToKtmidiOutput(output, file)
        }
    }
}