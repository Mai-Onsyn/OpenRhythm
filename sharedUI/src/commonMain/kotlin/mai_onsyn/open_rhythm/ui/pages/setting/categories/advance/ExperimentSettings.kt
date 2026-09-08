package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.icons.ic_experiment
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun ExperimentSettings() {
    SettingsCard(
        title = str(Res.string.set_experiment_title),
        icon = ic_experiment,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        itemWithSwitch(
            name = str(Res.string.set_experiment_useCachedWaterfall),
            description = str(Res.string.set_experiment_useCachedWaterfall_desc),
            initial = Global.settings.UseCachedWaterfall,
            onToggled = { Global.settings.UseCachedWaterfall = it }
        )
    }
}