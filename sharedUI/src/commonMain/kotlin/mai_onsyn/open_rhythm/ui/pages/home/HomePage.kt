package mai_onsyn.open_rhythm.ui.pages.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_exit_to_app
import mai_onsyn.open_rhythm.ui.icons.ic_library_music
import mai_onsyn.open_rhythm.ui.icons.ic_piano
import mai_onsyn.open_rhythm.ui.icons.ic_settings
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
import openrhythm.sharedui.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HomePage(
    useWideLayout: Boolean = false,
    onGotoExplorer: () -> Unit = {},
    onGotoFreePlay: () -> Unit = {},
    onGotoSettings: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    AnimatedContent(
        targetState = useWideLayout,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { wide ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ic_unknown,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                }
                Text(
                    text = "Open Rhythm",
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (wide) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) { NavigationList(
                    onGotoExplorer, onGotoFreePlay,
                    onGotoSettings, onExit
                ) }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { NavigationList(
                    onGotoExplorer, onGotoFreePlay,
                    onGotoSettings, onExit
                ) }
            }
        }
    }
}

@Composable
private fun NavigationList(
    onGotoExplorer: () -> Unit,
    onGotoFreePlay: () -> Unit,
    onGotoSettings: () -> Unit,
    onExit: () -> Unit
) {
    NavigationButton(
        displayText = "浏览文件",
        icon = ic_library_music,
        onClick = onGotoExplorer
    )
    NavigationButton(
        displayText = "自由演奏",
        icon = ic_piano,
        onClick = onGotoFreePlay
    )
    NavigationButton(
        displayText = "设置",
        icon = ic_settings,
        onClick = onGotoSettings
    )
    NavigationButton(
        displayText = "退出",
        icon = ic_exit_to_app,
        onClick = onExit
    )
}

@Composable
private fun NavigationButton(
    displayText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .widthIn(min = 128.dp)
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = displayText,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}