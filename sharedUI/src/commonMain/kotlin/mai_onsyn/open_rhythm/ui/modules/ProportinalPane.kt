package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density

/**
 * 保存比例配置的数据类
 * @param xRatio 子组件起点 X 轴占父容器宽度的比例 (0.0 ~ 1.0)
 * @param yRatio 子组件起点 Y 轴占父容器高度的比例 (0.0 ~ 1.0)
 * @param widthRatio 子组件宽度占父容器宽度的比例 (0.0 ~ 1.0)
 * @param heightRatio 子组件高度占父容器高度的比例 (0.0 ~ 1.0)
 */
@Immutable
data class ProportionalData(
    val xRatio: Float = 0f,
    val yRatio: Float = 0f,
    val widthRatio: Float = 1f,
    val heightRatio: Float = 1f
)

// 用于向自定义 Layout 传递布局数据的 Modifier
private class ProportionalDataModifier(
    val data: ProportionalData
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return data
    }
}

// 作用域，限制 Modifier 仅可在 ProportionalPane 内部使用
open class ProportionalPaneScope {
    fun Modifier.layoutRatio(
        xRatio: Float = 0f,
        yRatio: Float = 0f,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f
    ): Modifier {
        return this.then(
            ProportionalDataModifier(
                ProportionalData(
                    xRatio = xRatio.coerceIn(0f, 1f),
                    yRatio = yRatio.coerceIn(0f, 1f),
                    widthRatio = widthRatio.coerceIn(0f, 1f),
                    heightRatio = heightRatio.coerceIn(0f, 1f)
                )
            )
        )
    }
}

/**
 * 比例布局组件 (ProportionalPane)
 */
@Composable
fun ProportionalPane(
    modifier: Modifier = Modifier,
    content: @Composable ProportionalPaneScope.() -> Unit
) {
    val scope = ProportionalPaneScope()

    Layout(
        content = { scope.content() },
        modifier = modifier
    ) { measurables, constraints ->
        // 获取父容器的最大可用像素宽高
        val parentWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
        val parentHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else 0

        // 测量所有子组件并计算相对坐标
        val placeables = measurables.map { measurable ->
            val data = (measurable.parentData as? ProportionalData) ?: ProportionalData()

            // 根据比例计算实际像素尺寸
            val childWidth = (parentWidth * data.widthRatio).toInt().coerceAtLeast(0)
            val childHeight = (parentHeight * data.heightRatio).toInt().coerceAtLeast(0)

            // 强制子组件使用计算出的确切尺寸进行测量
            val placeable = measurable.measure(
                Constraints.fixed(childWidth, childHeight)
            )

            // 根据比例计算实际偏移像素
            val x = (parentWidth * data.xRatio).toInt()
            val y = (parentHeight * data.yRatio).toInt()

            Triple(placeable, x, y)
        }

        // 确定父组件大小并放置组件
        layout(parentWidth, parentHeight) {
            placeables.forEach { (placeable, x, y) ->
                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}