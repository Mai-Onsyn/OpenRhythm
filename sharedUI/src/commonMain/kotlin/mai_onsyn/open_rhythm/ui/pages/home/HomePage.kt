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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import openrhythm.sharedui.generated.resources.Res
import openrhythm.sharedui.generated.resources.ic_dark_mode
import org.jetbrains.compose.resources.painterResource

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
                            painter = painterResource(Res.drawable.ic_dark_mode),
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
        icon = painterResource(Res.drawable.ic_dark_mode),
        onClick = onGotoExplorer,
    )
    NavigationButton(
        displayText = "自由演奏",
        icon = painterResource(Res.drawable.ic_dark_mode),
        onClick = onGotoFreePlay,
    )
    NavigationButton(
        displayText = "设置",
        icon = painterResource(Res.drawable.ic_dark_mode),
        onClick = onGotoSettings,
    )
    NavigationButton(
        displayText = "退出",
        icon = painterResource(Res.drawable.ic_dark_mode),
        onClick = onExit,
    )
}

@Composable
private fun NavigationButton(
    displayText: String,
    icon: Painter,
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
                painter = icon,
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