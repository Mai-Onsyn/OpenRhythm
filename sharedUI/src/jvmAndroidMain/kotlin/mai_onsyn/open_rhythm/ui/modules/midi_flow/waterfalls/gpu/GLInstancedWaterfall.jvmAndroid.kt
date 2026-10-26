package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.silenium.compose.gl.canvas.GLCanvas
import dev.silenium.compose.gl.canvas.GLCanvasState
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import org.lwjgl.opengl.GL30

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float
) {
    GLCanvas(
        modifier = modifier.matchParentSize(),
        onDispose = {},
        onResize = {a, b ->
            println("${a ?: "null"}, $b")
        }
    ) {
        GL30.glEnable(GL30.GL_BLEND)
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)

        GL30.glClearColor(1f, 1f, 0f, 0.1f)
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT)
    }


    Box(modifier.align(Alignment.Center)) {
        Text(
            text = "OpenGL renderer Content copied from ios",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.Center),
            maxLines = 10
        )
    }
}