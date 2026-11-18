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
        title = str(Res.string.set_perf_title),
        icon = ic_flash_on,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {
        val items = mutableListOf(
            "Compose Canvas",
            "CPU Static Bitmap",
            "GPU Instanced"
        )
        val modeDescs = mutableListOf(
            str(Res.string.set_perf_mode_canvas_desc),
            str(Res.string.set_perf_mode_cached_desc),
            str(Res.string.set_perf_mode_gl_desc),
        )

        val glNeedRestartMsg = str(Res.string.set_perf_glNeedRestart)
        val glUnavailableMsg = str(Res.string.set_perf_glUnavailable)

        var showWaringDialog by remember { mutableStateOf(false) }
        var waringMessage by remember { mutableStateOf("") }
        itemWithDropDownMenu(
            name = str(Res.string.set_perf_renderingMode),
            description = modeDescs[Global.settings.WaterfallRenderingMode],
            items = items,
            initial = Global.settings.WaterfallRenderingMode,
            onSelected = {
                if (it == 2) when (PlatformInfos.platform) {
                    Platform.Windows -> {
                        if (!PlatformInfos.rendererBackend.contains("OPENGL")) {
                            waringMessage = glNeedRestartMsg
                            showWaringDialog = true
                        }
                        Global.settings.WaterfallRenderingMode = it
                    }
                    Platform.Android -> Global.settings.WaterfallRenderingMode = it
                    else -> {
                        waringMessage = glUnavailableMsg
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
            title = str(Res.string.common_warning),
            message = waringMessage,
            isDangerous = true,
            showCancel = false
        )


        animatedSwitch(
            visible = Global.settings.WaterfallRenderingMode == 2,
            name = str(Res.string.set_perf_fullUpload),
            description = str(Res.string.set_perf_fullUpload_desc),
            initial = Global.settings.UploadAllVBOs,
            onToggled = { Global.settings.UploadAllVBOs = it }
        )

        item(
            str(Res.string.set_perf_maxChannelSize),
            str(Res.string.set_perf_maxChannelSize_desc)
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