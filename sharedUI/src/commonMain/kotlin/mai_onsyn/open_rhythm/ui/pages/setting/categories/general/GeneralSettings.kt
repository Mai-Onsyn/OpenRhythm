package mai_onsyn.open_rhythm.ui.pages.setting.categories.general

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.sudarshanmhasrup.localina.api.LocaleUpdater
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.*
import mai_onsyn.open_rhythm.ui.modules.ColorPickerDialog
import mai_onsyn.open_rhythm.ui.modules.NumberSpinner
import mai_onsyn.open_rhythm.ui.modules.getContrastTextColor
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.orderLocale
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun GeneralSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppearanceSettings()
            InteractionSettings()
            SecuritySettings()
        }
    }
}

@Composable
private fun AppearanceSettings() {
    SettingsCard(
        title = str(Res.string.set_general_titleAppearance),
        icon = ic_palette,
        modifier = Modifier
            .widthIn(400.dp, 800.dp)
    ) {
        item(str(Res.string.set_general_theme)) {
            var selected by remember { mutableStateOf(Global.settings.DarkMode) }
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
                    Global.settings.DarkMode = it
                    selected = it
                },
                modifier = Modifier.height(32.dp),
                itemWidth = 48.dp,
                contentPadding = 4.dp,
                selectedColor = MaterialTheme.colorScheme.primary,
                selectedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        item(str(Res.string.set_general_primaryColor)) {
            val colors = remember {
                listOf(
                    Color(0xFF485F84),
                    Color(0xFF6750A4),
                    Color(0xFF7FFFAA),
                    Color(0xFFF4A460)
                )
            }
            var selected by remember {
                mutableStateOf(colors.indexOf(Global.settings.PrimarySeedColor))
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
                                Global.settings.PrimarySeedColor = color
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
                            .background(Global.settings.UserSpecifiedPrimarySeedColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ic_add,
                            contentDescription = "custom color",
                            modifier = Modifier.size(16.dp),
                            tint = getContrastTextColor(Global.settings.UserSpecifiedPrimarySeedColor)
                        )
                    }
                }

                ColorPickerDialog(
                    visible = colorPickerVisible,
                    initialColor = Global.settings.UserSpecifiedPrimarySeedColor,
                    onDismissRequest = { colorPickerVisible = false },
                    onConfirmRequest = {
                        colorPickerVisible = false
                        Global.settings.PrimarySeedColor = it
                        Global.settings.UserSpecifiedPrimarySeedColor = it
                        selected = -1
                    }
                )
            }
        }

        itemWithDropDownMenu(
            name = str(Res.string.set_general_language),
            initial = Global.settings.Language,
            onSelected = {
                Global.settings.Language = it
                LocaleUpdater.updateLocale(orderLocale(it))
            },
            items = listOf("English", "简体中文"),
            fixedWidth = 120.dp
        )

        itemWithSwitch(
            name = str(Res.string.set_general_statusBarShadow),
            description = str(Res.string.set_general_statusBarShadow_desc),
            initial = Global.settings.StatusBarShadow,
            onToggled = { Global.settings.StatusBarShadow = it }
        )

        itemWithSwitch(
            name = str(Res.string.set_general_showMidiDeviceInfos),
            initial = Global.settings.ShowMidiDeviceInfoInHome,
            onToggled = { Global.settings.ShowMidiDeviceInfoInHome = it }
        )
    }
}

@Composable
private fun InteractionSettings() {
    SettingsCard(
        title = str(Res.string.set_general_titleInteraction),
        icon = ic_arrow_selector_tool,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithDropDownMenu(
            name = str(Res.string.set_general_screenRotation),
            description = str(Res.string.set_general_screenRotation_desc),
            initial = Global.settings.MobileScreenOrientation,
            onSelected = { Global.settings.MobileScreenOrientation = it },
            items = listOf(
                str(Res.string.set_general_orientationPortrait),
                str(Res.string.set_general_orientationLandscape),
                str(Res.string.set_general_orientationFree),
                str(Res.string.set_general_orientationSystem)
            ),
            fixedWidth = 120.dp
        )
        itemWithSwitch(
            name = str(Res.string.set_general_doubleTapToPlayPause),
            description = str(Res.string.set_general_doubleTapToPlayPause_desc),
            initial = Global.settings.DoubleClickToPlayPause,
            onToggled = { Global.settings.DoubleClickToPlayPause = it }
        )
        itemWithSwitch(
            name = str(Res.string.set_general_twoFingerTapToPlayPause),
            description = str(Res.string.set_general_twoFingerTapToPlayPause_desc),
            initial = Global.settings.DoubleFingerTapToPlayPause,
            onToggled = { Global.settings.DoubleFingerTapToPlayPause = it }
        )
        itemWithSwitch(
            name = str(Res.string.set_general_autoPlay),
            description = str(Res.string.set_general_autoPlay_desc),
            initial = Global.settings.AutoStartPlayback,
            onToggled = { Global.settings.AutoStartPlayback = it }
        )
        item(str(Res.string.set_general_playbackStartDistance), str(Res.string.set_general_playbackStartDistance_desc)) {
            NumberSpinner(
                value = Global.settings.PlaybackStartDistance,
                onValueChange = { Global.settings.PlaybackStartDistance = it },
                range = 0..16
            )
        }
    }
}

@Composable
fun SecuritySettings() {
    SettingsCard(
        title = str(Res.string.set_general_titleSecurity),
        icon = ic_security,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = str(Res.string.set_general_showFolderPathInLibrary),
            description = str(Res.string.set_general_showFolderPathInLibrary_desc),
            initial = Global.settings.ShowFolderPathInLibrary,
            onToggled = { Global.settings.ShowFolderPathInLibrary = it }
        )
    }
}