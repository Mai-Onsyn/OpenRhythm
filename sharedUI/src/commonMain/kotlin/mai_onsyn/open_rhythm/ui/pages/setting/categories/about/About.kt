package mai_onsyn.open_rhythm.ui.pages.setting.categories.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_open_in_new
import openrhythm.sharedui.generated.resources.Res
import openrhythm.sharedui.generated.resources.round_256x
import org.jetbrains.compose.resources.painterResource

@Composable
fun About() {
    Box(Modifier.fillMaxSize()) {
        var screen by remember { mutableStateOf(0) }
        AboutMainScreen { screen = it }
        when (screen) {
            1 -> LicenseView { screen = 0 }
        }
    }
}

@Composable
private fun BoxScope.AboutMainScreen(onEnter: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.align(Alignment.Center)
    ) {
        Image(
            painter = painterResource(Res.drawable.round_256x),
            contentDescription = "app icon",
            modifier = Modifier
                .size(144.dp)
        )
        Text(
            text = "ver. 1.0.0",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        )
        HorizontalDivider(Modifier.width(500.dp).padding(top = 16.dp))
        ListItem("Open Source License") {
            Text(
                text = "GPL-3.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ListItem("Third-Party Library List", { onEnter(1) }) {
            Icon(
                imageVector = ic_arrow_back,
                contentDescription = "forward",
                modifier = Modifier
                    .rotate(180f)
                    .size(20.dp)
            )
        }
        val uriHandler = LocalUriHandler.current
        ListItem("Repository Homepage", { uriHandler.openUri("https://github.com/Mai-Onsyn/OpenRhythm") }) {
            Icon(
                imageVector = ic_open_in_new,
                contentDescription = "github",
                Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ListItem(
    preText: String,
    onclick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onclick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(400.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(16.dp)
        ) {
            Text(
                text = preText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseView(onBack: () -> Unit) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        LibrariesContainer(libraries, Modifier.fillMaxHeight())
    }
}