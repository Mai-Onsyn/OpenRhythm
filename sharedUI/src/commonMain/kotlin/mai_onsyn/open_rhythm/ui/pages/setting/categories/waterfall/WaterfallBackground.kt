package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_palette
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun WaterfallBackground() {
    SettingsCard(
        title = "Background",
        icon = ic_palette,
        modifier = Modifier.fillMaxWidth()
    ) {
        var showCustomColorSetting by remember { mutableStateOf(Singleton.settings.WaterfallBackgroundColor.isSpecified) }
        item("Color") {
            var selected by remember { mutableStateOf(if (Singleton.settings.WaterfallBackgroundColor.isSpecified) 1 else 0) }
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
                        Singleton.settings.WaterfallBackgroundColor = Singleton.settings.CustomWaterfallBackgroundColor
                    } else Singleton.settings.WaterfallBackgroundColor = Color.Unspecified
                },
                modifier = Modifier.height(40.dp),
                itemWidth = 96.dp,
                contentPadding = 0.dp,
                selectedColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        animatedItem(showCustomColorSetting, "Custom Color") {
            ColorSelector(
                modifier = Modifier
                    .size(100.dp, 40.dp),
                initialColor = Singleton.settings.CustomWaterfallBackgroundColor,
                onColorSelected = {
                    Singleton.settings.CustomWaterfallBackgroundColor = it
                    Singleton.settings.WaterfallBackgroundColor = it
                }
            )
        }
    }
}