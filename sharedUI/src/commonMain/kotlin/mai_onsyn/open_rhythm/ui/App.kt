package mai_onsyn.open_rhythm.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.zwander.kotlin.file.IPlatformFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.isActive
import kotlinx.io.readByteArray
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
import mai_onsyn.open_rhythm.ui.pages.AppNavigation
import mai_onsyn.open_rhythm.ui.theme.AppTheme
import mai_onsyn.open_rhythm.ui.theme.LocalThemeIsDark
import openrhythm.sharedui.generated.resources.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {

    AppNavigation(
        modifier = Modifier.fillMaxSize()
    )

//    var midi by remember { mutableStateOf<Midi?>(null) }
//    val trackColors = remember { _testOnly_GenerateTrackColors() }
//
//    LaunchedEffect(Unit) {
//        midi = _testOnly_LoadMidi()
//    }
//
//    var isPlaying by remember { mutableStateOf(false) }
////    OriginalApp()
//    midi?.let { m ->
//        MidiDownRegion(
//            modifier = Modifier.fillMaxSize(),
//            midi = m,
//            trackColors = trackColors,
//            isPlaying = isPlaying,
//            keyboardRatio = if (Singleton.settings.KeyboardAutoAspect) Singleton.settings.KeyboardAspectRatio else 0f,
//            onPlayStateChange = { isPlaying = it }
//        )
//    }
//    LaunchedEffect(Unit) {
//        delay(5000)
//        isPlaying = true
//    }

}

private fun _testOnly_GenerateTrackColors(): List<Color> = mutableListOf(
    Color(255, 182, 193),
    Color(220, 20, 60),
    Color(255, 105, 180),
    Color(218, 112, 214),
    Color(238, 130, 238),
    Color(255, 0, 255),
    Color(65, 105, 225),
    Color(176, 196, 222),
    Color(240, 248, 255),
    Color(0, 191, 255),
    Color(95, 158, 160),
    Color(0, 206, 209),
    Color(47, 79, 79),
    Color(0, 255, 127),
    Color(0, 128, 0),
    Color(173, 255, 47),
    Color(255, 255, 0),
    Color(128, 128, 0),
    Color(255, 165, 0),
    Color(205, 92, 92),
    Color(128, 0, 0),
).apply {
    this.shuffle(Random(114514))
}

suspend fun _testOnly_LoadMidi(): Midi? {
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\蓬莱伝説 v1.11.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\天球の奇蹟.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\U.N. Owen.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\最终鬼畜妹.mid", false)
    val file: IPlatformFile? = null
    var loaded = true
    if (file == null) {
        Logger.e { "Cant find test.mid" }
        loaded = false
    }
    if (loaded) {
        val byteArray = file!!.openInputStream()?.readByteArray()
        if (byteArray == null) {
            Logger.e { "Cant read test.mid" }
            loaded = false
        }
        if (loaded) return Midi.fromFile(file.nameWithoutExtension, byteArray!!.toList())
    }

    val openFilePicker = FileKit.openFilePicker() ?: return null
    val bytes = openFilePicker.readBytes()
    return Midi.fromFile(openFilePicker.nameWithoutExtension, bytes.toList())
}


@Composable
fun OriginalApp() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.cyclone),
            fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
            style = MaterialTheme.typography.displayLarge
        )

        var isRotating by remember { mutableStateOf(false) }

        val rotate = remember { Animatable(0f) }
        val target = 360f
        if (isRotating) {
            LaunchedEffect(Unit) {
                while (isActive) {
                    val remaining = (target - rotate.value) / target
                    rotate.animateTo(target, animationSpec = tween((1_000 * remaining).toInt(), easing = LinearEasing))
                    rotate.snapTo(0f)
                }
            }
        }

        Image(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp)
                .run { rotate(rotate.value) },
            imageVector = ic_unknown,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            contentDescription = null
        )

        ElevatedButton(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .widthIn(min = 200.dp),
            onClick = { isRotating = !isRotating },
            content = {
                Icon(ic_unknown, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    stringResource(if (isRotating) Res.string.stop else Res.string.run)
                )
            }
        )

        var isDark by LocalThemeIsDark.current
        val icon = remember(isDark) {
            if (isDark) ic_unknown
            else ic_unknown
        }

        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
            onClick = { isDark = !isDark },
            content = {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.theme))
            }
        )

        val uriHandler = LocalUriHandler.current
        TextButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
            onClick = { uriHandler.openUri("https://github.com/terrakok") },
        ) {
            Text(stringResource(Res.string.open_github))
        }
    }
}