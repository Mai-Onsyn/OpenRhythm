package mai_onsyn.open_rhythm.ui.utility

fun localeOrder(locale: String): Int = when (locale) {
    "en" -> 0
    "zh" -> 1
    else -> -1
}

fun orderLocale(order: Int): String = when (order) {
    0 -> "en"
    1 -> "zh"
    else -> "en"
}