package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_page_header
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun HeadAppearance() {
    SettingsCard(
        title = "Head",
        icon = ic_page_header,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = "Show drag area",
            description = "Drag this area to adjust the height of midi keyboard",
            initial = Singleton.settings.EnableKeyboardDragArea,
            onToggled = { Singleton.settings.EnableKeyboardDragArea = it }
        )

        var showCustomColorSetting by remember { mutableStateOf(Singleton.settings.KeyboardDragAreaColor.isSpecified) }
        animatedItem(
            visible = Singleton.settings.EnableKeyboardDragArea,
            name = "Drag area color"
        ) {
            var selected by remember { mutableStateOf(if (Singleton.settings.KeyboardDragAreaColor.isSpecified) 1 else 0) }
            val choices = remember {
                listOf(
                    "Theme" to null,
                    "Custom" to null
                )
            }
            ChoiceRow(
                choices = choices,
                selectedIndex = selected,
                onSelect = {
                    selected = it
                    showCustomColorSetting = selected == 1
                    if (showCustomColorSetting) {
                        Singleton.settings.KeyboardDragAreaColor = Singleton.settings.CustomKeyboardDragAreaColor
                    } else Singleton.settings.KeyboardDragAreaColor = Color.Unspecified
                },
                modifier = Modifier.height(40.dp),
                itemWidth = 96.dp,
                contentPadding = 0.dp,
                selectedColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }


        animatedItem(
            visible = Singleton.settings.EnableKeyboardDragArea && showCustomColorSetting,
            name = "Custom drag area color"
        ) {
            ColorSelector(
                initialColor = Singleton.settings.CustomKeyboardDragAreaColor,
                onColorSelected = {
                    Singleton.settings.KeyboardDragAreaColor = it
                    Singleton.settings.CustomKeyboardDragAreaColor = it
                }
            )
        }

        itemWithSwitch(
            name = "Draw red split line",
            initial = Singleton.settings.DrawRedSplitLine,
            onToggled = { Singleton.settings.DrawRedSplitLine = it }
        )
    }
}