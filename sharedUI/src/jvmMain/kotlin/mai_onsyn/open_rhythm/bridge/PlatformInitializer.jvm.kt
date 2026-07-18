package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.JvmMidiAccess
import dev.atsushieno.ktmidi.LibreMidiAccess
import dev.atsushieno.ktmidi.MidiAccess
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import java.awt.Cursor
import java.io.File
import java.util.Properties

actual fun getMidiAccess(): MidiAccess {
    return JvmMidiAccess()//LibreMidiAccess(0)
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