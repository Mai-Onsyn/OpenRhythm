package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.ui.icons.ic_piano
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import mai_onsyn.open_rhythm.ui.modules.LabeledRangeSlider
import mai_onsyn.open_rhythm.ui.modules.LabeledSliderWithPrefixSuffix
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun Body() {
    val colorScheme = MaterialTheme.colorScheme
    SettingsCard(
        title = "Body",
        icon = ic_piano,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = "Auto aspect ratio",
            description = "Auto adjust the aspect ratio when the window size changed",
            initial = Global.settings.KeyboardAutoAspect,
            onToggled = { Global.settings.KeyboardAutoAspect = it }
        )

        animatedItem(
            visible = Global.settings.KeyboardAutoAspect,
            name = "Aspect ratio",
            description = "The keyboard aspect ratio for auto aspect",
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

        item("White key color") {
            ColorSelector(
                initialColor = Global.settings.WhiteKeyColor,
                onColorSelected = { Global.settings.WhiteKeyColor = it }
            )
        }

        item("Black key color") {
            ColorSelector(
                initialColor = Global.settings.BlackKeyColor,
                onColorSelected = { Global.settings.BlackKeyColor = it }
            )
        }

        itemWithDropDownMenu(
            name = "Overlay labels",
            description = "The append text to show the pitch of key",
            initial = Global.settings.OverlayLabelsMode,
            onSelected = { Global.settings.OverlayLabelsMode = it },
            items = listOf("None", "Major", "White", "All")
        )

        item("Default pitch range", verticalLayout = true) {
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