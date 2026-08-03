package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.pages.play_screen.PlayPage

@Composable
fun WaterfallSettings() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val showPreview = maxWidth > 920.dp

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = if (showPreview) Arrangement.Start else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsRail(
                modifier = Modifier
                    .widthIn(400.dp, if (showPreview) 480.dp else 600.dp)
            )

            if (showPreview) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row {
                        Spacer(Modifier.width(16.dp))
                        PreviewRail(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Blue)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRail(modifier: Modifier) {
    Column(modifier) {
        WaterfallBackground()
    }
}

@Composable
private fun PreviewRail(modifier: Modifier) {
    Surface(
        modifier = modifier.innerShadow(),
    ) {
        PlayPage(null, {}, false)
    }
}

fun Modifier.innerShadow(
    shape: Shape = RectangleShape,
    color: Color = Color.Black.copy(alpha = 0.25f),
    blur: Dp = 8.dp
): Modifier = this.drawWithContent {
    // 1. 先正常绘制组件原本的内容（背景、文字等）
    drawContent()

    // 2. 限制绘制区域，保证内阴影不会溢出 Shape 圆角外部
    clip(shape)
    val blurPx = blur.toPx()

    // 顶部内阴影
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = 0f,
            endY = blurPx
        )
    )

    // 底部
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = size.height,
            endY = size.height - blurPx / 2
        )
    )

    // 左侧内阴影
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(color, Color.Transparent),
            startX = 0f,
            endX = blurPx
        )
    )

    // right
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(color, Color.Transparent),
            startX = size.width,
            endX = size.width - blurPx / 2
        )
    )
}