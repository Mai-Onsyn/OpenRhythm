package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.log.LogManager
import mai_onsyn.open_rhythm.ui.icons.ic_assignment
import mai_onsyn.open_rhythm.ui.icons.ic_ios_share
import mai_onsyn.open_rhythm.ui.modules.CompactOutlinedTextField
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun LogSettings() {
    SettingsCard(
        title = "Log",
        icon = ic_assignment,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithDropDownMenu(
            name = "Log level",
            initial = Global.settings.LogLevel,
            onSelected = { Global.settings.LogLevel = it },
            items = listOf("Trace", "Debug", "Info", "Warn", "Error", "Fatal")
        )

        item("Log limit", "The current log limit, doesn't affect the logs that have already been output") {
            CompactOutlinedTextField(
                modifier = Modifier.size(80.dp, 40.dp),
                value = Global.settings.MaxLogCount,
                onValueChange = { Global.settings.MaxLogCount = it },
            )
        }

        val scope = rememberCoroutineScope()
        var showResultDialog by remember { mutableStateOf(false) }
        var dialogMessage by remember { mutableStateOf("") }
        item("Export logs") {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        LogManager.export(
                            onCompleted = { file, count ->
                                showResultDialog = true
                                dialogMessage = "Successfully exported $count logs to ${file.absolutePath()}"
                            },
                            onFailed = { _, e ->
                                showResultDialog = true
                                dialogMessage = "Failed to export logs: ${e::class.simpleName}: ${e.message}"
                            }
                        )
                    }
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = ic_ios_share,
                    contentDescription = "Export",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        ConfirmDialog(
            visible = showResultDialog,
            onDismissRequest = { showResultDialog = false },
            onConfirm = { showResultDialog = false },
            title = "Result",
            message = dialogMessage
        )
    }
}