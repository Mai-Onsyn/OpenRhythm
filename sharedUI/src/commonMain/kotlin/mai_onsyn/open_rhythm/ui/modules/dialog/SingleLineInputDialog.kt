package mai_onsyn.open_rhythm.ui.modules.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.ui.modules.CompactOutlinedTextField
import mai_onsyn.open_rhythm.ui.modules.PrimaryOperationButton
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun SingleLineInputDialog(
    visible: Boolean,
    title: String,
    value: String = "",
    placeholderText: String? = null,
    errorHolderText: String? = null,
    icon: ImageVector? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    DialogPopup(
        visible = visible,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .size(400.dp, 300.dp)
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = title,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(Modifier.height(16.dp))

                var inputText by remember { mutableStateOf(value) }
                var isError by remember { mutableStateOf(false) }
                LaunchedEffect(isError) {
                    if (isError) {
                        delay(2000.milliseconds)
                        isError = false
                    }
                }
                CompactOutlinedTextField(
                    modifier = Modifier
                        .height(48.dp),
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = if (isError) { {
                        errorHolderText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } } else { {
                        placeholderText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } },
                    isError = isError,
                    onConfirm = {
                        if (inputText.isNotEmpty()) {
                            onConfirm(inputText)
                        } else isError = true
                    }
                )

                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PrimaryOperationButton("Confirm") {
                        if (inputText.isNotEmpty()) {
                            onConfirm(inputText)
                        } else isError = true
                    }
                    PrimaryOperationButton("Cancel", onDismiss)
                }
            }
        }
    }
}