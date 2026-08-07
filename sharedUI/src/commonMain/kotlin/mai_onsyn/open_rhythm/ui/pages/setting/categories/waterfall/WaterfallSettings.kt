package mai_onsyn.open_rhythm.ui.pages.setting.categories.waterfall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.parseMidi
import mai_onsyn.open_rhythm.ui.pages.play_screen.PlayPage
import openrhythm.sharedui.generated.resources.Res
import org.jetbrains.compose.resources.InternalResourceApi

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
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        WaterfallBackground()
        NoteAppearance()
        KeyboardAppearance()
    }
}

@OptIn(InternalResourceApi::class)
@Composable
private fun PreviewRail(modifier: Modifier) {
    Surface(
        modifier = modifier.innerShadow(),
    ) {
        val midi by produceState<Midi?>(null) {
            value = parseMidi("故郷の星が映る海", Res.readBytes("files/The sea reflecting my hometown star.mid").toList())
        }
        PlayPage(midi, {}, false)

        DisposableEffect(Unit) {
            onDispose {
                Singleton.player.stop()
                Singleton.player.seek((midi?.startTick ?: 0).toLong())
            }
        }
    }
}

fun Modifier.innerShadow(
    shape: Shape = RectangleShape,
    color: Color = Color.Black.copy(alpha = 0.25f),
    blur: Dp = 8.dp
): Modifier = this.drawWithContent {
    drawContent()

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