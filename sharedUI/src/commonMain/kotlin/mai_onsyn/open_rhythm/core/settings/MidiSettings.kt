package mai_onsyn.open_rhythm.core.settings

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.bridge.Global

@Serializable
data class MidiTrackSettings(
    var inst: Int? = null,
    @Serializable(with = ColorSerializer::class)
    var color: Color? = null,
    var volume: Int? = null,
    var visible: Boolean? = null,
    var audible: Boolean? = null
) {
    val isValid: Boolean
        get() = inst != null || color != null || volume != null || visible != null || audible != null
}

@Serializable
data class MidiFileSettings(
    val trackSettings: MutableMap<Int, MidiTrackSettings> = mutableMapOf(),
    var lastUpdated: Long = 0
) {
    val isValid: Boolean
        get() = trackSettings.isNotEmpty()
}

fun MidiFileSettings.take(track: Int): MidiFileSettings? {
    trackSettings[track]?.let {
        return MidiFileSettings(mutableMapOf(0 to it))
    }
    return null
}

fun addSettings(
    key: String,
    trackIndex: Int,
    inst: Int? = null,
    color: Color? = null,
    volume: Int? = null,
    visible: Boolean? = null,
    audible: Boolean? = null
) {
    val fs = Global.settings.midiFileSettings.getOrElse(key) { MidiFileSettings() }
    if (!fs.trackSettings.containsKey(trackIndex)) fs.trackSettings[trackIndex] = MidiTrackSettings(inst, color, volume, visible, audible)

    inst?.let { fs.trackSettings[trackIndex]!!.inst = it }
    color?.let { fs.trackSettings[trackIndex]!!.color = it }
    volume?.let { fs.trackSettings[trackIndex]!!.volume = it }
    visible?.let { fs.trackSettings[trackIndex]!!.visible = it }
    audible?.let { fs.trackSettings[trackIndex]!!.audible = it }
    Global.settings.midiFileSettings[key] = fs.copy()
}

fun removeSettings(
    key: String,
    trackIndex: Int,
    inst: Boolean = false,
    color: Boolean = false,
    volume: Boolean = false,
    visible: Boolean = false,
    audible: Boolean = false
) {
    val fs = Global.settings.midiFileSettings[key] ?: return
    val ts = fs.trackSettings[trackIndex] ?: return

    if (inst) ts.inst = null
    if (color) ts.color = null
    if (volume) ts.volume = null
    if (visible) ts.visible = null
    if (audible) ts.audible = null

    if (!ts.isValid) {
        fs.trackSettings.remove(trackIndex)
    }
    if (!fs.isValid) {
        Global.settings.midiFileSettings.remove(key)
        return
    }
    Global.settings.midiFileSettings[key] = fs.copy()
}