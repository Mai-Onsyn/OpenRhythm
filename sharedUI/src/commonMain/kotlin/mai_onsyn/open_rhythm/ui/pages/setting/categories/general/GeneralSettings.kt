package mai_onsyn.open_rhythm.ui.pages.setting.categories.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_brightness_auto
import mai_onsyn.open_rhythm.ui.icons.ic_dark_mode
import mai_onsyn.open_rhythm.ui.icons.ic_light_mode
import mai_onsyn.open_rhythm.ui.icons.ic_palette
import mai_onsyn.open_rhythm.ui.pages.setting.ChoiceRow
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun GeneralSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.Center)) {
            SettingsCard(
                title = "Appearance",
                icon = ic_palette,
                modifier = Modifier
                    .widthIn(400.dp, 800.dp)
            ) {
                item("Theme") {
                    var selected by remember { mutableStateOf(Singleton.settings.DarkMode) }
                    val choices = remember { listOf(
                        null to ic_light_mode,
                        null to ic_dark_mode,
                        null to ic_brightness_auto
                    ) }
                    ChoiceRow(
                        choices = choices,
                        selectedIndex = selected,
                        onSelect = {
                            Singleton.settings.DarkMode = it
                            selected = it
                        },
                        modifier = Modifier.height(40.dp),
                        itemWidth = 48.dp,
                        contentPadding = 4.dp,
                        selectedColor = MaterialTheme.colorScheme.primary,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

                item("Primary Color") {
                    Text("bbb")
                }
            }
        }
    }
}