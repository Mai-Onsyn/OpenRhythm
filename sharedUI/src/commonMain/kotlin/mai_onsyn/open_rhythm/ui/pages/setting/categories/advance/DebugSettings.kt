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

        fold(str(Res.string.set_debug_overlayLayer)) {
            itemWithSwitch(
                name = str(Res.string.set_debug_showCurrentTick),
                description = str(Res.string.set_debug_showCurrentTick_desc),
                initial = Global.settings.ShowCurrentTick,
                onToggled = { Global.settings.ShowCurrentTick = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_debug_showFps),
                description = str(Res.string.set_debug_showFps_desc),
                initial = Global.settings.ShowFps,
                onToggled = { Global.settings.ShowFps = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_debug_showFrameTime),
                description = str(Res.string.set_debug_showFrameTime_desc),
                initial = Global.settings.ShowFrameTime,
                onToggled = { Global.settings.ShowFrameTime = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_debug_showRenderingNoteCount),
                description = str(Res.string.set_debug_showRenderingNoteCount_desc),
                initial = Global.settings.ShowRenderingNoteCount,
                onToggled = { Global.settings.ShowRenderingNoteCount = it }
            )
            itemWithSwitch(
                name = str(Res.string.set_debug_showActiveNoteCount),
                description = str(Res.string.set_debug_showActiveNoteCount_desc),
                initial = Global.settings.ShowActiveNoteCount,
                onToggled = { Global.settings.ShowActiveNoteCount = it }
            )
        }
    }
}