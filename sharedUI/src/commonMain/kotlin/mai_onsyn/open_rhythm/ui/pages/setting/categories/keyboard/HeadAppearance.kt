package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_page_header
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun HeadAppearance() {
    SettingsCard(
        title = str(Res.string.set_kbd_title_head),
        icon = ic_page_header,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemWithSwitch(
            name = str(Res.string.set_kbd_show_drag_area),
            description = str(Res.string.set_kbd_show_drag_area_desc),
            initial = Global.settings.EnableKeyboardDragArea,
            onToggled = { Global.settings.EnableKeyboardDragArea = it }
        )

        var showCustomColorSetting by remember { mutableStateOf(Global.settings.KeyboardDragAreaColor.isSpecified) }
        animatedItem(
            visible = Global.settings.EnableKeyboardDragArea,
            name = str(Res.string.set_kbd_drag_area_color)
        ) {
            var selected by remember { mutableStateOf(if (Global.settings.KeyboardDragAreaColor.isSpecified) 1 else 0) }
            val themeText = str(Res.string.set_kbd_drag_area_theme)
            val customText = str(Res.string.set_kbd_drag_area_custom)
            val choices = remember {
                listOf(
                    themeText to null,
                    customText to null
                )
            }
            ChoiceRow(
                choices = choices,
                selectedIndex = selected,
                onSelect = {
                    selected = it
                    showCustomColorSetting = selected == 1
                    if (showCustomColorSetting) {
                        Global.settings.KeyboardDragAreaColor = Global.settings.CustomKeyboardDragAreaColor
                    } else Global.settings.KeyboardDragAreaColor = Color.Unspecified
                },
                modifier = Modifier.height(40.dp),
                itemWidth = 96.dp,
                contentPadding = 0.dp,
                selectedColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }


        animatedItem(
            visible = Global.settings.EnableKeyboardDragArea && showCustomColorSetting,
            name = str(Res.string.set_kbd_custom_drag_area_color)
        ) {
            ColorSelector(
                enableAlpha = true,
                initialColor = Global.settings.CustomKeyboardDragAreaColor,
                onColorSelected = {
                    Global.settings.KeyboardDragAreaColor = it
                    Global.settings.CustomKeyboardDragAreaColor = it
                }
            )
        }

        itemWithSwitch(
            name = str(Res.string.set_kbd_draw_red_split_line),
            initial = Global.settings.DrawRedSplitLine,
            onToggled = { Global.settings.DrawRedSplitLine = it }
        )
    }
}