package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.icons.ic_shadow
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun KeyboardShadows() {
    SettingsCard(
        title = "Shadows",
        icon = ic_shadow,
        modifier = Modifier.fillMaxWidth()
    ) {
        item("Shadow color") {
            ColorSelector(
                initialColor = Singleton.settings.KeyboardShadowColor,
                onColorSelected = { Singleton.settings.KeyboardShadowColor = it }
            )
        }
    }
}