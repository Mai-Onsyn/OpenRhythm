package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.darken
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.gl.*
import mai_onsyn.compose_gl.rememberGLCanvasHandle
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.InstancedManager
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.NoteChunkBuilder
import mai_onsyn.open_rhythm.ui.utility.FrequencyCounter
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

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
//    LaunchedEffect(Unit) {
//        while (true) {
//            withFrameNanos {
//                handle.requestRender()
//            }
//        }
//    }

    val fpsCounter = remember { FrequencyCounter() }

    key(im) { GLCanvas(
        modifier = modifier,
        handle = handle,
        onCanvasCreated = {
            im.initGL()
            glEnable(GL_BLEND)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            glClearColor(0f, 0f, 0f, 0f)
            if (Global.settings.UploadAllVBOs) im.uploadAll(midi.totalTicks.toDouble())
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
        glClear(GL_COLOR_BUFFER_BIT)
        im.screenHeightTick = visibleTickCount
        im.update(currTick)
        im.upload(pxPerTick, windowSize, roundCorners)
        im.draw()

        fpsCounter.tick()

        status.glFPS = fpsCounter.getAverageFrequency()

        status.renderingReadyFor = midi
        onFrameEnd()
    } }
}