package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.ui.icons.ic_piano
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.modules.LabeledSliderWithPrefixSuffix
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun Body() {
    SettingsCard(
        title = str(Res.string.set_kbd_title_body),
        icon = ic_piano,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = str(Res.string.set_kbd_auto_aspect_ratio),
            description = str(Res.string.set_kbd_auto_aspect_ratio_desc),
            initial = Global.settings.KeyboardAutoAspect,
            onToggled = { Global.settings.KeyboardAutoAspect = it }
        )

        animatedItem(
            visible = Global.settings.KeyboardAutoAspect,
            name = str(Res.string.set_kbd_aspect_ratio),
            description = str(Res.string.set_kbd_aspect_ratio_desc),
            verticalLayout = true
        ) {
            var aspectRatio by remember { mutableStateOf(Global.settings.KeyboardAspectRatio.toInt()) }
            SliderWithSuffix(
                value = aspectRatio,
                onValueChanged = {
                    aspectRatio = it
                    Global.settings.KeyboardAspectRatio = it.toFloat()
                },
                range = 2..16,
                steps = 1
            )
        }

        item(str(Res.string.set_kbd_white_key_color)) {
            ColorSelector(
                enableAlpha = true,
                initialColor = Global.settings.WhiteKeyColor,
                onColorSelected = { Global.settings.WhiteKeyColor = it }
            )
        }

        item(str(Res.string.set_kbd_black_key_color)) {
            ColorSelector(
                enableAlpha = true,
                initialColor = Global.settings.BlackKeyColor,
                onColorSelected = { Global.settings.BlackKeyColor = it }
            )
        }

        itemWithDropDownMenu(
            name = str(Res.string.set_kbd_overlay_labels),
            description = str(Res.string.set_kbd_overlay_labels_desc),
            initial = Global.settings.OverlayLabelsMode,
            onSelected = { Global.settings.OverlayLabelsMode = it },
            items = listOf("None", "Major", "White", "All")
        )

        item(str(Res.string.set_kbd_default_pitch_range), verticalLayout = true) {
            LabeledSliderWithPrefixSuffix(
                lValue = Global.settings.MinPitch,
                rValue = Global.settings.MaxPitch,
                range = 0..127,
                onLValueChanged = { Global.settings.MinPitch = it },
                onRValueChanged = { Global.settings.MaxPitch = it },
                minLength = 12,
                valueMapping = { Note.toString(it) }
            )
        }
    }
}