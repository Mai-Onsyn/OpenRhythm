package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.*
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.modules.dialog.SingleLineInputDialog
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolderManageRail(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onChange: (Int, UILibraryFolder) -> Unit,
    onDelete: (Int) -> Unit,
    refresher: Int
) {
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
        Global.settings.libraryFolderList.apply {
            add(to.index, removeAt(from.index))
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        onSelect(to.index)
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        itemsIndexed(Global.settings.libraryFolderList, { _, item -> item.dir }) { index, item ->
            var lastIsDragging by remember { mutableStateOf(false) }
            ReorderableItem(reorderableLazyListState, item.dir) { isDragging ->
                if (isDragging && !lastIsDragging) {
                    onSelect(index)
                }
                lastIsDragging = isDragging
                FolderRow(
                    selected = selectedIndex == index,
                    onSelect = { onSelect(index) },
                    onDelete = { onDelete(index) },
                    item = item,
                    onChanged = { onChange(index, it) },
                    isDragging = isDragging,
                    refresher = refresher
                )
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.FolderRow(
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    item: UILibraryFolder,
    onChanged: (UILibraryFolder) -> Unit,
    isDragging: Boolean = false,
    refresher: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary.copy(alpha = 0.12f)
        else colorScheme.primary.copy(alpha = 0f)
    )
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(
        onClick = onSelect,
        color = bgColor,
        contentColor = colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .hoverable(interactionSource)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isHovered) Box(
                modifier = Modifier
                    .draggableHandle()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ic_reorder,
                    contentDescription = item.name,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            } else OpacitySurface(
                contentPadding = 8.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.draggableHandle()
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
                    overflow = TextOverflow.MiddleEllipsis
                )
                if (Global.settings.ShowFolderPathInLibrary) Text(
                    text = item.dir,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
            }
            var fileCount by rememberSaveable { mutableStateOf(0) }
            Text(
                text = "$fileCount files",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            LaunchedEffect(refresher) {
                fileCount = if (Global.fileLoader.isFolderLoaded(item.dir)) Global.fileLoader.loadFolder(item.dir).count()
                else withContext(Dispatchers.IO) {
                    val folder = PlatformFile(item.dir)
                    if (!folder.exists() && !folder.isDirectory()) {
                        return@withContext 0
                    }
                    val midiFiles = folder.list()
                    return@withContext midiFiles.count { it.extension.lowercase() == "mid" }
                }
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
            var selectedIndex by remember { mutableStateOf(-1) }
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
                onDismissRequest = { showRenameDialog = false },
                onConfirm = {
                    showRenameDialog = false
                    onChanged(item.copy(name = it))
                }
            )

            ConfirmDialog(
                visible = showPathInfoDialog,
                onDismissRequest = { showPathInfoDialog = false },
                onConfirm = { showPathInfoDialog = false },
                title = "Path of ${item.name}",
                message = item.dir
            )

            ConfirmDialog(
                visible = showDeleteConfirmDialog,
                onDismissRequest = { showDeleteConfirmDialog = false },
                onConfirm = {
                    showDeleteConfirmDialog = false
                    onDelete()
                },
                title = "Delete ${item.name}?",
                isDangerous = true,
                message = "Are you sure you want to delete this folder? \n(This won't delete the file on your device.)",
            )
        }
    }
}
