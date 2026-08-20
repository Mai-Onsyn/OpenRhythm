package mai_onsyn.open_rhythm.ui.modules.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogPopup(
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    shape: Shape = MaterialTheme.shapes.extraLarge,
    tonalElevation: Dp = 6.dp,
    shadowElevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
//    val popupPositionProvider = remember { GlobalPopupPositionProvider() }

    val showProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    if (showProgress == 0f) return

    RootPopup(
//        popupPositionProvider = popupPositionProvider,
//        properties = PopupProperties(
//            focusable = true,
//            dismissOnBackPress = false,
//            dismissOnClickOutside = false,
//            usePlatformDefaultWidth = false,
//            clippingEnabled = false
//        )
    ) {
        val focusRequester = remember { FocusRequester() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .focusable()
                .focusRequester(focusRequester)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f * showProgress))
                .safeDrawingPadding()
                .pointerInput(Unit) {
                    detectTapGestures { onDismissRequest() }
                }
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                        onDismissRequest()
                        true
                    }
                    else false
                }
        ) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            BackHandler { onDismissRequest() }
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = showProgress
                        val scale = showProgress * 0.15f + 0.85f
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {}
                    }
            ) {
                Surface(
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = tonalElevation,
                    shadowElevation = shadowElevation
                ) {
                    Box(content = content)
                }
            }
        }
    }
}

class GlobalPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}