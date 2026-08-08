package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.contrastRatio
import com.materialkolor.ktx.toHex
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup

@Composable
fun ColorSelector(
    modifier: Modifier = Modifier.size(100.dp, 40.dp),
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    var color by remember { mutableStateOf(initialColor) }
    var showColorPicker by remember { mutableStateOf(false) }
    Surface(
        onClick = { showColorPicker = true },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = color,
        shape = MaterialTheme.shapes.small,
        contentColor = getContrastTextColor(color)
    ) {
        Box(modifier.pointerHoverIcon(PointerIcon.Hand)) {
            Text(
                text = color.toHex(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    ColorPickerDialog(
        visible = showColorPicker,
        initialColor = color,
        onDismissRequest = { showColorPicker = false },
        onConfirmRequest = {
            showColorPicker = false
            color = it
            onColorSelected(it)
        }
    )
}

fun getContrastTextColor(backgroundColor: Color): Color {
    val blackContrast = backgroundColor.contrastRatio(Color.Black)
    val whiteContrast = backgroundColor.contrastRatio(Color.White)

    return if (whiteContrast > blackContrast) Color.White else Color.Black
}