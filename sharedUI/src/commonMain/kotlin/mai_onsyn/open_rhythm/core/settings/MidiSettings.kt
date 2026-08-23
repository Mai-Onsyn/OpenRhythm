package mai_onsyn.open_rhythm.core.settings

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class MidiTrackSettings(
    var inst: Int? = null,
    @Serializable(with = ColorSerializer::class)
    var color: Color? = null,
    var volume: Float? = null,
    var visible: Boolean? = null,
    var audible: Boolean? = null
)

@Serializable
data class MidiFileSettings(
    val trackSettings: MutableMap<Int, MidiTrackSettings> = mutableMapOf(),
    var lastUpdated: Long = 0
)