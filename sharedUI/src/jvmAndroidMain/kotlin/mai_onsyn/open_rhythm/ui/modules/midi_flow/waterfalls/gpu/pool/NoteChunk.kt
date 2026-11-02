package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.DrawableNote

class NoteChunk(
    val startTick: Double,
    val durationTicks: Double,
    val id: Int,
    val entities: List<DrawableNote>
) {
    override fun toString(): String {
        return "Chunk(startTick=$startTick, durationTicks=$durationTicks, id=$id, entitie=${entities.size})"
    }
}