package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.ui.modules.ProportionalPane
import mai_onsyn.open_rhythm.ui.modules.ProportionalPaneScope

@Composable
fun VisualKeyboard(
    modifier: Modifier = Modifier,
    eventDispatcher: GlobalKeyEventDispatcher,
    activeKeys: MutableMap<Long, Color>,
    onKeyStateChange: (Long, Boolean) -> Unit,
    selectedKey: Long? = null,
    drawControl: Boolean = true,
    drawNumpad: Boolean = true
) {
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
            FunctionRow(activeKeys, onKeyStateChange, selectedKey)
            NumberRow(activeKeys, onKeyStateChange, selectedKey)
        }
    }
}
@Composable
private fun KeyboardUnitScope.FunctionRow(
    activeKeys: MutableMap<Long, Color>,
    onKeyStateChange: (Long, Boolean) -> Unit,
    selectedKey: Long? = null
) {
    KeyButton(
        Modifier.keyUnit(0f, 0f, 1f, 1f),
        "ESC",
        selectedKey == Key.Escape.keyCode,
        activeKeys[Key.Escape.keyCode],
        { onKeyStateChange(Key.Escape.keyCode, activeKeys.containsKey(Key.Escape.keyCode)) }
    )
    var offset = 2f
    for (i in 1..12) {
        val code = Key.F1.keyCode + i
        KeyButton(
            Modifier.keyUnit(offset, 0f, 1f, 1f),
            "F${i}",
            selectedKey == code,
            activeKeys[code],
            { onKeyStateChange(code, activeKeys.containsKey(code)) }
        )
        offset += if (i % 4 == 0) 1.5f else 1f
    }
}

@Composable
private fun KeyboardUnitScope.NumberRow(
    activeKeys: MutableMap<Long, Color>,
    onKeyStateChange: (Long, Boolean) -> Unit,
    selectedKey: Long? = null
) {
    KeyButton(
        Modifier.keyUnit(0f, 1.25f, 1f, 1f),
        "/",
        selectedKey == Key.Grave.keyCode,
        activeKeys[Key.Grave.keyCode],
        { onKeyStateChange(Key.Grave.keyCode, activeKeys.containsKey(Key.Grave.keyCode)) },
        "?"
    )
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
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    var size by remember { mutableStateOf(DpSize.Zero) }
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.extraSmall,
        shadowElevation = 2.dp,
        border = BorderStroke(0.8.dp, activeColor ?: MaterialTheme.colorScheme.surfaceVariant)
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
                fontSize = with(density) { (size.height * 0.4f).toSp() },
                color = contentColorFor(color),
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
                    fontSize = with(density) { (size.height * 0.4f).toSp() },
                    color = contentColorFor(color),
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(-0.2f * size.width, -0.2f * size.height)
                )
            }
        }
//        ProportionalPane(Modifier.fillMaxSize().onSizeChanged { height = with(density) { (it.height * 0.4f).toDp() } }) {
//            Text(
//                text = firstText,
//                fontSize = with(density) { height.toSp() },
//                color = contentColorFor(color),
//                maxLines = 1,
//                overflow = TextOverflow.Visible,
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .then(
//                        if (secondText == null) Modifier.layoutRatio(0.2f, 0.2f, 0.6f, 0.6f)
//                        else Modifier.layoutRatio(0.35f, 0.35f, 0.6f, 0.6f)
//                    ).background(Color.Red)
//            )
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .then(
//                        if (secondText == null) Modifier.layoutRatio(0.2f, 0.2f, 0.6f, 0.6f)
//                        else Modifier.layoutRatio(0.35f, 0.35f, 0.6f, 0.6f)
//                    )
//            ) {
//                Text(
//                    text = firstText,
//                    fontSize = with(density) { width.toSp() },
//                    style = TextStyle(
//                        color = contentColorFor(color),
//                    ),
//                    maxLines = 1,
//                    overflow = TextOverflow.Visible
//                )
//            }
//            if (secondText != null) {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .layoutRatio(0f, 0f, 0.6f, 0.6f)
//                ) {
//                    Text(
//                        text = secondText,
//                        fontSize = with(density) { width.toSp() },
//                        style = TextStyle(
//                            color = contentColorFor(MaterialTheme.colorScheme.surfaceVariant),
//                        ),
//                        maxLines = 1,
//                        overflow = TextOverflow.Visible
//                    )
//                }
//            }
//        }
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