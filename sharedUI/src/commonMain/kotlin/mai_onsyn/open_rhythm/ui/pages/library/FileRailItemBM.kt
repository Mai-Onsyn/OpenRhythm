package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.UIMidiData
import mai_onsyn.open_rhythm.ui.icons.ic_graphic_eq
import mai_onsyn.open_rhythm.ui.modules.LoadingSpinner
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.utility.str
import mai_onsyn.open_rhythm.ui.utility.toFileSize
import openrhythm.sharedui.generated.resources.*

/**
 * OMG 逆天代码能跑通就不要动了
 */
val loadingSet = mutableStateSetOf<String>()

@Composable
fun FileRailItemBM(
    modifier: Modifier = Modifier,
    target: UIMidiData,
    onRefresh: () -> Unit
) {
    var showInfoSelector by remember { mutableStateOf(false) }

    var loading by rememberSaveable { mutableStateOf(Global.fileLoader.fileIsLoading(target.path)) }
    Surface(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            if (!loading) {
                showInfoSelector = true
                Logger.d { "Open info selector for black midi: ${target.fileName}" }
            }
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpacitySurface(
                contentPadding = 8.dp,
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Icon(
                    imageVector = ic_graphic_eq,
                    contentDescription = target.path,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = target.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = target.fileSize.toFileSize(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Not Loaded",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (loading) {
                LoadingSpinner(Modifier.size(24.dp))
            }
        }
    }

    var fileToLoad by rememberSaveable { mutableStateOf(0) }
    DialogPopup(
        visible = showInfoSelector,
        onDismissRequest = { showInfoSelector = false }
    ) {
        BlackMidiLoadConfirmContent(
            midiName = target.fileName,
            fileSize = target.fileSize,
            onConfirm = { fileToLoad++; showInfoSelector = false },
            onCancel = { showInfoSelector = false }
        )
    }
    LaunchedEffect(Unit) {
        if (loading && fileToLoad == 0) {
            loadingSet.add(target.path)
        }
    }
    LaunchedEffect(loadingSet.contains(target.path)) {
        if (!loadingSet.contains(target.path)) {
            loading = false
            onRefresh()
        }
    }
    LaunchedEffect(fileToLoad) {
        if (fileToLoad == 1) {
            loading = true
            Global.fileLoader.loadFile(target.path) { midi ->
                onRefresh()
                loadingSet.remove(target.path)
                loading = false
            }
        }
    }
}

@Composable
fun BlackMidiLoadConfirmContent(
    midiName: String,
    fileSize: Long,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 420.dp)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Text(
            text = str(Res.string.lib_bmConfirmTitle),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = str(Res.string.lib_bmConfirmMessage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        InfoRow(label = str(Res.string.common_fileName), value = midiName)
        Spacer(Modifier.height(6.dp))
        InfoRow(label = str(Res.string.common_fileSize), value = fileSize.toFileSize())

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                onClick = onCancel
            ) { Text(str(Res.string.common_cancel)) }
            Spacer(Modifier.width(8.dp))
            PrimaryOperationButton(
                title = str(Res.string.lib_bmConfirmButton),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}