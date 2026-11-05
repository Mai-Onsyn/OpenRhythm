package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import co.touchlab.kermit.Logger
import com.materialkolor.ktx.darken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.scope.GLRenderScope
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.InstancedManager
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.NoteChunkBuilder
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import org.lwjgl.opengl.GL46C.*

private val im = InstancedManager(
    1, 0.0, 1,
    NoteChunkBuilder(1, Midi("", 480, 1), mutableListOf(), mutableListOf(), mutableMapOf(), Size.Zero)
)

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    midi: Midi,
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
//    TestRectsDraw()
    LaunchedEffect(midi, trackColors) {
        withContext(Dispatchers.Main) {
            im.chunkTickSize = midi.ppq * 4
            im.chunkBuilder = NoteChunkBuilder(
                midi.ppq * 4, midi, maxNoteDurationList, trackColors, gridPos, windowSize
            )
        }
    }

    GLCanvas(
        onDraw = { scope ->
            val frameStart = Time.nanos
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            im.screenHeightTick = visibleTickCount
            im.update(currTick)
            im.upload(pxPerTick, roundCorners)
            im.draw()
            val activeNotes = filterWindowNotes(midi, currTick, 0, maxNoteDurationList, trackColors)
            activeNoteOutput.clear()
            for (a in activeNotes) {
                activeNoteOutput[a.note.pitch] = if (isBlackKey(a.note.pitch)) a.color.darken(1.5f) else a.color
            }
            val frameEnd = Time.nanos
//            Logger.v { "Rendering cost ${frameEnd - frameStart}ns" }
        },
        modifier = modifier,
        onSurfaceCreated = {
            im.initGL()
            glEnable(GL_BLEND)
            glEnable(GL_DEPTH_TEST)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            glClearColor(0f, 0f, 0f, 0f)
        },
        onSurfaceDestroyed = {
//            im.dispose()
        },
        onSurfaceResized = { scope, size ->
            im.chunkBuilder.windowSizePx = windowSize
        },
        framePolicy = FramePolicy.Continuous(100)
    )

//    Box(modifier) {
//        Text(
//            text = "OpenGL renderer Content",
//            style = MaterialTheme.typography.displayMedium,
//            modifier = Modifier.align(Alignment.Center),
//            maxLines = 10
//        )
//    }
}