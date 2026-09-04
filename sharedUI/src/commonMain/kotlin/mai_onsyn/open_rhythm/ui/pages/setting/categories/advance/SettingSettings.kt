package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_build_circle
import mai_onsyn.open_rhythm.ui.icons.ic_delete
import mai_onsyn.open_rhythm.ui.icons.ic_reset_wrench
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun SettingSettings() { // 设置设置的设置
    SettingsCard(
        title = str(Res.string.set_other_title),
        icon = ic_build_circle,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item(
            name = str(Res.string.set_other_resetAllMidiFileSettings),
            description = str(Res.string.set_other_resetAllMidiFileSettings_desc)
        ) {
            var showCleanDialog by remember { mutableStateOf(false) }
            var showCleanedDialog by remember { mutableStateOf(false) }
            DangerousButton(ic_delete) { showCleanDialog = true }

            var configCount by remember { mutableStateOf(Global.settings.midiFileSettings.size) }
            ConfirmDialog(
                visible = showCleanDialog,
                onDismissRequest = { showCleanDialog = false },
                onConfirm = {
                    showCleanDialog = false
                    showCleanedDialog = true
                    Logger.w { "$configCount File settings has been cleaned" }
                    Global.settings.clearUserMidiFileSettings()
                },
                title = str(Res.string.set_other_cleanUp),
                message = str(Res.string.set_other_cleanUpConfirm, configCount),
                isDangerous = true
            )

            ConfirmDialog(
                visible = showCleanedDialog,
                onDismissRequest = {
                    showCleanedDialog = false
                    configCount = Global.settings.midiFileSettings.size
                },
                onConfirm = { showCleanedDialog = false },
                title = str(Res.string.set_log_result),
                message = str(Res.string.set_other_cleanedMidiFileSettings, configCount)
            )
        }

        var resetAllExecuted by remember { mutableStateOf(false) }
        item(
            str(Res.string.set_other_resetAllSettings),
            if (resetAllExecuted) str(Res.string.set_other_resetAllSettings_desc) else null,
            descColor = MaterialTheme.colorScheme.error
        ) {
            var showResetAllDialog by remember { mutableStateOf(false) }
            DangerousButton(ic_reset_wrench) { showResetAllDialog = true }

            ConfirmDialog(
                visible = showResetAllDialog,
                onDismissRequest = { showResetAllDialog = false },
                onConfirm = {
                    resetAllExecuted = true
                    Global.settings.resetAllSettings()
                    showResetAllDialog = false
                    Logger.w { "All settings has been reset" }
                },
                title = str(Res.string.set_other_resetSettingsTitle),
                message = str(Res.string.set_other_resetSettingsConfirm),
                isDangerous = true
            )
        }
    }
}

@Composable
private fun DangerousButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Reset",
            modifier = Modifier.size(20.dp)
        )
    }
}