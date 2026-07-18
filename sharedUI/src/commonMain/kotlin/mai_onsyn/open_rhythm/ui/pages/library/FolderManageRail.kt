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
import mai_onsyn.open_rhythm.ui.icons.ic_more_vert
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
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
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val bgColor by animateColorAsState(
                targetValue = if (selectedIndex == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0f)
            )
            Surface(
                onClick = { onSelect(index) },
                color = bgColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.small,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    var fileCount by remember { mutableStateOf(0) }
                    Text(
                        text = "$fileCount files",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
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

                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        var iconRegionWidth by remember { mutableStateOf(0) }
                        IconButton(
                            onClick = { expanded = true },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(24.dp, 32.dp)
                        ) {
                            Icon(
                                imageVector = ic_more_vert,
                                contentDescription = "Operations for ${item.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        var showRenameDialog by remember { mutableStateOf(false) }
                        var showDeleteConfirmDialog by remember { mutableStateOf(false) }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.onSizeChanged { iconRegionWidth = it.width },
                            offset = DpOffset(with(LocalDensity.current) { (-iconRegionWidth).toDp() + 24.dp }, 0.dp),
                        ) {
                            DropDownContextItem(
                                text = "Rename",
                                onClick = {
                                    expanded = false
                                    showRenameDialog = true
                                },
                                icon = ic_edit_square
                            )
                            DropDownContextItem(
                                text = "Delete",
                                onClick = {
                                    expanded = false
                                    showDeleteConfirmDialog = true
                                },
                                icon = ic_delete,
                                color = MaterialTheme.colorScheme.error
                            )
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
                            visible = showDeleteConfirmDialog,
                            onDismiss = { showDeleteConfirmDialog = false },
                            onConfirm = {
                                showDeleteConfirmDialog = false
                                onDelete(index)
                            },
                            title = "Delete ${item.name}?",
                            isDangerous = true,
                            message = "Are you sure you want to delete this folder? (This won't delete the file on your device.)",
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropDownContextItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    DropdownMenuItem(
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium)
        },
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = MenuDefaults.itemColors(
            textColor = color,
            leadingIconColor = color
        )
    )
}