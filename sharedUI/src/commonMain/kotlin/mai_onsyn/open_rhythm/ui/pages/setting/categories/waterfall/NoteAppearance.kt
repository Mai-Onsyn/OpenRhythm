package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*
import kotlin.math.roundToInt

@Composable
fun NoteAppearance() {
    SettingsCard(
        title = str(Res.string.set_note_title),
        icon = ic_music_note,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = str(Res.string.set_note_drawPitchLabels),
            initial = Global.settings.DrawPitchLabels,
            onToggled = { Global.settings.DrawPitchLabels = it }
        )
        itemWithSwitch(
            name = str(Res.string.set_note_drawShadow),
            initial = Global.settings.DrawNoteShadow,
            onToggled = { Global.settings.DrawNoteShadow = it }
        )
        item(str(Res.string.set_note_opacity), verticalLayout = true) {
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
            name = str(Res.string.set_note_opacityToKeyboard),
            description = str(Res.string.set_note_opacityToKeyboard_desc),
            initial = Global.settings.OpacityAffectKeyboard,
            onToggled = { Global.settings.OpacityAffectKeyboard = it }
        )
        item(str(Res.string.set_note_cornerPercent), verticalLayout = true) {
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
        item(str(Res.string.set_note_quarterHeight), verticalLayout = true) {
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
        item(str(Res.string.set_note_minDuration), verticalLayout = true) {
            var value by remember { mutableStateOf(Global.settings.BasicNoteMiniumDuration.let { if (it == 31) 8 else it }) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Global.settings.BasicNoteMiniumDuration = if (it == 8) 31 else it
                    Global.fileLoader.clearCache()
                },
                steps = 1,
                range = 0..8,
                valueMapping = {
                    if (it == 8) "OFF"
                    else (1 shl it).toString()
                }
            )
        }
        item(str(Res.string.set_note_minDrumDuration), verticalLayout = true) {
            var value by remember { mutableStateOf(Global.settings.DrumNoteMiniumDuration.let { if (it == 31) 8 else it }) }
            SliderWithSuffix(
                value = value,
                onValueChanged = {
                    value = it
                    Global.settings.DrumNoteMiniumDuration = if (it == 8) 31 else it
                    Global.fileLoader.clearCache()
                },
                steps = 1,
                range = 0..8,
                valueMapping = {
                    if (it == 8) "OFF"
                    else (1 shl it).toString()
                }
            )
        }
    }
}