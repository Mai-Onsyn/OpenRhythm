package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_bug_report
import mai_onsyn.open_rhythm.ui.pages.library.cachedMidiFiles
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun DebugSettings() {
    SettingsCard(
        title = "Debug",
        icon = ic_bug_report,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = "Don't parse midi",
            description = "NOT RECOMMENDED: Only you want the original track",
            initial = Global.settings.UseParserV1,
            onToggled = {
                Global.settings.UseParserV1 = it
                cachedMidiFiles.clear()
            }
        )

        fold("Overlay layer") {
            itemWithSwitch(
                name = "Show current tick",
                description = "The MIDI tick position at the bottom boundary of the waterfall flow",
                initial = Global.settings.ShowCurrentTick,
                onToggled = { Global.settings.ShowCurrentTick = it }
            )
            itemWithSwitch(
                name = "Show FPS",
                description = "Frame per second of midi waterfall flow",
                initial = Global.settings.ShowFps,
                onToggled = { Global.settings.ShowFps = it }
            )
            itemWithSwitch(
                name = "Show frame time",
                description = "The interval millisecond time between two waterfall stream frames",
                initial = Global.settings.ShowFrameTime,
                onToggled = { Global.settings.ShowFrameTime = it }
            )
            itemWithSwitch(
                name = "Show rendering note count",
                description = "The total number of notes currently rendered on the screen",
                initial = Global.settings.ShowRenderingNoteCount,
                onToggled = { Global.settings.ShowRenderingNoteCount = it }
            )
            itemWithSwitch(
                name = "Show active note count",
                description = "Total number of notes currently active",
                initial = Global.settings.ShowActiveNoteCount,
                onToggled = { Global.settings.ShowActiveNoteCount = it }
            )
        }
    }
}