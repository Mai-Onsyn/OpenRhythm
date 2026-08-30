package mai_onsyn.open_rhythm.ui.theme

import androidx.compose.ui.graphics.Color

object TrackColorDefaults {
    fun colors(): List<Color> {
        if (_default == null) {
            _default = Array(16) { i ->
                Color.hsv((350f + i * 137.5f) % 360f, 0.3f + i / 4 * 0.1f, 1f)
            }.toList()
        }
        return _default!!
    }

    private var _default: List<Color>? = null
}