package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.input.pointer.PointerIcon
import com.russhwolf.settings.Settings
import dev.atsushieno.ktmidi.MidiAccess
import dev.atsushieno.ktmidi.MidiOutput
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher

expect fun getMidiAccess(): MidiAccess

expect object AppCursors {
    val horizontalResize: PointerIcon
    val verticalResize: PointerIcon
}

expect fun createSetting(): Settings

expect suspend fun FileKit.pickDirectoryWithPermission(): PlatformFile?

expect suspend fun FileKit.pickFileWithPermission(): PlatformFile?

expect fun registerGlobalKeyEventDispatcher(keyEventDispatcher: GlobalKeyEventDispatcher)

expect fun setupMidiOutput(output: MidiOutput, name: String, context: Any)