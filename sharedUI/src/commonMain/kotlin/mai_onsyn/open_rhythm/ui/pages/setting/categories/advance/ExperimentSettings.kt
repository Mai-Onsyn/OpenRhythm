package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
//        itemWithSwitch(
//            name = str(Res.string.set_experiment_useCachedWaterfall),
//            description = str(Res.string.set_experiment_useCachedWaterfall_desc),
//            initial = Global.settings.UseCachedWaterfall,
//            onToggled = { Global.settings.UseCachedWaterfall = it }
//        )

        val items = mutableListOf(
            "CPU Realtime",
//            str(Res.string.set_experiment_useCachedWaterfall),
            "CPU Static",
            "GPU Instanced"
        )
        val modeDescs = mutableListOf(
            "Use CPU to rendering the waterfall realtime, it's the safest choice",
            str(Res.string.set_experiment_useCachedWaterfall_desc),
            "Use GPU parallel rendering the waterfall realtime, to improve the performance extremely",
        )
        itemWithDropDownMenu(
            name = "Rendering mode",
            description = modeDescs[Global.settings.WaterfallRenderingMode],
            items = items,
            initial = Global.settings.WaterfallRenderingMode,
            onSelected = { Global.settings.WaterfallRenderingMode = it },
            fixedWidth = Dp.Unspecified
        )
    }
}