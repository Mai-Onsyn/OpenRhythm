package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

@Composable
fun AppDefaultMidiKeyboard(
    modifier: Modifier = Modifier,
    minPitch: Int = Global.settings.MinPitch,
    maxPitch: Int = Global.settings.MaxPitch,
    midiActiveKey: Map<Int, Color> = emptyMap(),
    userActiveKey: Map<Int, Color> = emptyMap(),
    blackVerticalPercentage: Float = 0.64f,
    blackHorizontalPercentage: Float = 0.75f,
    spacing: Dp = 1.dp,
    appendTexts: Map<Int, String> = emptyMap(),
    centerAppendLayer: Boolean = false,
    whiteKeyColor: Color = Global.settings.WhiteKeyColor,
    blackKeyColor: Color = Global.settings.BlackKeyColor,
    darkPart: Color = Global.settings.KeyboardShadowColor,
    onPress: (Int, Int) -> Unit = { pitch, velocity -> },
    onRelease: (Int) -> Unit = {},
    draggableAreaColor: Color? = if (Global.settings.EnableKeyboardDragArea) Global.settings.KeyboardDragAreaColor else null,
    enableSplitRedLine: Boolean = Global.settings.DrawRedSplitLine,
    onVerticalDragged: (Float) -> Unit = {}
) {
    MidiKeyBoard(
        modifier = modifier,
        minPitch = minPitch,
        maxPitch = maxPitch,
        midiActiveKey = midiActiveKey,
        userActiveKey = userActiveKey,
        blackVerticalPercentage = blackVerticalPercentage,
        blackHorizontalPercentage = blackHorizontalPercentage,
        spacing = spacing,
        appendTexts = appendTexts,
        centerAppendLayer = centerAppendLayer,
        whiteKeyColor = whiteKeyColor,
        blackKeyColor = blackKeyColor,
        darkPart = darkPart,
        onPress = onPress,
        onRelease = onRelease,
        draggableAreaColor = draggableAreaColor,
        enableSplitRedLine = enableSplitRedLine,
        onVerticalDragged = onVerticalDragged
    )
}

@Composable
fun appliedOverlayLabels(): MutableMap<Int, String> {
    val appendTextMap = remember(
        Global.settings.OverlayLabelsMode,
        Global.settings.MinPitch,
        Global.settings.MaxPitch
    ) {
        mutableMapOf<Int, String>().apply {
            for (i in Global.settings.MinPitch..Global.settings.MaxPitch) {
                val access = when (Global.settings.OverlayLabelsMode) {
                    1 -> i % 12 == 0
                    2 -> !isBlackKey(i)
                    3 -> true
                    else -> false
                }
                if (access) {
                    put(i, Note.toString(i))
                }
            }
        }
    }
    return appendTextMap
}