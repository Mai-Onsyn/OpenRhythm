package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_add
import mai_onsyn.open_rhythm.ui.icons.ic_remove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSpinner(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange, // 指定范围，例如 1..100
    label: String,
    modifier: Modifier = Modifier
) {
    // 内部维护一个 String 状态，允许用户在输入过程中出现临时空值
    var textValue by remember(value) { mutableStateOf(value.toString()) }
    val interactionSource = remember { MutableInteractionSource() }

    // 统一边界安全校正函数
    val clampAndSettle = {
        val parsed = textValue.toIntOrNull() ?: range.first
        val clamped = parsed.coerceIn(range)
        textValue = clamped.toString()
        onValueChange(clamped)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. 减号按钮
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first,
            modifier = Modifier.size(36.dp).pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(ic_remove, contentDescription = "decrease", modifier = Modifier.size(18.dp))
        }

        // 2. 复用 A 方案的紧凑输入框
        BasicTextField(
            value = textValue,
            onValueChange = { input ->
                // 🎯 核心防线：只允许输入纯数字，且长度不超过最大值的位数
                if (input.isEmpty() || input.all { it.isDigit() }) {
                    textValue = input
                    // 如果能解析出合法的范围内数字，即时通知外部更新
                    input.toIntOrNull()?.let {
                        if (it in range) onValueChange(it)
                    }
                }
            },
            modifier = Modifier
                .width(72.dp) // 给一个合适的数字宽度
                .onFocusChanged { if (!it.isFocused) clampAndSettle() }, // 👈 失焦时自动强制校正边界
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center // 数字居中更好看
            ),
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, // 召唤数字键盘
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { clampAndSettle() })
        ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = textValue,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = { Text(label) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp), // 极致紧凑
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = MaterialTheme.shapes.small
                    )
                }
            )
        }

        // 3. 加号按钮
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last,
            modifier = Modifier.size(36.dp).pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(ic_add, contentDescription = "add", modifier = Modifier.size(18.dp))
        }
    }
}