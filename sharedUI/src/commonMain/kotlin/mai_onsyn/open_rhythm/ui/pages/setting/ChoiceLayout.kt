package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ChoiceColumn(
    choices: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
) {
    val colorScheme = MaterialTheme.colorScheme

    val selectedOffsetDp by animateDpAsState(
        targetValue = (itemHeight + 4.dp) * selectedIndex,
        animationSpec = tween(easing = LinearOutSlowInEasing)
    )
    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .verticalScroll(scrollState)
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        color = colorScheme.primaryContainer,
                        topLeft = Offset(0f, selectedOffsetDp.toPx()),
                        size = size.copy(height = itemHeight.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
    ) {
        choices.forEachIndexed { index, (name, icon) ->
            val selectedContentColor by animateColorAsState(
                targetValue = if (selectedIndex == index) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                animationSpec = tween(easing = LinearOutSlowInEasing)
            )
            Surface(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
                    .height(itemHeight)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent,
                contentColor = selectedContentColor
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ChoiceRow(
    choices: List<Pair<String?, ImageVector?>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 40.dp,
    contentPadding: Dp = 16.dp,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val selectedOffsetDp by animateDpAsState(
        targetValue = (itemWidth + 4.dp) * selectedIndex,
        animationSpec = tween(easing = LinearOutSlowInEasing)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        color = selectedColor,
                        topLeft = Offset(selectedOffsetDp.toPx(), 0f),
                        size = size.copy(width = itemWidth.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
    ) {
        choices.forEachIndexed { index, (name, icon) ->
            val selectedContentColor by animateColorAsState(
                targetValue = if (selectedIndex == index) selectedContentColor else contentColor,
                animationSpec = tween(easing = LinearOutSlowInEasing)
            )
            Surface(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
                    .width(itemWidth)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent,
                contentColor = selectedContentColor
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(contentPadding)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = name,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}