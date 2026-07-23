package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.pages.setting.categories.SettingsContent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsPage(
    useWideLayout: Boolean,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }
//    Text("Settings Page", style = MaterialTheme.typography.titleLarge)
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var selectedIndex by remember { mutableStateOf(0) }
        if (useWideLayout) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(24.dp)
                    .width(200.dp),
            ) {
                Row {
                    IconButton(
                        onClick = onBack,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .size(56.dp, 32.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = ic_arrow_back,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                val choices = remember {
                    SettingsContent.entries.map { it.displayName to it.icon }
                }
                ChoiceColumn(
                    choices = choices,
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it },
                    itemHeight = 56.dp,
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(
                modifier = Modifier.safeDrawingPadding().padding(24.dp)
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
                    setting.content()
                }
            }
        }
    }
}