package mai_onsyn.open_rhythm.ui.pages.setting.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import mai_onsyn.open_rhythm.ui.icons.*
import mai_onsyn.open_rhythm.ui.pages.setting.categories.about.About
import mai_onsyn.open_rhythm.ui.pages.setting.categories.advance.AdvancedSettings
import mai_onsyn.open_rhythm.ui.pages.setting.categories.general.GeneralSettings
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.KeyMappingSettings
import mai_onsyn.open_rhythm.ui.pages.setting.categories.keyboard.KeyboardSettings
import mai_onsyn.open_rhythm.ui.pages.setting.categories.midi.MidiSettings
import mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall.WaterfallSettings

enum class SettingsContent(
    val id: Int,
    val displayName: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
) {
    GUIDE(0, "Guide", ic_settings, {}),

    GENERAL(100, "General", ic_settings, ::GeneralSettings),
    MIDI(200, "MIDI", ic_instant_mix, ::MidiSettings),
    KEY_MAP(400, "Key Mapping", ic_keyboard, ::KeyMappingSettings),
    WATERFALL(500, "Waterfall", ic_waterfall_chart, ::WaterfallSettings),
    KEYBOARD(600, "Keyboard", ic_piano, ::KeyboardSettings),
    ADVANCE(700, "Advanced", ic_plumbing, ::AdvancedSettings),
    ABOUT(800, "About", ic_info, ::About)
}