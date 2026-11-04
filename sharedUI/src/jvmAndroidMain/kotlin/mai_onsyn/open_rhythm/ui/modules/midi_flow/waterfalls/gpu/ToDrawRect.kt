package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu

data class ToDrawRect(
    val fracX: Float,
    val fracW: Float,
    val tickInChunk: Float,
    val durationTick: Float,
    val color: Int,
    val depth: Int
)