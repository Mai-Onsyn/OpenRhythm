package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_bug_report
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun DebugSettings() {
    SettingsCard(
        title = "Debug",
        icon = ic_bug_report,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {

    }
}