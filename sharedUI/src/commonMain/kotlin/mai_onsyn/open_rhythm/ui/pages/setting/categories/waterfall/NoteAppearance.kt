package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun NoteAppearance() {
    SettingsCard(
        title = "Note",
        icon = ic_music_note,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = "Draw pitch labels",
            initial = Singleton.settings.DrawPitchLabels,
            onToggled = { Singleton.settings.DrawPitchLabels = it }
        )
        itemWithSwitch(
            name = "Draw note shadow",
            initial = Singleton.settings.DrawNoteShadow,
            onToggled = { Singleton.settings.DrawNoteShadow = it }
        )
        item("Round coner percent", verticalLayout = true) {
            var value by remember { mutableStateOf((Singleton.settings.NoteRoundConerPercent * 100).toInt()) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Singleton.settings.NoteRoundConerPercent = it / 100f
                },
                steps = 5,
                range = 0..100,
                extraSuffix = "%"
            )
        }
        item("Quarter note height", verticalLayout = true) {
            var value by remember { mutableStateOf(Singleton.settings.QuarterNoteDpHeight.toInt()) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Singleton.settings.QuarterNoteDpHeight = it.toFloat()
                },
                steps = 10,
                range = 10..480,
                extraSuffix = "dp"
            )
        }
    }
}