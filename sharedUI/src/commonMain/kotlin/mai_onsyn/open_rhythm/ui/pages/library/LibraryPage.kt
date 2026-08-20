package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.nameWithoutExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.bridge.pickDirectoryWithPermission
import mai_onsyn.open_rhythm.ui.icons.ic_add
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_refresh
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.modules.dialog.SingleLineInputDialog
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LibraryPage(
    useWideLayout: Boolean,
    onBack: () -> Unit,
    onEnterPlayMidiScreen: (MidiPlayMethod) -> Unit
) {
    BackHandler { onBack() }

    Box(Modifier.safeDrawingPadding()) {
        var selectedFolderIndex by rememberSaveable { mutableStateOf(0) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Column {
                    Text(
                        text = "MIDI Library",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Manage your MIDI folders and files",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))

                var showNewFolderExistedPopup by remember { mutableStateOf(false) }
                var showNewFolderPopup by remember { mutableStateOf(false) }
                var newFolderName by remember { mutableStateOf("") }
                var newFolderDir by remember { mutableStateOf("") }
                Button(
                    onClick = {
                        val scope = CoroutineScope(Dispatchers.Default)
                        scope.launch {
                            FileKit.pickDirectoryWithPermission()?.let {
                                newFolderName = it.nameWithoutExtension
                                newFolderDir = it.absolutePath()
                                if (Global.settings.libraryFolderList.find { folder -> folder.dir == newFolderDir } != null) {
                                    showNewFolderExistedPopup = true
                                }
                                else showNewFolderPopup = true
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = ic_add,
                            contentDescription = "Add Folder",
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Text(
                            text = "Add Folder",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                SingleLineInputDialog(
                    visible = showNewFolderPopup,
                    value = newFolderName,
                    onDismissRequest = { showNewFolderPopup = false },
                    title = "Name for this New Folder",
                    onConfirm = {
                        newFolderName = it
                        Global.settings.libraryFolderList.add(UILibraryFolder(newFolderName, newFolderDir))
                        showNewFolderPopup = false
                    }
                )

                ConfirmDialog(
                    visible = showNewFolderExistedPopup,
                    onDismissRequest = { showNewFolderExistedPopup = false },
                    title = "Error",
                    onConfirm = { showNewFolderExistedPopup = false },
                    message = "Folder \"$newFolderDir\" has been added"
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            if (useWideLayout) {
                WideLayout(onEnterPlayMidiScreen, selectedFolderIndex) { selectedFolderIndex = it }
            } else NarrowLayout(onEnterPlayMidiScreen, selectedFolderIndex) { selectedFolderIndex = it }
        }

        Box(Modifier.padding(top = 8.dp, start = 8.dp)) {
            IconButton(
                onClick = onBack,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .size(56.dp, 32.dp)
            ) {
                Icon(
                    imageVector = ic_arrow_back,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp).pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
    }
}

@Composable
private fun WideLayout(
    onEnterPlayMidiScreen: (MidiPlayMethod) -> Unit,
    selectedFolderIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row {
        FolderRail(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f),
            selectedFolderIndex,
            onSelect = onSelect
        )
        VerticalDivider(Modifier.padding(horizontal = 24.dp))
        FileRail(
            Modifier
                .fillMaxHeight()
                .weight(0.6f),
            selectedFolderIndex,
            onEnterPlayMidiScreen
        )
    }
}

@Composable
private fun NarrowLayout(
    onEnterPlayMidiScreen: (MidiPlayMethod) -> Unit,
    selectedFolderIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        FolderRail(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            selectedFolderIndex,
            onSelect = onSelect
        )
        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        FileRail(
            Modifier
                .fillMaxWidth()
                .weight(0.6f),
            selectedFolderIndex,
            onEnterPlayMidiScreen
        )
    }
}

@Composable
private fun FolderRail(
    modifier: Modifier,
    selectedFolderIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Library Folders",
                style = MaterialTheme.typography.titleMedium
            )

            OpacitySurface(contentPadding = 2.dp) {
                Text(
                    text = Global.settings.libraryFolderList.size.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = {

                },
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = ic_refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        FolderManageRail(
            modifier = Modifier.fillMaxWidth().weight(1f),
            selectedIndex = selectedFolderIndex,
            onSelect = onSelect,
            onChange = { index, newValue ->
                Global.settings.libraryFolderList[index] = newValue
            },
            onDelete = {
                (Global.settings.libraryFolderList.size - 1).let { maxIdx ->
                    if (selectedFolderIndex > maxIdx) {
                        onSelect(maxIdx)
                    }
                }
                Global.settings.libraryFolderList.removeAt(it)
            }
        )
    }
}

@Composable
private fun FileRail(
    modifier: Modifier,
    selectedFolderIndex: Int,
    onEnterPlayMidiScreen: (MidiPlayMethod) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        var fileCount by remember { mutableStateOf(0) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Midi Files",
                style = MaterialTheme.typography.titleMedium
            )

            OpacitySurface(contentPadding = 2.dp) {
                Text(
                    text = fileCount.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        FileManageRail(
            modifier = Modifier.fillMaxSize(),
            path = Global.settings.libraryFolderList.let {
                if (it.isEmpty()) ""
                else it[min(selectedFolderIndex, it.size - 1)].dir
            },
            onFileCountAvailable = { fileCount = it },
            onConfirm = onEnterPlayMidiScreen
        )
    }
}

@Serializable
data class UILibraryFolder(
    val name: String,
    val dir: String
)