package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

@Composable
fun BoxScope.EnabledTableItem(
    initial: Boolean,
    onChanged: (Boolean) -> Unit
) {
    var enabled by remember { mutableStateOf(initial) }
    Checkbox(
        checked = enabled,
        onCheckedChange = {
            onChanged(it)
            enabled = it
        },
        modifier = Modifier
            .align(Alignment.CenterStart)
            .pointerHoverIcon(PointerIcon.Hand)
    )
}