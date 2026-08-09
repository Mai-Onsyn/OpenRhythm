package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.parseMidi
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.pages.play_screen.PlayPage
import mai_onsyn.open_rhythm.ui.pages.setting.categories.SettingsContent
import openrhythm.sharedui.generated.resources.Res
import org.jetbrains.compose.resources.InternalResourceApi

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsPage(
    useWideLayout: Boolean,
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var selectedIndex by remember { mutableStateOf(if (useWideLayout) 1 else 0) }
        if (useWideLayout) {
            WideNavigation(onBack, selectedIndex) { selectedIndex = it + 1 }
        }
        BackHandler {
            if (useWideLayout) onBack()
            else {
                if (selectedIndex != 0) selectedIndex = 0
                else onBack()
            }
        }
        LaunchedEffect(useWideLayout) {
            if (useWideLayout && selectedIndex == 0) selectedIndex = 1
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            BoxWithConstraints(
                modifier = Modifier.safeDrawingPadding().padding(if (selectedIndex == 0) 0.dp else 24.dp)
            ) {
                val limitWidth = SettingsContent.entries[selectedIndex] == SettingsContent.WATERFALL ||
                        SettingsContent.entries[selectedIndex] == SettingsContent.KEYBOARD
                val showPreview = limitWidth && maxWidth > 960.dp
                val contentWidth = if (limitWidth) animateDpAsState(if (showPreview) 480.dp else 800.dp) else null
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = SettingsContent.entries[selectedIndex],
                        transitionSpec = {
                            val direction = when {
                                targetState.id > initialState.id -> 1
                                targetState.id < initialState.id -> -1
                                else -> 0
                            }

                            val slideOffset = { height: Int -> (height * 0.25f).toInt() }

                            val enter = slideInVertically(
                                initialOffsetY = { if (direction > 0) -slideOffset(it) else slideOffset(it) },
                                animationSpec = spring(stiffness = 800f, dampingRatio = 0.8f)
                            ) + fadeIn(animationSpec = tween(300))

                            val exit = slideOutVertically(
                                targetOffsetY = { if (direction > 0) slideOffset(it) else -slideOffset(it) },
                                animationSpec = spring(stiffness = 800f, dampingRatio = 0.8f)
                            ) + fadeOut(animationSpec = tween(200))

                            ContentTransform(
                                targetContentEnter = enter,
                                initialContentExit = exit,
                                sizeTransform = SizeTransform(clip = false)
                            )
                        }
                    ) { setting ->
                        if (setting.id != 0) {
                            if (limitWidth) {
                                Box(Modifier.widthIn(400.dp, contentWidth!!.value)) {
                                    setting.content()
                                }
                            } else setting.content()
                        } else {
                            NarrowNavigation(onBack) { selectedIndex = it }
                        }
                    }

                    AnimatedVisibility(
                        visible = showPreview,
                        enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn() + scaleIn(initialScale = 0.3f),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut() + scaleOut(targetScale = 0.3f)
                    ) {
                        PreviewRail(
                            Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WideNavigation(onBack: () -> Unit, selectedIndex: Int, onSelected: (index: Int) -> Unit) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(24.dp)
            .width(200.dp),
    ) {
        NavigationHeader(onBack)
        Spacer(Modifier.height(24.dp))

        val choices = remember {
            SettingsContent.entries.takeLast(SettingsContent.entries.size - 1).map { it.displayName to it.icon }
        }
        ChoiceColumn(
            choices = choices,
            selectedIndex = selectedIndex - 1,
            onSelect = onSelected,
            itemHeight = 56.dp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NarrowNavigation(onBack: () -> Unit, onSelected: (index: Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.height(24.dp))
        NavigationHeader(onBack)
        Spacer(Modifier.height(24.dp))

        SettingsContent.entries.forEachIndexed { index, settingsContent ->
            if (index == 0) return@forEachIndexed

            Surface(
                onClick = { onSelected(index) },
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = settingsContent.icon,
                        contentDescription = settingsContent.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = settingsContent.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(
            onClick = onBack,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .size(56.dp, 32.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = ic_arrow_back,
                contentDescription = "Back",
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@OptIn(InternalResourceApi::class)
@Composable
fun PreviewRail(modifier: Modifier) {
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