package mai_onsyn.open_rhythm.ui.pages.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.*
import openrhythm.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomePage(
    useWideLayout: Boolean = false,
    onGotoExplorer: () -> Unit = {},
    onGotoFreePlay: () -> Unit = {},
    onGotoSettings: () -> Unit = {}
) {
    AnimatedContent(
        targetState = useWideLayout,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { wide ->
        if (Global.settings.ShowMidiDeviceInfoInHome) Box(Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Icon(
                    imageVector = ic_settings_input_svideo,
                    contentDescription = "Input device",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Global.settings.enabledMidiInputDeviceList.joinToString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = Global.settings.SelectedOutputDeviceName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = ic_graphic_eq,
                    contentDescription = "Input device",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.foreground_512x),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Text(
                    text = "Open Rhythm",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (wide) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) { NavigationList(
                    onGotoExplorer, onGotoFreePlay, onGotoSettings
                ) }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { NavigationList(
                    onGotoExplorer, onGotoFreePlay, onGotoSettings
                ) }
            }
        }
    }
}

@Composable
private fun NavigationList(
    onGotoExplorer: () -> Unit,
    onGotoFreePlay: () -> Unit,
    onGotoSettings: () -> Unit
) {
    NavigationCard(
        title = stringResource(Res.string.home_title_explorer),
        description = stringResource(Res.string.home_desc_explorer),
        icon = ic_library_music,
        onClick = onGotoExplorer
    )
    NavigationCard(
        title = stringResource(Res.string.home_title_freePlay),
        description = stringResource(Res.string.home_desc_freePlay),
        icon = ic_piano,
        onClick = onGotoFreePlay
    )
    NavigationCard(
        title = stringResource(Res.string.home_title_settings),
        description = stringResource(Res.string.home_desc_settings),
        icon = ic_settings,
        onClick = onGotoSettings
    )
}

@Deprecated("Use NavigationCard instead")
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