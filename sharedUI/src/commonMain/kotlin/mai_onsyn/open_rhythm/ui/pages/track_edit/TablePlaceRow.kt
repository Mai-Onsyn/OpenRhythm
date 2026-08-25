package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TablePlaceRow(
    modifier: Modifier = Modifier,
    header: @Composable BoxScope.() -> Unit,
    inst: @Composable BoxScope.() -> Unit,
    preview: @Composable BoxScope.() -> Unit,
    color: @Composable BoxScope.() -> Unit,
    volume: @Composable BoxScope.() -> Unit,
    enable: @Composable BoxScope.() -> Unit,
    play: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints {
        val showPreview = maxWidth > 1000.dp
        val lrAvailable = if (showPreview) 800.dp else maxWidth
        val lWeight = lrAvailable * 0.35f / maxWidth
        val mWeight = (maxWidth - lrAvailable) / maxWidth
        val rWeight = lrAvailable * 0.65f / maxWidth
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(lWeight)
            ) {
                Box(Modifier.width(56.dp).fillMaxHeight()) { header() }
                Box(Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp)) { inst() }
            }
            if (showPreview) {
                Box(Modifier.weight(mWeight).fillMaxHeight()) { preview() }
            }
            Row(
                modifier = Modifier.weight(rWeight),
            ) {
                Box(Modifier.width(120.dp).fillMaxHeight()) { color() }
                Box(Modifier.weight(1f).fillMaxHeight().padding(end = 16.dp)) { volume() }
                Box(Modifier.width(56.dp).fillMaxHeight()) { enable() }
                Box(Modifier.width(56.dp).fillMaxHeight()) { play() }
            }
        }
    }
}