package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.toHex
import mai_onsyn.open_rhythm.ui.modules.ColorPickerDialog

@Composable
fun BoxScope.ColorTableItem(
    initial: Color,
    onChanged: (Color) -> Unit
) {
    var color by remember { mutableStateOf(initial) }
    var showColorPicker by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showColorPicker = true },
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .width(100.dp)
            .height(40.dp)
            .align(Alignment.CenterStart)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = color.toHex(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    ColorPickerDialog(
        visible = showColorPicker,
        onDismissRequest = { showColorPicker = false },
        initialColor = color,
        onConfirmRequest = {
            showColorPicker = false
            color = it
            onChanged(color)
        }
    )
}