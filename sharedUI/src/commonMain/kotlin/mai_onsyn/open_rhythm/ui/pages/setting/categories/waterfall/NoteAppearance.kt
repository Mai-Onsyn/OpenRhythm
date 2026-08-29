package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import kotlin.math.roundToInt

@Composable
fun NoteAppearance() {
    SettingsCard(
        title = "Note",
        icon = ic_music_note,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = "Draw pitch labels",
            initial = Global.settings.DrawPitchLabels,
            onToggled = { Global.settings.DrawPitchLabels = it }
        )
        itemWithSwitch(
            name = "Draw note shadow",
            initial = Global.settings.DrawNoteShadow,
            onToggled = { Global.settings.DrawNoteShadow = it }
        )
        item("Opacity", verticalLayout = true) {
            var opacity by remember { mutableStateOf((Global.settings.NoteOpacity * 100).roundToInt()) }
            SliderWithSuffix(
                value = opacity,
                onValueChanged = {
                    opacity = it
                    Global.settings.NoteOpacity = it / 100f
                },
                steps = 5,
                range = 0..100,
                extraSuffix = "%"
            )
        }
        animatedSwitch(
            visible = Global.settings.NoteOpacity < 1f,
            name = "Apply opacity to keyboard",
            description = "Make the notes that light up on the keyboard have the same opacity",
            initial = Global.settings.OpacityAffectKeyboard,
            onToggled = { Global.settings.OpacityAffectKeyboard = it }
        )
        item("Round coner percent", verticalLayout = true) {
            var value by remember { mutableStateOf((Global.settings.NoteRoundConerPercent * 100).roundToInt()) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Global.settings.NoteRoundConerPercent = it / 100f
                },
                steps = 5,
                range = 0..100,
                extraSuffix = "%"
            )
        }
        item("Quarter note height", verticalLayout = true) {
            var value by remember { mutableStateOf(Global.settings.QuarterNoteDpHeight.roundToInt()) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Global.settings.QuarterNoteDpHeight = it.toFloat()
                },
                steps = 10,
                range = 10..480,
                extraSuffix = "dp"
            )
        }
    }
}