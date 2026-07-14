package mai_onsyn.open_rhythm.ui.utility

private val W_pref = arrayOf(1, 1, 2, 2, 3, 4, 4, 5, 5, 6, 6, 7)
private val B_pref = arrayOf(false, true, false, true, false, false, true, false, true, false, true, false)
private val B_offset = arrayOf(0f, -1/6f, 1f, 1/6f, 0f, 0f, -1/4f, 0f, 0f, 0f, 1/4f, 0f)

fun countWhiteKeys(min: Int, max: Int): Int {
    val hi = max / 12 * 7 + W_pref[max % 12]
    val lo = min / 12 * 7 + W_pref[min % 12]
    return hi - lo + 1
}

fun isBlackKey(pitch: Int): Boolean = B_pref[pitch % 12]

fun blackKeyOffset(pitch: Int): Float = B_offset[pitch % 12]