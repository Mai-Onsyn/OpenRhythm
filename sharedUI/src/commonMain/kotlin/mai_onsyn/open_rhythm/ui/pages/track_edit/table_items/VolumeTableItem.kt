package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.ui.modules.SliderWithSuffix

@Composable
fun BoxScope.VolumeTableItem(
    initial: Int,
    onChanged: (Int) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    SliderWithSuffix(
        modifier = Modifier.align(Alignment.Center),
        value = value,
        onValueChanged = { value = it },
        steps = 1,
        range = -50..50,
        onSlidStop = { onChanged(value) },
        centerValue = 0,
        valueMapping = {
            if (it >= 0) "+$it"
            else it.toString()
        },
        showTooltip = false
    )
}