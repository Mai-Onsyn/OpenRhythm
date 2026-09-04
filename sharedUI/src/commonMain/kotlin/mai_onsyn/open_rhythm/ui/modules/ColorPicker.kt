package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.materialkolor.ktx.toHex
import mai_onsyn.open_rhythm.ui.modules.dialog.DialogPopup
import openrhythm.sharedui.generated.resources.Res
import openrhythm.sharedui.generated.resources.common_cancel
import openrhythm.sharedui.generated.resources.common_confirm
import openrhythm.sharedui.generated.resources.common_hexCode
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

//private fun parseHexToColor(hex: String): Color? {
//    val clean = hex.removePrefix("#")
//    if (clean.length != 6) return null
//    return try {
//        val colorInt = clean.toLong(16).toInt() or 0xFF000000.toInt()
//        Color(colorInt)
//    } catch (e: Exception) {
//        null
//    }
//}

private fun parseHexToColor(hex: String): Color? {
    val clean = hex.removePrefix("#")
    return when (clean.length) {
        6 -> { // RRGGBB
            try {
                val colorInt = clean.toInt(16) or 0xFF000000.toInt()
                Color(colorInt)
            } catch (_: Exception) { null }
        }
        8 -> { // RRGGBBAA
            try {
                val r = clean.substring(2, 4).toInt(16)
                val g = clean.substring(4, 6).toInt(16)
                val b = clean.substring(6, 8).toInt(16)
                val a = clean.substring(0, 2).toInt(16)
                Color(r / 255f, g / 255f, b / 255f, a / 255f)
            } catch (_: Exception) { null }
        }
        else -> null
    }
}

//private fun colorToHex(color: Color): String {
//    val r = (color.red * 255).roundToInt()
//    val g = (color.green * 255).roundToInt()
//    val b = (color.blue * 255).roundToInt()
//    val a = (color.alpha * 255).roundToInt()
//
//    fun Int.format(): String = this.toString(16).padStart(2, '0').uppercase()
//
//    return if (a == 255) {
//        "#${r.format()}${g.format()}${b.format()}"
//    } else {
//        "#${r.format()}${g.format()}${b.format()}${a.format()}"
//    }
//}

private fun rgbToHsb(color: Color): Triple<Float, Float, Float> {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * ((g - b) / delta % 6)
        max == g -> 60f * ((b - r) / delta + 2)
        else -> 60f * ((r - g) / delta + 4)
    }.let { if (it < 0) it + 360f else it }
    val s = if (max == 0f) 0f else delta / max
    val v = max
    return Triple(h, s, v)
}

@Composable
fun ColorPickerDialog(
    visible: Boolean,
    initialColor: Color = Color.White,
    enableAlpha: Boolean = false,
    onColorChanged: (Color) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirmRequest: (Color) -> Unit = {}
) {
    DialogPopup(
        visible = visible,
        onDismissRequest = onDismissRequest
    ) {
        ColorPicker(
            initialColor = initialColor,
            enableAlpha = enableAlpha,
            onColorChanged = onColorChanged,
            onConfirmRequest = {
                onConfirmRequest(it)
                Logger.i { "Picked color: ${it.toHex()}" }
            },
            onCancelRequest = onDismissRequest,
            modifier = Modifier
                .width(400.dp)
        )
    }
}


@Composable
fun ColorPicker(
    initialColor: Color = Color.Red,
    enableAlpha: Boolean = false,
    onColorChanged: (Color) -> Unit,
    onConfirmRequest: (Color) -> Unit = {},
    onCancelRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hsv by remember { mutableStateOf(rgbToHsb(initialColor)) }
    var alpha by remember { mutableStateOf(initialColor.alpha) }

    val currentColor = remember(hsv.first, hsv.second, hsv.third, alpha) {
        Color.hsv(hsv.first, hsv.second, hsv.third, alpha)
    }

    var hexInput by remember(currentColor) {
        mutableStateOf(currentColor.toHex())
    }

    LaunchedEffect(currentColor) {
        onColorChanged(currentColor)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(currentColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium
                )
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val hueGradients = remember {
                listOf(
                    Color.Red, Color.Yellow, Color.Green,
                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                )
            }
            HsbSliderRow(
                label = "H",
                value = hsv.first,
                valueRange = 0f..360f,
                displayValue = "${hsv.first.roundToInt()}°",
                trackBrush = Brush.horizontalGradient(hueGradients),
                onValueChange = { hsv = hsv.copy(first = it) }
            )

            val satStartColor = remember(hsv.third) { Color.hsv(0f, 0f, hsv.third) }
            val satEndColor = remember(hsv.first, hsv.third) { Color.hsv(hsv.first, 1f, hsv.third) }
            HsbSliderRow(
                label = "S",
                value = hsv.second * 100f,
                valueRange = 0f..100f,
                displayValue = "${(hsv.second * 100).roundToInt()}%",
                trackBrush = Brush.horizontalGradient(listOf(satStartColor, satEndColor)),
                onValueChange = { hsv = Triple(hsv.first, it / 100f, hsv.third) }
            )

            val valEndColor = remember(hsv.first, hsv.second) { Color.hsv(hsv.first, hsv.second, 1f) }
            HsbSliderRow(
                label = "B",
                value = hsv.third * 100f,
                valueRange = 0f..100f,
                displayValue = "${(hsv.third * 100).roundToInt()}%",
                trackBrush = Brush.horizontalGradient(listOf(Color.Black, valEndColor)),
                onValueChange = { hsv = Triple(hsv.first, hsv.second, it / 100f) }
            )

            if (enableAlpha) HsbSliderRow(
                label = "A",
                value = alpha * 100f,
                valueRange = 0f..100f,
                displayValue = "${(alpha * 100).roundToInt()}%",
                trackBrush = Brush.horizontalGradient(listOf(Color.Transparent, Color.hsv(hsv.first, hsv.second, hsv.third, 1f))),
                onValueChange = { alpha = it / 100f }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            CompactOutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    val cleanInput = input.uppercase().filter { it.isDigit() || it in 'A'..'F' || it == '#' }
                    if (cleanInput.length <= if (enableAlpha) 9 else 7) {
                        hexInput = cleanInput
                        parseHexToColor(cleanInput)?.let { parsedColor ->
                            hsv = rgbToHsb(parsedColor)
                            alpha = parsedColor.alpha
                        }
                    }
                },
                label = { Text(stringResource(Res.string.common_hexCode)) },
                onConfirm = {
                    parseHexToColor(hexInput)?.let { parsedColor ->
                        hsv = rgbToHsb(parsedColor)
                        alpha = parsedColor.alpha
                    }
                },
                modifier = Modifier
                    .width(114.dp)
            )
            Spacer(Modifier.weight(1f))
            PrimaryOperationButton(stringResource(Res.string.common_confirm)) { onConfirmRequest(currentColor) }
            Spacer(Modifier.width(8.dp))
            PrimaryOperationButton(stringResource(Res.string.common_cancel), onCancelRequest)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HsbSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    trackBrush: Brush,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .requiredHeight(24.dp),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .pointerHoverIcon(PointerIcon.Hand)
                )
            },
            track = { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(trackBrush)
                        .pointerHoverIcon(PointerIcon.Hand)
                )
            }
        )

        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            modifier = Modifier.width(42.dp)
        )
    }
}