package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.ui.input.key.Key
import kotlinx.serialization.Serializable

private val defaultKeys = arrayOf(Key.A, Key.W, Key.S, Key.E, Key.D, Key.F, Key.T, Key.G, Key.Y, Key.H, Key.U, Key.J, Key.K, Key.O, Key.L, Key.P, Key.Semicolon, Key.Apostrophe)

@Serializable
data class KeyMidiMapping(
    val keyCode: Long,
    val pitch: Int
) {
    companion object {
        fun default() = mutableListOf<KeyMidiMapping>().apply {
            for ((i, element) in defaultKeys.withIndex()) {
                add(KeyMidiMapping(element.keyCode, i + 60))
            }
        }
    }
}

fun List<KeyMidiMapping>.toMappingMap(): Map<Long, Int> = mutableMapOf<Long, Int>().apply {
    this@toMappingMap.forEach { this[it.keyCode] = it.pitch }
}