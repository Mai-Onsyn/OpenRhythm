package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

import mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.DrawableNote

class NoteChunk(
    val startTick: Double,
    val durationTicks: Double,
    val chunk: Int,
    val entities: List<DrawableNote>,
    var offset: Double
)