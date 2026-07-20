package mai_onsyn.open_rhythm.ui.pages.play_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.modules.FlatSlider

@Composable
fun StatusBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isPlaying: Boolean,
    onToggledPlay: (Boolean) -> Unit,
    onHide: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box() {
            FlatSlider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(16.dp)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                progress = progress,
                onChanged = onProgressChange,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}