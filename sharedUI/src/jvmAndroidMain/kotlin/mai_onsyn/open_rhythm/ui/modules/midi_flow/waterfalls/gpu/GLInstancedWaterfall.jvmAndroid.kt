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
import dev.silenium.compose.gl.canvas.rememberGLCanvasState
import dev.silenium.libs.gl.fbo.FBO
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import org.lwjgl.opengl.GL30

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float
) {
    val state = rememberGLCanvasState()
    GLCanvas(
        state = state,
        modifier = modifier.matchParentSize(),
        onDispose = {},
        onResize = { old, new ->
            println("Resized to $new")
        }
    ) {
        GL30.glEnable(GL30.GL_BLEND)
        GL30.glBlendFunc(GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA)

        // 预乘 clear 颜色：rgb * a。这里 (1,1,0) @ 0.1 => (0.1, 0.1, 0), 0.1
        val clearAlpha = 0.1f
        GL30.glClearColor(1f * clearAlpha, 1f * clearAlpha, 0f * clearAlpha, clearAlpha)
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