package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_build_circle
import mai_onsyn.open_rhythm.ui.icons.ic_reset_wrench
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun SettingSettings() { // 设置设置的设置
    SettingsCard(
        title = "Other",
        icon = ic_build_circle,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        var showDialog by remember { mutableStateOf(false) }
        var executed by remember { mutableStateOf(false) }
        item(
            "Reset settings",
            if (executed) "You need to restart the application to apply this change" else null,
            descColor = MaterialTheme.colorScheme.error
        ) {
            Button(
                onClick = { showDialog = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = ic_reset_wrench,
                    contentDescription = "Reset",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        ConfirmDialog(
            visible = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                executed = true
                Global.settings.resetAllSettings()
                showDialog = false
            },
            title = "Reset Settings",
            message = "Are you sure you want to reset all settings? \n(This might never be recoverable!)",
            isDangerous = true
        )
    }
}