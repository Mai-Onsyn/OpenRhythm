package mai_onsyn.open_rhythm.bridge

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun GLWaterfall(modifier: Modifier) {
    Box(modifier) {
        Text(
            text = "OpenGL renderer is NOT supported on ios!",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.Center),
            maxLines = 10
        )
    }
}