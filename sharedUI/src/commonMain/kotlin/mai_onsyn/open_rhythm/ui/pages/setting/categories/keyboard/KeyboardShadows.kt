package mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_shadow
import mai_onsyn.open_rhythm.ui.modules.ColorSelector
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun KeyboardShadows() {
    SettingsCard(
        title = str(Res.string.set_kbd_title_shadows),
        icon = ic_shadow,
        modifier = Modifier.fillMaxWidth()
    ) {
        item(str(Res.string.set_kbd_shadow_color)) {
            ColorSelector(
                enableAlpha = true,
                initialColor = Global.settings.KeyboardShadowColor,
                onColorSelected = { Global.settings.KeyboardShadowColor = it }
            )
        }
    }
}