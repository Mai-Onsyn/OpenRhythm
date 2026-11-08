package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import androidx.compose.ui.geometry.Size
import mai_onsyn.compose_gl.gl.*

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
        pxPerTickLoc = glGetUniformLocation(program, "pxPerTick")
        windowSizeLoc = glGetUniformLocation(program, "windowSize")
        currChunkTickLoc = glGetUniformLocation(program, "currChunkTick")
        chunkTickSizeLoc = glGetUniformLocation(program, "chunkTickSize")
        roundedCornersLoc = glGetUniformLocation(program, "roundedCorners")
    }

    fun update(
        pxPerTick: Float,
        windowSize: Size,
        chunkTickSize: Float,
        roundedCorners: Float,
    ) {
        glUniform1f(pxPerTickLoc, pxPerTick)
        glUniform2f(windowSizeLoc, windowSize.width, windowSize.height)
        glUniform1f(roundedCornersLoc, roundedCorners)
        glUniform1f(chunkTickSizeLoc, chunkTickSize)
    }

    fun updateInChunk(chunkTick: Float) {
        glUniform1f(currChunkTickLoc, chunkTick)
    }
}