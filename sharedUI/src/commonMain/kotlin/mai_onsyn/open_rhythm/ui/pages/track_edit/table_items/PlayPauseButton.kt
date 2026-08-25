package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.modules.MorphingPlayPauseButton
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface

@Composable
fun BoxScope.PlayPauseButton(
    isPlaying: Boolean,
    onChanged: (Boolean) -> Unit
) {
    OpacitySurface(
        contentPadding = 0.dp,
        modifier = Modifier.align(Alignment.Center).pointerHoverIcon(PointerIcon.Hand)
    ) {
        IconButton(
            modifier = Modifier.size(40.dp),
            onClick = {
                onChanged(!isPlaying)
            }
        ) {
            MorphingPlayPauseButton(
                modifier = Modifier.size(24.dp),
                isPlaying = isPlaying,
                fill = MaterialTheme.colorScheme.primary
            )
        }
    }
}