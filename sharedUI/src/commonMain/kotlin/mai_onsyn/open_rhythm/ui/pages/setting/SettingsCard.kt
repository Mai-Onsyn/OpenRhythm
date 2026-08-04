package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_drop_down

class SettingsCardScope(
    private val showDivider: Boolean = true
) {

    private var firstItem = true


    @Composable
    fun item(
        name: String,
        description: String? = null,
        content: @Composable () -> Unit
    ) {
        if (!firstItem && showDivider) {
            HorizontalDivider()
        }

        firstItem = false

        SettingItemRow(
            name = name,
            description = description,
            content = content
        )
    }

    @Composable
    fun animatedItem(
        visible: Boolean,
        name: String,
        description: String? = null,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            item(
                name = name,
                description = description,
                content = content
            )
        }
    }


    @Composable
    fun fold(
        title: String,
        content: @Composable SettingsCardScope.() -> Unit
    ) {
        if (!firstItem && showDivider) {
            HorizontalDivider()
        }

        firstItem = false

        FoldItem(
            title = title,
            content = content
        )
    }


    @Composable
    fun itemWithSwitch(
        name: String,
        description: String? = null,
        initial: Boolean = false,
        onToggled: (Boolean) -> Unit
    ) {
        item(name, description) {

            var checked by remember(initial) {
                mutableStateOf(initial)
            }

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
            SettingsCardScope().content()
        }
    }
}

@Composable
private fun FoldItem(
    title: String,
    content: @Composable SettingsCardScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
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
                targetValue = if (expanded) -180f else 0f
            )

            Icon(
                imageVector = ic_arrow_drop_down,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(iconRotation)
            )
        }


        AnimatedVisibility(visible = expanded) {
            Column {
                HorizontalDivider()

                SettingsCardScope().content()
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
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
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