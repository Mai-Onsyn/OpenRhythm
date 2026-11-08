package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.darken
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.rememberGLCanvasHandle
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.InstancedManager
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.NoteChunkBuilder
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import org.lwjgl.opengl.GL46C.*

@Composable
actual fun NoteRenderScope.OpenGLWaterfall(
    modifier: Modifier,
    midi: Midi,
    trackColors: List<Color>,
    activeNoteOutput: MutableMap<Int, Color>,
    roundCorners: Float,
    onFrameEnd: NoteRenderScope.() -> Unit
) {
    val im = remember(midi, trackColors, roundCorners) {
        InstancedManager(midi.ppq, currTick, visibleTickCount,
            NoteChunkBuilder(midi.ppq, midi, maxNoteDurationList, trackColors, gridPos)
        )
    }

    SideEffect {
        val activeNotes = filterWindowNotes(midi, currTick, 0, maxNoteDurationList, trackColors)
        activeNoteOutput.clear()
        for (a in activeNotes) {
            activeNoteOutput[a.note.pitch] = if (isBlackKey(a.note.pitch)) a.color.darken(1.5f) else a.color
        }
        status.activeNoteCount = activeNotes.size
    }

    val handle = rememberGLCanvasHandle()
    LaunchedEffect(currTick) {
        handle.requestRender()
    }

    key(im) { GLCanvas(
        modifier = modifier,
        handle = handle,
        onCanvasCreated = {
            im.initGL()
            glEnable(GL_BLEND)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            glClearColor(0f, 0f, 0f, 0f)
            im.uploadAll(midi.totalTicks.toDouble())
        },
        onCanvasDisposed = {
            im.dispose()
        },
        onCanvasResized = {},
        fallbackContent = {
            Box(
                modifier = Modifier.matchParentSize()
            ) {
                Text("OpenGL rendering is NOT supported on your device!")
            }
        },
        config = GLConfig(msaa = 4)
    ) {
        val frameStart = Time.nanos
        glClear(GL_COLOR_BUFFER_BIT)
        im.screenHeightTick = visibleTickCount
        val update = Time.nanos
        im.update(currTick)
        val upload = Time.nanos
        im.upload(pxPerTick, windowSize, roundCorners)
        val draw = Time.nanos
        im.draw()

        val frameEnd = Time.nanos

//            if (frameEnd - frameStart < 1000) return@GLCanvas
//            recordPerf("update", upload - update)
//            recordPerf("upload", draw - upload)
//            recordPerf("draw",   frameEnd - draw)
//            Logger.v {
//                perfStats.entries.joinToString("; ") { (k, s) ->
//                    "$k[min=${s[0]} max=${s[1]} avg=${s[2] / s[3]}]"
//                }
//            }

        status.renderingReadyFor = midi
        onFrameEnd()
    } }
}