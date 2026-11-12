package mai_onsyn.open_rhythm.ui.pages.setting.categories.advance

import androidx.compose.foundation.layout.size
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
import mai_onsyn.open_rhythm.bridge.Platform
import mai_onsyn.open_rhythm.bridge.PlatformInfos
import mai_onsyn.open_rhythm.ui.icons.ic_flash_on
import mai_onsyn.open_rhythm.ui.modules.CompactOutlinedTextField
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@Composable
fun PerformanceSettings() {
    SettingsCard(
        title = "Performance",
        icon = ic_flash_on,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        val items = mutableListOf(
            "Compose Canvas",
//            str(Res.string.set_experiment_useCachedWaterfall),
            "CPU Static Bitmap",
            "GPU Instanced"
        )
        val modeDescs = mutableListOf(
            "Use CPU to rendering the waterfall realtime, it's the safest choice",
            str(Res.string.set_experiment_useCachedWaterfall_desc),
            "Use GPU parallel rendering the waterfall realtime, to improve the performance extremely",
        )

        var showWaringDialog by remember { mutableStateOf(false) }
        var waringMessage by remember { mutableStateOf("") }
        itemWithDropDownMenu(
            name = "Rendering mode",
            description = modeDescs[Global.settings.WaterfallRenderingMode],
            items = items,
            initial = Global.settings.WaterfallRenderingMode,
            onSelected = {
                if (it == 2) when (PlatformInfos.platform) {
                    Platform.Windows -> {
                        if (!PlatformInfos.rendererBackend.contains("OPENGL")) {
                            waringMessage = "If you want to use GPU Instanced rendering mode, you have to restart the application, otherwise it could use an incredible amount of memory!"
                            showWaringDialog = true
                        }
                        Global.settings.WaterfallRenderingMode = it
                    }
                    Platform.Android -> Global.settings.WaterfallRenderingMode = it
                    else -> {
                        waringMessage = "The GPU Instanced rendering mode might not be available on your device."
                        showWaringDialog = true
                    }
                } else Global.settings.WaterfallRenderingMode = it
            },
            fixedWidth = Dp.Unspecified
        )

        ConfirmDialog(
            visible = showWaringDialog,
            onDismissRequest = { showWaringDialog = false },
            onConfirm = { showWaringDialog = false },
            title = "Waring",
            message = waringMessage,
            isDangerous = true,
            showCancel = false
        )


        animatedSwitch(
            visible = Global.settings.WaterfallRenderingMode == 2,
            name = "Full upload",
            description = "At the start of playback, upload all the notes to the GPU, trading memory usage(20B/Note) for ultimate rendering performance.",
            initial = Global.settings.UploadAllVBOs,
            onToggled = { Global.settings.UploadAllVBOs = it }
        )

        item(
            "Max event channel size",
            "The buffer size of the built-in MIDI event sender needs to be adjusted based on the device's performance."
        ) {
            val callback: (Int) -> Unit = {
                Global.settings.MaxMidiChannelSize = it
                Global.player.setMaxChannelSize(it)
            }
            CompactOutlinedTextField(
                modifier = Modifier.size(80.dp, 40.dp),
                value = Global.settings.MaxMidiChannelSize,
                onValueChange = callback,
                onConfirm = { callback(Global.settings.MaxMidiChannelSize) }
            )
        }
    }
}