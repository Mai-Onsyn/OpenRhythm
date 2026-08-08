package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_piano
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
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
            initial = Singleton.settings.KeyboardAutoAspect,
            onToggled = { Singleton.settings.KeyboardAutoAspect = it }
        )

        animatedItem(
            visible = Singleton.settings.KeyboardAutoAspect,
            name = "Aspect ratio",
            description = "The keyboard aspect ratio for auto aspect",
            verticalLayout = true
        ) {
            var aspectRatio by remember { mutableStateOf(Singleton.settings.KeyboardAspectRatio.toInt()) }
            SliderWithSuffix(
                value = aspectRatio,
                onValueChanged = {
                    aspectRatio = it
                    Singleton.settings.KeyboardAspectRatio = it.toFloat()
                },
                range = 2..16,
                steps = 1
            )
        }

        item("White key color") {
            ColorSelector(
                initialColor = Singleton.settings.WhiteKeyColor,
                onColorSelected = { Singleton.settings.WhiteKeyColor = it }
            )
        }

        item("Black key color") {
            ColorSelector(
                initialColor = Singleton.settings.BlackKeyColor,
                onColorSelected = { Singleton.settings.BlackKeyColor = it }
            )
        }

        item("Overlay labels", "The append text to show the pitch of key") {
            val dropDownItems = remember { listOf(
                ContextDropDownMenuItem("None", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("Major", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("White", selectedContentColor = colorScheme.primary),
                ContextDropDownMenuItem("All", selectedContentColor = colorScheme.primary),
            ) }
            var expandDropMenu by remember { mutableStateOf(false) }
            ContextDropdownMenu(
                expanded = expandDropMenu,
                onDismissRequest = { expandDropMenu = false },
                items = dropDownItems,
                selectedIndex = Singleton.settings.OverlayLabelsMode,
                onSelect = { Singleton.settings.OverlayLabelsMode = it }
            ) {
                OutlinedButton(
                    onClick = { expandDropMenu = true },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .width(100.dp)
                ) {
                    Text(
                        text = dropDownItems.getOrNull(Singleton.settings.OverlayLabelsMode)?.label ?: "Error",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}