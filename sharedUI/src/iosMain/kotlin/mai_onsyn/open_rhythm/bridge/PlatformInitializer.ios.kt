package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import coil3.compose.rememberAsyncImagePainter
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.TraditionalCoreMidiAccess
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import platform.Foundation.NSUserDefaults

actual fun getMidiAccess(): MidiAccess {
    return TraditionalCoreMidiAccess()
}

actual object AppCursors {
    actual val horizontalResize: PointerIcon
        get() = PointerIcon.Default
    actual val verticalResize: PointerIcon
        get() = PointerIcon.Default
}

actual fun createSetting(): Settings {
    return NSUserDefaultsSettings(
        delegate = NSUserDefaults.standardUserDefaults
    )
}

actual suspend fun FileKit.pickDirectoryWithPermission(): PlatformFile? {
    return FileKit.openDirectoryPicker()
}