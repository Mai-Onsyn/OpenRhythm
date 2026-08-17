package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isRegularFile
import kotlinx.coroutines.launch
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.bridge.pickFileWithPermission
import mai_onsyn.open_rhythm.ui.icons.ic_delete
import mai_onsyn.open_rhythm.ui.icons.ic_wallpaper
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun WaterfallBackground() {
    SettingsCard(
        title = "Background",
        icon = ic_wallpaper,
        modifier = Modifier.fillMaxWidth()
    ) {
        var showCustomColorSetting by remember { mutableStateOf(Global.settings.WaterfallBackgroundColor.isSpecified) }
        item("Color") {
            var selected by remember { mutableStateOf(if (Global.settings.WaterfallBackgroundColor.isSpecified) 1 else 0) }
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
                        Global.settings.WaterfallBackgroundColor = Global.settings.CustomWaterfallBackgroundColor
                    } else Global.settings.WaterfallBackgroundColor = Color.Unspecified
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
                initialColor = Global.settings.CustomWaterfallBackgroundColor,
                onColorSelected = {
                    Global.settings.CustomWaterfallBackgroundColor = it
                    Global.settings.WaterfallBackgroundColor = it
                }
            )
        }

        var bgImageDir by remember { mutableStateOf(Global.settings.BackgroundImageDir.let { it.ifBlank { null } }) }
        item(
            "Image",
            bgImageDir
        ) {
            Row {
                IconButton(
                    onClick ={
                        bgImageDir = null
                        Global.settings.BackgroundImageDir = ""
                    },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = ic_delete,
                        contentDescription = "clear",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.width(8.dp))
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            FileKit.pickFileWithPermission()?.let { file ->
                                if (file.isRegularFile() && file.extension == "png" || file.extension == "jpg") {
                                    bgImageDir = file.absolutePath()
                                    Global.settings.BackgroundImageDir = file.absolutePath()
                                }
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(
                        text = "Select",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        val showBgImageSetting by derivedStateOf { !bgImageDir.isNullOrBlank() }
        animatedFold(showBgImageSetting, "Image Settings") {
            itemWithSwitch(
                name = "Original image size",
                description = "This can use a lot of memory for big images",  // 这得吃不少内存，先生
                initial = Global.settings.OriginalBackgroundImageSize,
                onToggled = { Global.settings.OriginalBackgroundImageSize = it }
            )
            item("Image opacity", verticalLayout = true) {
                var value by remember { mutableStateOf((Global.settings.BackgroundImageOpacity * 100).toInt()) }
                SliderWithSuffix(
                    value = value,
                    onValueChanged = {
                        value = it
                        Global.settings.BackgroundImageOpacity = value / 100f
                    },
                    range = 0..100,
                    steps = 1,
                    extraSuffix = "%"
                )
            }
            item("Image blur", verticalLayout = true) {
                val valueMappingList = remember {
                    val breakPoints = listOf(
                        0 to 1,
                        20 to 5,
                        50 to 10,
                        100 to 50,
                        500 to 100,
                    )
                    val array = IntArray(41)
                    var currentStep = 0
                    var outputValue = 0
                    for (i in 0..40) {
                        outputValue += currentStep
                        array[i] = outputValue
                        breakPoints.forEach { if (it.first == outputValue) currentStep = it.second }
                    }
                    array
                }
                val valueMapping: (Int) -> String = { x -> valueMappingList[x].toString() }
                var value by remember { mutableStateOf(
                    valueMappingList.indexOf(
                        Global.settings.BackgroundImageBlurDp.toInt()
                    ).let { if (it != -1) it else 0 }
                ) }
                SliderWithSuffix(
                    value = value,
                    onValueChanged = {
                        value = it
                        Global.settings.BackgroundImageBlurDp = valueMapping(value).toFloat()
                    },
                    range = 0..40,
                    steps = 1,
                    extraSuffix = "dp",
                    valueMapping = valueMapping
                )
            }
        }

        fold("Octave lines") {
            itemWithSwitch(
                name = "Enable",
                initial = Global.settings.DrawOctaveLines,
                onToggled = { Global.settings.DrawOctaveLines = it }
            )

            item("Color") {
                ColorSelector(
                    initialColor = Global.settings.OctaveLineColor,
                    onColorSelected = { Global.settings.OctaveLineColor = it }
                )
            }

            item("Thickness", verticalLayout = true) {
                var mappedValue by remember { mutableStateOf((Global.settings.OctaveLineThickness * 10).toInt()) }
                SliderWithSuffix(
                    value = mappedValue,
                    onValueChanged = {
                        Global.settings.OctaveLineThickness = mappedValue / 10f
                        mappedValue = it
                    },
                    steps = 1,
                    range = 1..40,
                    valueMapping = { "${it/10f}" },
                    extraSuffix = "dp",
                )
            }
        }

        fold("Section lines") {
            itemWithSwitch(
                name = "Enable",
                initial = Global.settings.DrawSectionLines,
                onToggled = { Global.settings.DrawSectionLines = it }
            )

            item("Color") {
                ColorSelector(
                    initialColor = Global.settings.SectionLineColor,
                    onColorSelected = { Global.settings.SectionLineColor = it }
                )
            }

            item("Thickness", verticalLayout = true) {
                var mappedValue by remember { mutableStateOf((Global.settings.SectionLineThickness * 10).toInt()) }
                SliderWithSuffix(
                    value = mappedValue,
                    onValueChanged = {
                        Global.settings.SectionLineThickness = mappedValue / 10f
                        mappedValue = it
                    },
                    steps = 1,
                    range = 1..40,
                    valueMapping = { "${it/10f}" },
                    extraSuffix = "dp",
                )
            }
        }
    }
}