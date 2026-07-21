package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import co.touchlab.kermit.Logger
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.TraditionalCoreMidiAccess
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
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

actual fun registerGlobalKeyEventDispatcher(keyEventDispatcher: GlobalKeyEventDispatcher) {
    Logger.w { "ios platform cannot register a global keyEvent handler" }
}