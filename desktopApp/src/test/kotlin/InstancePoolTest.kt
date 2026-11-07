import androidx.compose.ui.geometry.Size
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.InstancedManager
import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool.NoteChunkBuilder
import kotlin.test.Test

class InstancePoolTest {
    @Test
    fun testInstancePool() {
        val mng = InstancedManager(
            chunkTickSize = 480,
            currentTick = 0.0,
            screenHeightTick = 1000,
            chunkBuilder = NoteChunkBuilder(
                480,
                Midi("empty", 480, 180000),
                listOf(),
                listOf(),
                mapOf()
            )
        )

        mng.update(0.0)

        mng.update(1000.0)

        mng.update(-2000.0)
    }
}