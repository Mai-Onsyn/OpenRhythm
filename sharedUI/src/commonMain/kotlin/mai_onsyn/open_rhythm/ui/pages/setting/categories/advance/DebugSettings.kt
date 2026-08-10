package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
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
            initial = Singleton.settings.UseParserV1,
            onToggled = {
                Singleton.settings.UseParserV1 = it
                cachedMidiFiles.clear()
            }
        )

        fold("Overlay layer") {
            itemWithSwitch(
                name = "Show current tick",
                description = "The MIDI tick position at the bottom boundary of the waterfall flow",
                initial = Singleton.settings.ShowCurrentTick,
                onToggled = { Singleton.settings.ShowCurrentTick = it }
            )
            itemWithSwitch(
                name = "Show frame time",
                description = "The interval millisecond time between two waterfall stream frames",
                initial = Singleton.settings.ShowFrameTime,
                onToggled = { Singleton.settings.ShowFrameTime = it }
            )
            itemWithSwitch(
                name = "Show rendering note count",
                description = "The total number of notes currently rendered on the screen",
                initial = Singleton.settings.ShowRenderingNoteCount,
                onToggled = { Singleton.settings.ShowRenderingNoteCount = it }
            )
            itemWithSwitch(
                name = "Show active note count",
                description = "Total number of notes currently active",
                initial = Singleton.settings.ShowActiveNoteCount,
                onToggled = { Singleton.settings.ShowActiveNoteCount = it }
            )
        }
    }
}