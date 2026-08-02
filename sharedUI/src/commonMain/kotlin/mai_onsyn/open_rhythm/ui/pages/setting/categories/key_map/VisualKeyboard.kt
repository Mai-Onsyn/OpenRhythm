package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.times
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.ui.modules.ProportionalPane
import mai_onsyn.open_rhythm.ui.modules.ProportionalPaneScope

@Composable
fun VisualKeyboard(
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
//            Logger.d { "KeyName ${event.key}, code ${event.key.keyCode}, dot is ${Key.NumPadDot.keyCode}" }

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

    Surface(
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        KeyboardLayout(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .aspectRatio(uw / uh),
            widthUnitCount = uw,
            heightUnitCount = uh
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
                KeyButton(
                    Modifier.keyUnit(key.offsetX + offset, key.offsetY, key.width, key.height),
                    key.firstName,
                    selectedKey == key.code,
                    activeKeys[key.code],
                    { onSelectChanged(key.code) },
                    key.lastName
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    firstText: String,
    selected: Boolean,
    activeColor: Color? = null,
    onClick: () -> Unit,
    secondText: String? = null,
) {
    val density = LocalDensity.current
    val color by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )
    val variantColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    )
    var size by remember { mutableStateOf(DpSize.Zero) }
    val sizeFactor = min(size.width, size.height)
    Surface(
        onClick = onClick,
        modifier = modifier.padding(sizeFactor * 0.04f),
        color = color,
        shape = MaterialTheme.shapes.extraSmall,
        shadowElevation = 2.dp,
        border = BorderStroke(sizeFactor * if (activeColor == null) 0.02f else 0.03f, activeColor ?: MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    size = with(density) {
                        DpSize(it.width.toDp(), it.height.toDp())
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstText,
                fontSize = with(density) { (sizeFactor * 0.3f).toSp() },
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .then(
                        if (secondText == null) Modifier
                        else Modifier.offset(0.2f * size.width, 0.2f * size.height)
                    )
            )
            secondText?.let {
                Text(
                    text = secondText,
                    fontSize = with(density) { (sizeFactor * 0.2f).toSp() },
                    color = variantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(-0.2f * size.width, -0.2f * size.height)
                )
            }
        }
    }
}

class KeyboardUnitScope(val uw: Float, val uh: Float): ProportionalPaneScope() {
    fun Modifier.keyUnit(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): Modifier {
        return this.layoutRatio(
            x / uw,
            y / uh,
            width / uw,
            height / uh
        )
    }
}

@Composable
fun KeyboardLayout(
    modifier: Modifier = Modifier,
    widthUnitCount: Float = 22.5f,
    heightUnitCount: Float = 6.25f,
    content: @Composable KeyboardUnitScope.() -> Unit
) {
    ProportionalPane(
        modifier = modifier
    ) {
        KeyboardUnitScope(widthUnitCount, heightUnitCount).content()
    }
}