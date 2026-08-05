package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LabeledSlider(
    value: Int,
    range: IntRange,
    steps: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (Int) -> Unit,
    onSlidStart: (Int) -> Unit,
    onSlidStop: (Int) -> Unit,
) {

}