package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
    enable: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints {
        val showPreview = maxWidth > 800.dp
        val lrAvailable = if (showPreview) 500.dp else maxWidth
        val lWeight = lrAvailable * 0.4f / maxWidth
        val mWeight = (maxWidth - lrAvailable) / maxWidth
        val rWeight = lrAvailable * 0.6f / maxWidth
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(lWeight),
            ) {
                Box(Modifier.width(56.dp)) { header() }
                Box(Modifier.weight(1f)) { inst() }
            }
            if (showPreview) {
                Box(Modifier.weight(mWeight)) { preview() }
            }
            Row(
                modifier = Modifier.weight(rWeight),
            ) {
                Box(Modifier.weight(6f)) { color() }
                Box(Modifier.weight(10f)) { volume() }
                Box(Modifier.weight(4f)) { enable() }
            }
        }
    }
}