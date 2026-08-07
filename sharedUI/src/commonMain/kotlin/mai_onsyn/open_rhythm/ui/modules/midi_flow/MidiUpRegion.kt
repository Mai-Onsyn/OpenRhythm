package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents
import kotlin.collections.set

@Composable
fun MidiUpRegion(
    modifier: Modifier = Modifier,
    keyboardRatio: Float = 0f,
) {
    val density = LocalDensity.current
    var keyboardHeight by remember { mutableStateOf(100.dp) }
    Column(
        modifier = modifier
            .onSizeChanged {
                if (keyboardRatio == 0f) return@onSizeChanged
                keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
            },
    ) {
        val activeKeys = remember { mutableStateMapOf<Int, Color>() }
        val notes = remember { mutableStateListOf<LiveNote>() }

        Box(
            Modifier
                .weight(1f)
                .background(Singleton.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
        ) {
            val platformContext = LocalPlatformContext.current
            val bgImageRequest by produceState<ImageRequest?>(null, Singleton.settings.BackgroundImageDir) {
                val file = PlatformFile(Singleton.settings.BackgroundImageDir)
                withContext(Dispatchers.IO) {
                    value = if (file.exists() && file.isRegularFile()) {
                        if (Singleton.settings.OriginalBackgroundImageSize) ImageRequest.Builder(platformContext)
                            .data(file.readBytes())
                            .size(Size.ORIGINAL)
                            .build()
                        else ImageRequest.Builder(platformContext)
                            .data(file.readBytes())
                            .build()
                    } else null
                }
            }
            AsyncImage(
                model = bgImageRequest,
                contentDescription = "background image",
                modifier = Modifier
                    .alpha(Singleton.settings.BackgroundImageOpacity)
                    .blur(Singleton.settings.BackgroundImageBlurDp.dp)
                    .matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            MidiUpFlow(
                modifier = Modifier
                    .fillMaxSize(),
                notes = notes,
                color = Singleton.settings.KeyboardInteractionColor,
                drawOctaveLine = Singleton.settings.DrawOctaveLines,
                noteRoundPercent = Singleton.settings.NoteRoundConerPercent
            )
        }

        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            userActiveKey = activeKeys,
            onPress = { key, velocity ->
                activeKeys[key] = Singleton.settings.KeyboardInteractionColor
                Singleton.player.noteOn(key, velocity)

                notes.add(LiveNote(key, Time.nanos))
            },
            onRelease = { key ->
                activeKeys.remove(key)
                Singleton.player.noteOff(key)

                notes.firstOrNull { it.pitch == key && it.endNanos == null }?.let {
                    it.endNanos = Time.nanos
                }
            },
            onVerticalDragged = {
                keyboardHeight = max(64.dp, with(density) { keyboardHeight - it.toDp() })
            },
            draggableAreaColor = if (Singleton.settings.EnableKeyboardDragArea) Singleton.settings.KeyboardDragAreaColor else null,
            enableSplitRedLine = Singleton.settings.DrawRedSplitLine
        )

        BindInputDeviceEvents(
            activeKeys,
            { key, _ ->
                notes.add(LiveNote(key, Time.nanos))
            },
            { key ->
                notes.firstOrNull { it.pitch == key && it.endNanos == null }?.let {
                    it.endNanos = Time.nanos
                }
            }
        )
    }
}