package mai_onsyn.open_rhythm.ui.pages.setting.categories.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_open_in_new
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*
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
        val licenseText by produceState("loading...") { value = Res.readBytes("files/LICENSE.txt").decodeToString() }
        Image(
            painter = painterResource(Res.drawable.round_256x),
            contentDescription = "app icon",
            modifier = Modifier
                .size(144.dp)
        )
        Text(
            text = "ver. 1.1",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        )
        HorizontalDivider(Modifier.width(500.dp).padding(top = 16.dp))
        var showLicenseDialog by remember { mutableStateOf(false) }
        ListItem(str(Res.string.set_about_openSourceLicense), { showLicenseDialog = true }) {
            Text(
                text = "GPL-3.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            DialogPopup(
                visible = showLicenseDialog,
                onDismissRequest = { showLicenseDialog = false }
            ) {
                LicenseText(licenseText, Modifier.width(500.dp).height(800.dp).padding(horizontal = 16.dp, vertical = 32.dp))
            }
        }
        ListItem(str(Res.string.set_about_thirdPartyLibraryList), { onEnter(1) }) {
            Icon(
                imageVector = ic_arrow_back,
                contentDescription = "forward",
                modifier = Modifier
                    .rotate(180f)
                    .size(20.dp)
            )
        }
        val uriHandler = LocalUriHandler.current
        ListItem(str(Res.string.set_about_repositoryHomepage), {
            uriHandler.openUri("https://github.com/Mai-Onsyn/OpenRhythm")
            Logger.i { "Jumped url: https://github.com/Mai-Onsyn/OpenRhythm" }
        }) {
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


@Composable
fun LicenseText(
    licenseText: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    val lines = remember(licenseText) {
        licenseText.replaceInnerLineBreaksWithSpace().lines()
    }

    SelectionContainer {
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding
        ) {
            itemsIndexed(
                items = lines,
                key = { index, _ -> index }
            ) { _, line ->
                Text(
                    text = line.ifEmpty { " " },
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun String.replaceInnerLineBreaksWithSpace(): String {
    val n = length
    if (n == 0) return this
    val sb = StringBuilder(n)
    var i = 0
    while (i < n) {
        val c = this[i]
        if (c == '\n' || c == '\r') {
            var j = i + 1
            while (j < n && (this[j] == '\n' || this[j] == '\r')) j++

            val prevOk = i > 0 && !this[i - 1].isWhitespace()
            val nextOk = j < n && !this[j].isWhitespace()

            if (prevOk && nextOk) sb.append(' ')
            else sb.append(this, i, j)
            i = j
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}