package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class ContextDropDownMenuItem(
    val label: String? = null,
    val icon: ImageVector? = null,
    val contentColor: Color = Color.Unspecified,
    val selectedContentColor: Color = Color.Unspecified
)

@Composable
fun ContextDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    items: List<ContextDropDownMenuItem>,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.End,
    content: @Composable BoxScope.() -> Unit
) {
    var popupRegionWidth by remember { mutableStateOf(0) }
    var interactionRegionWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { interactionRegionWidth = it.width }
        ) {
            content()
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.onSizeChanged { popupRegionWidth = it.width },
            offset = DpOffset(
                x = with(density) { (interactionRegionWidth - popupRegionWidth).toDp() },
                y = 0.dp
            )
        ) {
            items.forEachIndexed { index, item ->
                if (item.icon == null && item.label == null) return@forEachIndexed

                val isSelected = index == selectedIndex

                DropDownContextItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        onDismissRequest()
                        onSelect(index)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropDownContextItem(
    item: ContextDropDownMenuItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
) {
    val color =
        if (isSelected && item.selectedContentColor.isSpecified) item.selectedContentColor
        else if (item.contentColor.isSpecified) item.contentColor
        else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        text = {
            item.label?.let {
                Text(
                    text = it,
                    style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium
                )
            }
        },
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        leadingIcon = item.icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        colors = MenuDefaults.itemColors(
            textColor = color,
            leadingIconColor = color
        )
    )
}