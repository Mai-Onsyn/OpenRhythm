package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun str(
    resource: StringResource,
    vararg formatArgs: Any
): String = stringResource(resource, *formatArgs)

@Composable
fun str(
    resource: StringResource
): String = stringResource(resource)

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