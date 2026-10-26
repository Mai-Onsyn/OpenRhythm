package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float
) {
    Box(modifier.align(Alignment.Center)) {
        Text(
            text = "OpenGL renderer is NOT supported on ios!",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.Center),
            maxLines = 10
        )
    }
}