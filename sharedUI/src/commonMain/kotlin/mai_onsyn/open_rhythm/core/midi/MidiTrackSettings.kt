package mai_onsyn.open_rhythm.core.midi

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.core.settings.ColorSerializer

@Serializable
data class MidiTrackSettings(
    var inst: Int? = null,
    @Serializable(with = ColorSerializer::class)
    var color: Color? = null,
    var volume: Float? = null,
    var visible: Boolean? = null,
    var audible: Boolean? = null
)