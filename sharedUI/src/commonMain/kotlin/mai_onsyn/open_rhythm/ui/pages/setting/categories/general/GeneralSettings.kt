package mai_onsyn.open_rhythm.ui.pages.setting.categories.general

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_add
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_selector_tool
import mai_onsyn.open_rhythm.ui.icons.ic_brightness_auto
import mai_onsyn.open_rhythm.ui.icons.ic_dark_mode
import mai_onsyn.open_rhythm.ui.icons.ic_light_mode
import mai_onsyn.open_rhythm.ui.icons.ic_palette
import mai_onsyn.open_rhythm.ui.modules.ColorPickerDialog
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import kotlin.collections.listOf

@Composable
fun GeneralSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppearanceSettings()
            InteractionSettings()
        }
    }
}

@Composable
private fun AppearanceSettings() {
    SettingsCard(
        title = "Appearance",
        icon = ic_palette,
        modifier = Modifier
            .widthIn(400.dp, 800.dp)
    ) {
        item("Theme") {
            var selected by remember { mutableStateOf(Singleton.settings.DarkMode) }
            val choices = remember {
                listOf(
                    null to ic_light_mode,
                    null to ic_dark_mode,
                    null to ic_brightness_auto
                )
            }
            ChoiceRow(
                choices = choices,
                selectedIndex = selected,
                onSelect = {
                    Singleton.settings.DarkMode = it
                    selected = it
                },
                modifier = Modifier.height(32.dp),
                itemWidth = 48.dp,
                contentPadding = 4.dp,
                selectedColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        item("Primary Color") {
            val colors = remember {
                listOf(
                    Color(0xFF485F84),
                    Color(0xFF6750A4),
                    Color(0xFF7FFFAA),
                    Color(0xFFF4A460)
                )
            }
            var selected by remember {
                mutableStateOf(colors.indexOf(Singleton.settings.PrimarySeedColor))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEachIndexed { index, color ->
                    val isSelected = selected == index
                    Box(
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selected = index
                                Singleton.settings.PrimarySeedColor = color
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                val isCustomSelected = selected == -1
                var colorPickerVisible by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isCustomSelected) 1.dp else 0.dp,
                            color = if (isCustomSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            colorPickerVisible = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Singleton.settings.UserSpecifiedPrimarySeedColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ic_add,
                            contentDescription = "custom color",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                ColorPickerDialog(
                    visible = colorPickerVisible,
                    initialColor = Singleton.settings.UserSpecifiedPrimarySeedColor,
                    onDismissRequest = { colorPickerVisible = false },
                    onConfirmRequest = {
                        colorPickerVisible = false
                        Singleton.settings.PrimarySeedColor = it
                        Singleton.settings.UserSpecifiedPrimarySeedColor = it
                        selected = -1
                    }
                )
            }
        }
    }
}

@Composable
private fun InteractionSettings() {
    SettingsCard(
        title = "Interaction",
        icon = ic_arrow_selector_tool,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = "Double tap to play/pause",
            description = "This is pretty easy to trigger by accident",
            initial = Singleton.settings.DoubleClickToPlayPause,
            onToggled = { Singleton.settings.DoubleClickToPlayPause = it }
        )
        itemWithSwitch(
            name = "Tap with two fingers to play/pause",
            description = "This might be tricky to handle",
            initial = Singleton.settings.DoubleFingerTapToPlayPause,
            onToggled = { Singleton.settings.DoubleFingerTapToPlayPause = it }
        )
    }
}