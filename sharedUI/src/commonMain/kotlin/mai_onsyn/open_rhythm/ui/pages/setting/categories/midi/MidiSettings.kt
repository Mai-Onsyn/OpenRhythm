package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_audio_file
import mai_onsyn.open_rhythm.ui.icons.ic_refresh
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiKeyBoard
import mai_onsyn.open_rhythm.ui.pages.library.cachedMidiFiles
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

const val UNKNOWN_DEVICE = "Unknown Device"

@Composable
fun MidiSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MidiInputSettings()
            MidiOutputSettings()
            MidiFileSettings()

            TestKeyboard()
        }
    }
}

@Composable
fun RefreshDeviceButton(
    scope: CoroutineScope,
    onRefresh: () -> Unit,
) {
    var iconRotation by remember { mutableStateOf(0f) }
    val iconRotationValue by animateFloatAsState(iconRotation, tween(durationMillis = 1000))
    IconButton(
        onClick = {
            iconRotation += 360
            scope.launch {
                Singleton.refreshMidiAccess()
                onRefresh()
            }
        },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = ic_refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .rotate(iconRotationValue)
        )
    }
}

@Composable
private fun MidiFileSettings() {
    SettingsCard(
        title = "File",
        icon = ic_audio_file,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = "Don't parse midi",
            description = "NOT RECOMMENDED: Only you want the original track",
            initial = Singleton.settings.UseParserV1,
            onToggled = {
                Singleton.settings.UseParserV1 = it
                cachedMidiFiles.clear()
            }
        )
    }
}

@Composable
private fun TestKeyboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(Singleton.settings.KeyboardAspectRatio)
    ) {
        val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }
        MidiKeyBoard(
            modifier = Modifier
                .fillMaxSize(),
            minPitch = 21,
            maxPitch = 108,
            userActiveKey = userActiveKeys,
            onPress = { pitch, velocity ->
                userActiveKeys[pitch] = Singleton.settings.KeyboardInteractionColor
                Singleton.player.noteOn(pitch, velocity)
            },
            onRelease = {
                userActiveKeys.remove(it)
                Singleton.player.noteOff(it)
            },
            draggableAreaColor = if (Singleton.settings.EnableKeyboardDragArea) Singleton.settings.KeyboardDragAreaColor else null,
            enableSplitRedLine = Singleton.settings.DrawRedSplitLine
        )

        BindInputDeviceEvents(userActiveKeys)
    }
}