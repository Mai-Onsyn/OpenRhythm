package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_bug_report
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun DebugSettings() {
    SettingsCard(
        title = str(Res.string.set_debug_title),
        icon = ic_bug_report,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = str(Res.string.set_debug_dontParseMidi),
            description = str(Res.string.set_debug_dontParseMidi_desc),
            initial = Global.settings.UseParserV1,
            onToggled = {
                Global.settings.UseParserV1 = it
                Global.fileLoader.clearCache()
                Logger.d { "Cleared loaded MIDI files cache" }
            }
        )

        itemWithSwitch(
            name = str(Res.string.set_debug_showDebugInfos),
            description = str(Res.string.set_debug_showDebugInfos_desc),
            initial = Global.settings.ShowDebugInfos,
            onToggled = { Global.settings.ShowDebugInfos = it }
        )
    }
}