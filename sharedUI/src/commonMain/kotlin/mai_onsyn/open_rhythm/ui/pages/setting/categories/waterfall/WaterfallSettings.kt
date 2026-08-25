package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaterfallSettings() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().widthIn(max = 800.dp).verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.weight(1f))

        WaterfallBackground()
        NoteAppearance()

        Spacer(Modifier.weight(1f))
    }
}