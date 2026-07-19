package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

expect fun getMidiAccess(): MidiAccess

expect object AppCursors {
    val horizontalResize: PointerIcon
    val verticalResize: PointerIcon
}

expect fun createSetting(): Settings

expect suspend fun FileKit.pickDirectoryWithPermission(): PlatformFile?