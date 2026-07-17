package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import openrhythm.sharedui.generated.resources.Res
import openrhythm.sharedui.generated.resources.ic_dark_mode
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
            else NarrowLayout(onEnterMidiDetail)
        }

        Box(Modifier.padding(top = 8.dp, start = 8.dp)) {
            IconButton(
                onClick = onBack,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .size(56.dp, 32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_dark_mode),
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
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {},
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_dark_mode),
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
        }
        HorizontalDivider(Modifier.padding(vertical = 24.dp))

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
                            text = "5",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                var selectedIndex by remember { mutableStateOf(0) }
                FolderManageRail(
                    modifier = Modifier,
                    items = listOf(
                        LibraryFolder("a", "vgafgwa", 5),
                        LibraryFolder("b", "vgafgwa", 5),
                        LibraryFolder("c", "vgafgwa", 5)
                    ),
                    selectedIndex = selectedIndex
                ) { selectedIndex = it }
            }

            VerticalDivider(Modifier.padding(horizontal = 24.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
            ) {

            }
        }
    }
}

@Composable
private fun NarrowLayout(
    onEnterMidiDetail: () -> Unit
) {

}

private data class LibraryFolder(
    val name: String,
    val dir: String,
    val fileCount: Int,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolderManageRail(
    modifier: Modifier = Modifier,
    items: List<LibraryFolder>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}