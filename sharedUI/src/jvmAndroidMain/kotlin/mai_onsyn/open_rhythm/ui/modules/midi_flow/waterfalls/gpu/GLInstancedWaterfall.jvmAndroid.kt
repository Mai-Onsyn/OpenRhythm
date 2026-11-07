package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import com.materialkolor.ktx.darken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai_onsyn.compose_gl.GLCanvas
import mai_onsyn.compose_gl.config.FramePolicy
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.NoteRenderScope
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.filterWindowNotes
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.InstancedManager
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.NoteChunkBuilder
import mai_onsyn.open_rhythm.ui.utility.isBlackKey
import org.lwjgl.opengl.GL46C.*

//private val im = InstancedManager(
//    1, 0.0, 1,
//    NoteChunkBuilder(1, Midi("", 480, 1), mutableListOf(), mutableListOf(), mutableMapOf(), Size.Zero)
//)

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
//    LaunchedEffect(midi, trackColors) {
//        withContext(Dispatchers.Main) {
//            im.chunkTickSize = midi.ppq
//            im.chunkBuilder = NoteChunkBuilder(
//                midi.ppq, midi, maxNoteDurationList, trackColors, gridPos, windowSize
//            )
//        }
//    }

    val im = remember(midi, trackColors, roundCorners) {
        InstancedManager(midi.ppq, currTick, visibleTickCount,
            NoteChunkBuilder(midi.ppq, midi, maxNoteDurationList, trackColors, gridPos)
        )
    }

    // [0]=min [1]=max [2]=sum [3]=count
    val perfStats = remember { mutableMapOf<String, LongArray>() }

    fun recordPerf(key: String, value: Long) {
        if (value < 1000L) return
        val s = perfStats.getOrPut(key) { longArrayOf(Long.MAX_VALUE, Long.MIN_VALUE, 0L, 0L) }
        if (value < s[0]) s[0] = value
        if (value > s[1]) s[1] = value
        s[2] += value
        s[3]++
    }
    SideEffect {
        val activeNotes = filterWindowNotes(midi, currTick, 0, maxNoteDurationList, trackColors)
        activeNoteOutput.clear()
        for (a in activeNotes) {
            activeNoteOutput[a.note.pitch] = if (isBlackKey(a.note.pitch)) a.color.darken(1.5f) else a.color
        }
    }

    key(im) { GLCanvas(
        modifier = modifier,
        onCanvasCreated = {
            im.initGL()
            glEnable(GL_BLEND)
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
            glClearColor(0f, 0f, 0f, 0f)
        },
        onCanvasDisposed = {
            im.dispose()
        },
        onCanvasResized = {},
        framePolicy = FramePolicy.Continuous(100)
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
    } }

//    Box(modifier) {
//        Text(
//            text = "OpenGL renderer Content",
//            style = MaterialTheme.typography.displayMedium,
//            modifier = Modifier.align(Alignment.Center),
//            maxLines = 10
//        )
//    }
}