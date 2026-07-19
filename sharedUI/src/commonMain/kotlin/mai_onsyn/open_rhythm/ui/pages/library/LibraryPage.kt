package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.nameWithoutExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.bridge.pickDirectoryWithPermission
import mai_onsyn.open_rhythm.ui.icons.ic_add
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.dialog.SingleLineInputDialog
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LibraryPage(
    useWideLayout: Boolean,
    onBack: () -> Unit,
    onEnterMidiDetail: () -> Unit
) {
    BackHandler { onBack() }

    Box(Modifier.safeDrawingPadding()) {
        AnimatedContent(
            targetState = useWideLayout,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { wide ->
            if (wide) WideLayout(onEnterMidiDetail)
            else WideLayout(onEnterMidiDetail)
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun WideLayout(
    onEnterMidiDetail: () -> Unit
) {
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            showNewFolderPopup = true
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
                onDismiss = { showNewFolderPopup = false },
                title = "Name for this New Folder",
                onConfirm = {
                    newFolderName = it
                    Singleton.settings.libraryFolderList.add(UILibraryFolder(newFolderName, newFolderDir))
                    showNewFolderPopup = false
                }
            )
        }
        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        var selectedFolderIndex by remember { mutableStateOf(0) }
        Row {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
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
                            text = Singleton.settings.libraryFolderList.size.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
//                val items = remember { mutableStateListOf(
//                    UILibraryFolder("a", "D:\\Users\\Desktop\\Files\\voice\\MIDI"),
//                    UILibraryFolder("b", "Desktop"),
//                    UILibraryFolder("c", "D:\\Users\\Desktop\\Files\\voice\\Midi Sounds TMP")
//                ) }
//                val items = remember { mutableStateListOf() }
                FolderManageRail(
                    modifier = Modifier,
                    items = Singleton.settings.libraryFolderList,
                    selectedIndex = selectedFolderIndex,
                    onSelect = { selectedFolderIndex = it },
                    onChange = { index, newValue ->
                        Singleton.settings.libraryFolderList[index] = newValue
                    },
                    onDelete = {
                        (Singleton.settings.libraryFolderList.size - 1).let { maxIdx ->
                            if (selectedFolderIndex > maxIdx) {
                                selectedFolderIndex = maxIdx
                            }
                        }
                        Singleton.settings.libraryFolderList.removeAt(it)
                    }
                )
            }

            VerticalDivider(Modifier.padding(horizontal = 24.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
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
                    path = Singleton.settings.libraryFolderList.let {
                        if (it.isEmpty()) ""
                        else it[min(selectedFolderIndex, it.size - 1)].dir
                    },
//                    path = Singleton.settings.libraryFolderList[selectedFolderIndex].dir,
                    onFileCountAvailable = { fileCount = it }
                )
            }
        }
    }
}

@Composable
private fun NarrowLayout(
    onEnterMidiDetail: () -> Unit
) {

}

@Serializable
data class UILibraryFolder(
    val name: String,
    val dir: String
)