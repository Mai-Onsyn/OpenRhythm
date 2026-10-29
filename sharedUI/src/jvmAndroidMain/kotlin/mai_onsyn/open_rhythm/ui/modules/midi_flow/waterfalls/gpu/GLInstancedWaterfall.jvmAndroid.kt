package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope

var showGLCanvas by mutableStateOf(true)

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float
) {
//    if (showGLCanvas) GLCanvas(
//        onDraw = {
//            glEnable(GL_BLEND)
//            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
//
//            glClearColor(0.1f, 0f, 0f, 0.1f)
//            glClear(GL_COLOR_BUFFER_BIT)
//        }
//    )
    TestRectsDraw()

    Box(modifier.align(Alignment.Center)) {
        Text(
            text = "OpenGL renderer Content",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.Center),
            maxLines = 10
        )
    }
}
