package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher

private typealias URect = Rect

@Composable
fun VisualKeyboard2(
    modifier: Modifier = Modifier,
    eventDispatcher: GlobalKeyEventDispatcher,
    activeKeys: MutableMap<Long, Color>,
    onSelectChanged: (Long) -> Unit,
    selectedKey: Long? = null,
    drawControl: Boolean = true,
    drawNumpad: Boolean = true
) {
    DisposableEffect(Unit) {
        val handler: suspend (KeyEvent) -> Boolean = { event ->
            if (event.type == KeyEventType.KeyDown) {
                activeKeys[event.key.keyCode] = Color.Red
            } else {
                activeKeys.remove(event.key.keyCode)
            }
            false
        }
        eventDispatcher.registerHandler(handler)
        onDispose {
            eventDispatcher.unregisterHandler(handler)
        }
    }
    val uw =
        if (drawControl && drawNumpad) 22.5f
        else if (drawControl && !drawNumpad) 18.25f
        else if (!drawControl && drawNumpad) 19.25f
        else 15f
    val uh = 6.25f

    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        var mouseUPos by remember { mutableStateOf(Offset.Zero) }
        val mouseClickEvents = remember { mutableStateListOf<Offset>() }
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .aspectRatio(uw / uh)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            mouseUPos = event.changes[0].position / size.height.toFloat() * 6.25f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        mouseClickEvents.add(it / size.height.toFloat() * 6.25f)
                    }
                }
        ) {
            var offset = 0f     // 0 = main; 15.25 = ctrl; 18.5 = numpad
            val keySequence = sequence {
                yieldAll(mainArea)
                if (drawControl) {
                    offset = 15.25f
                    yieldAll(controlArea)
                }
                if (drawNumpad) {
                    offset = if (drawControl) 18.5f else 15.25f
                    yieldAll(numpadArea)
                }
            }
            for (key in keySequence) {
                val region = Rect(Offset(key.offsetX + offset, key.offsetY), Size(key.width, key.height)).scale(0.96f)
                if (mouseClickEvents.isNotEmpty() && mouseClickEvents.first() in region) {
                    mouseClickEvents.removeAt(0)
                    onSelectChanged(key.code)
                }
                drawKeyButton(
                    region = region,
                    firstText = key.firstName,
                    secondText = key.lastName,
                    activeBorderColor = activeKeys[key.code],
                    color =
                        if (selectedKey == key.code) colorScheme.primary
                        else if (mouseUPos in region) colorScheme.surfaceContainerHighest
                        else colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}

private fun DrawScope.drawKeyButton(
    region: URect,
    firstText: String,
    secondText: String? = null,
    activeBorderColor: Color? = null,
    color: Color
) {
    val unitMultiplier = size.height * 0.16f    // *0.16 = /6.25
    val conerValue = unitMultiplier * 0.04f
    if (activeBorderColor != null) {
        drawRoundRect(
            color = activeBorderColor,
            topLeft = region.topLeft * unitMultiplier,
            size = region.size * unitMultiplier,
            cornerRadius = CornerRadius(conerValue, conerValue)
        )
        val scaled = region.scale(0.96f)
        val newCornerValue = conerValue * 0.96f
        drawRoundRect(
            color = color,
            topLeft = scaled.topLeft * unitMultiplier,
            size = scaled.size * unitMultiplier,
            cornerRadius = CornerRadius(newCornerValue, newCornerValue)
        )
    } else {
        drawRoundRect(
            color = color,
            topLeft = region.topLeft * unitMultiplier,
            size = region.size * unitMultiplier,
            cornerRadius = CornerRadius(conerValue, conerValue)
        )
    }
}

private fun Rect.scale(scale: Float): Rect {
    val center = this.center
    var newWidth: Float
    var newHeight: Float
    if (width > height) {
        newHeight = height * scale
        newWidth = width - height + newHeight
    } else {
        newWidth = width * scale
        newHeight = height - width + newWidth
    }
    val newLeft = center.x - newWidth / 2f
    val newTop = center.y - newHeight / 2f
    return Rect(left = newLeft, top = newTop, right = newLeft + newWidth, bottom = newTop + newHeight)
}