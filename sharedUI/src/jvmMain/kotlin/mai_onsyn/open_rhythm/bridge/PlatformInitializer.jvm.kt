package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.SystemFont
import co.touchlab.kermit.Logger
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiOutput
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import mai_onsyn.open_rhythm.core.util.GlobalKeyEventDispatcher
import java.awt.Cursor
import java.io.File
import java.util.*

actual fun getMidiAccess(): MidiAccess {
//    return LibreMidiAccess(API.Unspecified)
    Logger.d { "Use JVM MIDI Access" }
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

    Logger.d { "Created settings with file: ${configFile.absolutePath}" }
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
    Logger.d { "Setup key event dispatcher for jvm window" }
}

actual fun setupMidiOutput(output: MidiOutput, name: String, context: Any) {
    if (context is String && name == "Gervill") {
        val file = File(context)
        if (context.isNotBlank() && file.isFile) {
            loadSoundbankToKtmidiOutput(output, file)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
actual fun createFontFamily(): FontFamily {
    return FontFamily(
        SystemFont("Microsoft YaHei UI"),
        SystemFont("PingFang SC"),
        SystemFont("Noto Sans CJK SC"),
        SystemFont("Yu Gothic UI")
    )
}
