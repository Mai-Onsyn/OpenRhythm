package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class ContextDropDownMenuItem(
    val label: String? = null,
    val icon: ImageVector? = null,
    val contentColor: Color = Color.Unspecified,
    val selectedContentColor: Color = Color.Unspecified,
    val category: String? = null
)

private sealed class MenuNode {
    data class Header(val title: String) : MenuNode()
    data class Leaf(val item: ContextDropDownMenuItem, val index: Int) : MenuNode()
}

@Composable
fun ContextDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    items: List<ContextDropDownMenuItem>,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.End,
    initiallyExpanded: Boolean = false, // 分类默认是否展开
    content: @Composable BoxScope.() -> Unit
) {
    var popupRegionWidth by remember { mutableStateOf(0) }
    var interactionRegionWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val expandedCategories = remember(items, selectedIndex) {
        mutableStateMapOf<String, Boolean>().apply {
            items.getOrNull(selectedIndex)?.category?.let { put(it, true) }
        }
    }

    Box(modifier = modifier) {
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
            offset = if (alignment == Alignment.End) DpOffset(
                x = with(density) { (interactionRegionWidth - popupRegionWidth).toDp() },
                y = 0.dp
            ) else DpOffset.Zero
        ) {
            val nodes = remember(items) { buildMenuNodes(items) }

            nodes.forEach { node ->
                when (node) {
                    is MenuNode.Header -> {
                        val isExpanded = expandedCategories[node.title] ?: initiallyExpanded
                        DropDownCategoryHeader(
                            title = node.title,
                            expanded = isExpanded,
                            childSelected = items[selectedIndex].category == node.title,
                            onClick = {
                                expandedCategories[node.title] = !isExpanded
                            }
                        )
                    }

                    is MenuNode.Leaf -> {
                        val category = node.item.category
                        val isExpanded = category == null ||
                                (expandedCategories[category] ?: initiallyExpanded)

                        AnimatedVisibility(
                            visible = isExpanded,
                        ) {
                            DropDownContextItem(
                                item = node.item,
                                isSelected = node.index == selectedIndex,
                                onClick = {
                                    onDismissRequest()
                                    onSelect(node.index)
                                },
                                modifier = Modifier.padding(
                                    start = if (category != null) 16.dp else 0.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildMenuNodes(items: List<ContextDropDownMenuItem>): List<MenuNode> {
    val nodes = mutableListOf<MenuNode>()
    val seenCategories = mutableSetOf<String>()

    items.forEachIndexed { index, item ->
        if (item.icon == null && item.label == null) return@forEachIndexed

        val category = item.category
        if (category != null && seenCategories.add(category)) {
            nodes += MenuNode.Header(category)
        }

        nodes += MenuNode.Leaf(item, index)
    }

    return nodes
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropDownContextItem(
    modifier: Modifier = Modifier,
    item: ContextDropDownMenuItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
) {
    val color =
        if (isSelected && item.selectedContentColor.isSpecified) item.selectedContentColor
        else if (item.contentColor.isSpecified) item.contentColor
        else MaterialTheme.colorScheme.onSurfaceVariant
    DropdownMenuItem(
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropDownCategoryHeader(
    title: String,
    childSelected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val color = if (childSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontWeight = if (childSelected) FontWeight.Bold else FontWeight.SemiBold
    DropdownMenuItem(
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        text = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = fontWeight,
                color = color
            )
        },
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        leadingIcon = {
            Text(
                text = if (expanded) "−" else "+",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = fontWeight,
                color = color
            )
//            Icon(
//                imageVector = if (expanded) ic_remove else ic_add,
//                contentDescription = title,
//                modifier = Modifier.size(18.dp)
//            )
        },
        colors = MenuDefaults.itemColors(
            textColor = color,
            leadingIconColor = color
        )
    )
}