package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_drop_down
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_drop_up
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
import kotlin.math.PI

sealed class SettingEntry {
    data class Single(
        val name: String,
        val description: String?,
        val content: @Composable () -> Unit
    ) : SettingEntry()

    data class Fold(
        val title: String,
        val items: List<SettingEntry>
    ) : SettingEntry()
    // 状态由渲染层管理，不在数据类中保存
}

class SettingsCardScope {
    private val entries = mutableListOf<SettingEntry>()

    @Composable
    fun item(
        name: String,
        description: String? = null,
        content: @Composable () -> Unit
    ) {
        entries.add(SettingEntry.Single(name, description, content))
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

    @Composable
    fun fold(
        title: String,
        content: @Composable SettingsCardScope.() -> Unit
    ) {
        val subScope = SettingsCardScope()
        subScope.content()
        entries.add(SettingEntry.Fold(title, subScope.entries.toList()))
    }

    internal fun clear() = entries.clear()
    internal fun getEntries() = entries.toList()
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
            val entries = scope.getEntries()

            RenderEntries(entries = entries, startWithDivider = false)
        }
    }
}

@Composable
private fun RenderEntries(
    entries: List<SettingEntry>,
    startWithDivider: Boolean
) {
    Column {
        entries.forEachIndexed { index, entry ->
            val showDividerAbove = when {
                index == 0 && !startWithDivider -> false
                else -> true
            }
            if (showDividerAbove) {
                HorizontalDivider()
            }

            when (entry) {
                is SettingEntry.Single -> {
                    SettingItemRow(entry.name, entry.description, entry.content)
                }
                is SettingEntry.Fold -> {
                    FoldItem(
                        title = entry.title,
                        items = entry.items
                    )
                }
            }
        }
    }
}

@Composable
private fun FoldItem(
    title: String,
    items: List<SettingEntry>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }  // 默认折叠

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) -180f else 0f,
                animationSpec = tween(easing = LinearOutSlowInEasing)
            )
            Icon(
                imageVector = ic_arrow_drop_down,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(iconRotation),
            )
        }

        AnimatedVisibility(visible = expanded) {
            RenderEntries(
                entries = items,
                startWithDivider = true
            )
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