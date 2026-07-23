package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.ui.icons.ic_delete
import mai_onsyn.open_rhythm.ui.icons.ic_edit_square
import mai_onsyn.open_rhythm.ui.icons.ic_folder
import mai_onsyn.open_rhythm.ui.icons.ic_folder_eye
import mai_onsyn.open_rhythm.ui.icons.ic_more_vert
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.modules.dialog.SingleLineInputDialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderManageRail(
    modifier: Modifier = Modifier,
    items: List<UILibraryFolder>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onChange: (Int, UILibraryFolder) -> Unit,
    onDelete: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val bgColor by animateColorAsState(
                targetValue = if (selectedIndex == index) colorScheme.primary.copy(alpha = 0.12f)
                else colorScheme.primary.copy(alpha = 0f)
            )
            Surface(
                onClick = { onSelect(index) },
                color = bgColor,
                contentColor = colorScheme.onSurface,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OpacitySurface(
                        contentPadding = 8.dp,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(
                            imageVector = ic_folder,
                            contentDescription = item.name,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.dir,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    var fileCount by remember { mutableStateOf(0) }
                    Text(
                        text = "$fileCount files",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    suspend fun loadFileCount() = withContext(Dispatchers.IO) {
                        val folder = PlatformFile(item.dir)
                        if (!folder.exists() && !folder.isDirectory()) {
                            fileCount = 0
                            return@withContext null
                        }
                        val midiFiles = folder.list()
                        fileCount = midiFiles.count { it.isRegularFile() && it.extension == "mid" }
                    }
                    LaunchedEffect(Unit) {
                        loadFileCount()
                    }


                    var showRenameDialog by remember { mutableStateOf(false) }
                    var showPathInfoDialog by remember { mutableStateOf(false) }
                    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

                    val contextMenuItems = remember {
                        listOf(
                            ContextDropDownMenuItem("Rename", ic_edit_square),
                            ContextDropDownMenuItem("Show Path", ic_folder_eye),
                            ContextDropDownMenuItem("Delete", ic_delete, contentColor = colorScheme.error)
                        )
                    }
                    var selectedIndex by remember { mutableStateOf(selectedIndex) }
                    var expanded by remember { mutableStateOf(false) }
                    ContextDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        selectedIndex = selectedIndex,
                        onSelect = {
                            selectedIndex = it
                            when (it) {
                                0 -> showRenameDialog = true
                                1 -> showPathInfoDialog = true
                                2 -> showDeleteConfirmDialog = true
                            }
                        },
                        items = contextMenuItems
                    ) {
                        IconButton(
                            onClick = { expanded = true },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(24.dp, 32.dp)
                        ) {
                            Icon(
                                imageVector = ic_more_vert,
                                contentDescription = "Operations for ${item.name}",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    SingleLineInputDialog(
                        visible = showRenameDialog,
                        title = "Rename",
                        value = item.name,
                        icon = ic_edit_square,
                        placeholderText = "Input a new name",
                        errorHolderText = "Name can't be empty",
                        onDismiss = { showRenameDialog = false },
                        onConfirm = {
                            showRenameDialog = false
                            onChange(index, item.copy(name = it))
                        }
                    )

                    ConfirmDialog(
                        visible = showPathInfoDialog,
                        onDismiss = { showPathInfoDialog = false },
                        onConfirm = { showPathInfoDialog = false },
                        title = "Path of ${item.name}",
                        message = item.dir
                    )

                    ConfirmDialog(
                        visible = showDeleteConfirmDialog,
                        onDismiss = { showDeleteConfirmDialog = false },
                        onConfirm = {
                            showDeleteConfirmDialog = false
                            onDelete(index)
                        },
                        title = "Delete ${item.name}?",
                        isDangerous = true,
                        message = "Are you sure you want to delete this folder? \n(This won't delete the file on your device.)",
                    )
                }
            }
        }
    }
}