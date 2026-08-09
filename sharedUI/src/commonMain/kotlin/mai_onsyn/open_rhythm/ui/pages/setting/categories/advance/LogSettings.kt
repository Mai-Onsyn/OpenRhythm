package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.log.LogManager
import mai_onsyn.open_rhythm.ui.icons.ic_assignment
import mai_onsyn.open_rhythm.ui.modules.CompactOutlinedTextField
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun LogSettings() {
    val colorScheme = MaterialTheme.colorScheme
    SettingsCard(
        title = "Log",
        icon = ic_assignment,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        item("Log level") {
            val candidates = remember { listOf(
                ContextDropDownMenuItem("Trace", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("Debug", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("Info", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("Warn", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("Error", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("All", selectedContentColor = colorScheme.primary),
            ) }

            var expanded by remember { mutableStateOf(false) }
            ContextDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                selectedIndex = Singleton.settings.LogLevel,
                onSelect = { Singleton.settings.LogLevel = it },
                items = candidates,
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = candidates[Singleton.settings.LogLevel].label!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item("Log limit", "The current log limit, doesn't affect the logs that have already been output") {
            CompactOutlinedTextField(
                modifier = Modifier
                    .size(100.dp, 40.dp),
                value = Singleton.settings.MaxLogCount,
                onValueChange = { Singleton.settings.MaxLogCount = it },
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
                Text(
                    text = "Export",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        ConfirmDialog(
            visible = showResultDialog,
            onDismiss = { showResultDialog = false },
            onConfirm = { showResultDialog = false },
            title = "Result",
            message = dialogMessage
        )
    }
}