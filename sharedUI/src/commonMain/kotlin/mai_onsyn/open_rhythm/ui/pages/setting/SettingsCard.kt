package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SettingItem(
    val name: String,
    val description: String?,
    val content: @Composable () -> Unit
)

class SettingsCardScope {
    private val items = mutableListOf<SettingItem>()

    @Composable
    fun item(
        name: String,
        description: String? = null,
        content: @Composable () -> Unit
    ) {
        items.add(SettingItem(name, description, content))
    }

    @Composable
    fun itemWithSwitch(
        name: String,
        description: String? = null,
        initial: Boolean = false,
        onToggled: (Boolean) -> Unit
    ) {
        item(name, description) {
            var checked by remember { mutableStateOf(initial) }
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onToggled(it)
                },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            )
        }
    }

    internal fun clear() = items.clear()
    internal fun getItems(): List<SettingItem> = items.toList()
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable SettingsCardScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Card(
            shape = MaterialTheme.shapes.small,
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            val scope = remember { SettingsCardScope() }
            scope.clear()
            scope.content()
            val items = scope.getItems()

            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider()
                }
                SettingItemRow(item.name, item.description, item.content)
            }
        }
    }
}

@Composable
private fun SettingItemRow(
    name: String,
    description: String?,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        content()
    }
}