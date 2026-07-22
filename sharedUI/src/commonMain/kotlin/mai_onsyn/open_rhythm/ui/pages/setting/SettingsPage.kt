package mai_onsyn.open_rhythm.ui.pages.setting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_settings
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
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