package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix

@Composable
fun BoxScope.VolumeTableItem(
    initial: Float,
    onChanged: (Float) -> Unit
) {
    var value by remember { mutableStateOf((initial * 100f).toInt()) }
    SliderWithSuffix(
        modifier = Modifier.align(Alignment.Center),
        value = value,
        onValueChanged = { value = it },
        steps = 1,
        range = -50..50,
        onSlidStop = { onChanged(value / 100f) },
        centerValue = 0,
        valueMapping = {
            if (it >= 0) "+$it"
            else it.toString()
        },
        extraSuffix = "%",
        showTooltip = false
    )
}