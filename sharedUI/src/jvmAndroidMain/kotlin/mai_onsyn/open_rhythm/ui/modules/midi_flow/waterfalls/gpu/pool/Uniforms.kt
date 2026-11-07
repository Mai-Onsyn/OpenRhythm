package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.geometry.Size
import org.lwjgl.opengl.GL46C

class Uniforms {
    var pxPerTickLoc = 0
        private set

    var windowSizeLoc = 0
        private set

    var currChunkTickLoc = 0
        private set

    var chunkTickSizeLoc = 0
        private set

    var roundedCornersLoc = 0
        private set

    fun initialize(program: Int) {
        pxPerTickLoc = GL46C.glGetUniformLocation(program, "pxPerTick")
        windowSizeLoc = GL46C.glGetUniformLocation(program, "windowSize")
        currChunkTickLoc = GL46C.glGetUniformLocation(program, "currChunkTick")
        chunkTickSizeLoc = GL46C.glGetUniformLocation(program, "chunkTickSize")
        roundedCornersLoc = GL46C.glGetUniformLocation(program, "roundedCorners")
    }

    fun update(
        pxPerTick: Float,
        windowSize: Size,
        chunkTickSize: Float,
        roundedCorners: Float,
    ) {
        GL46C.glUniform1f(pxPerTickLoc, pxPerTick)
        GL46C.glUniform2f(windowSizeLoc, windowSize.width, windowSize.height)
        GL46C.glUniform1f(roundedCornersLoc, roundedCorners)
        GL46C.glUniform1f(chunkTickSizeLoc, chunkTickSize)
    }

    fun updateInChunk(chunkTick: Float) {
        GL46C.glUniform1f(currChunkTickLoc, chunkTick)
    }
}