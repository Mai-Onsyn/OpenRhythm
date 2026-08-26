package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mai_onsyn.open_rhythm.ui.modules.ContextDropDownMenuItem
import mai_onsyn.open_rhythm.ui.modules.ContextDropdownMenu
import openrhythm.sharedui.generated.resources.Res

@Composable
fun BoxScope.InstrumentTableItem(
    initial: Int,
    onChanged: (Int) -> Unit,
    isDrum: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    var value by remember { mutableStateOf(if (isDrum) drumKitPrograms.indexOf(initial).let { if (it == -1) 0 else it } else initial) }

    val instItems = remember { mutableListOf<ContextDropDownMenuItem>().apply {
        if (isDrum) {
            for (i in drumKitNames) {
                add(ContextDropDownMenuItem(
                    label = i,
                    selectedContentColor = colorScheme.primary
                ))
            }
        } else {
            var categoryIndex = 0
            for (inst in 0..127) {
                add(ContextDropDownMenuItem(
                    label = instNames[inst],
                    selectedContentColor = colorScheme.primary,
                    category = categories[categoryIndex]
                ))

                if (categoryBreakPoints[categoryIndex] == inst) {
                    categoryIndex++
                }
            }
        }
    } }

    var menuExpanded by remember { mutableStateOf(false) }
    ContextDropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        items = instItems,
        selectedIndex = value,
        alignment = Alignment.Start,
        onSelect = {
            value = it
            onChanged(if (isDrum) drumKitPrograms[value] else value)
        },
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .align(Alignment.CenterStart)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        OutlinedButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(0.25.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDrum) drumKitNames[value] else instNames[value],
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (if (isDrum) drumKitPrograms[value] else value).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

val categoryBreakPoints = arrayOf(7, 15, 23, 31, 39, 47, 55, 63, 71, 79, 87, 95, 103, 111, 119, 127)

val instNames: List<String>
    get() {
        if (_instNames == null) {
            val jsonStr = runBlocking(Dispatchers.IO) {
                Res.readBytes("files/instruments/en_us.json")
            }.decodeToString()
            _instNames = Json.decodeFromString(jsonStr) as List<String>
            if (_instNames!!.size != 128)
                throw IllegalArgumentException("Invalid instrument count in json file: expected 128, actual ${_instNames!!.size}")
        }
        return _instNames!!
    }

val categories: List<String>
    get() {
        if (_categories == null) {
            val jsonStr = runBlocking(Dispatchers.IO) {
                Res.readBytes("files/instruments/categories/en_us.json")
            }.decodeToString()
            _categories = Json.decodeFromString(jsonStr) as List<String>
            if (_categories!!.size != 16)
                throw IllegalArgumentException("Invalid category count in json file: expected 16, actual ${_categories!!.size}")
        }
        return _categories!!
    }

private var _instNames: List<String>? = null
private var _categories: List<String>? = null

//val drumKitNames: Array<String> = Array(9) { n ->
//    when (n shl 3) {
//        0 -> "Standard Kit"; 8 -> "Room Kit"; 16 -> "Power Kit"
//        24 -> "Electronic Kit"; 25 -> "TR-808 Kit"; 32 -> "Jazz Kit"
//        40 -> "Brush Kit"; 48 -> "Orchestra Kit"; 56 -> "Sound Effects Kit"
//        else -> "Custom Kit #$n"
//    }
//}
//val drumKitNames: Map<Int, String> = mapOf(
//    0 to "Standard Kit",
//    8 to "Room Kit",
//    16 to "Power Kit",
//    24 to "Electronic Kit",
//    25 to "TR-808 Kit",
//    32 to "Jazz Kit",
//    40 to "Brush Kit",
//    48 to "Orchestra Kit",
//    56 to "Sound Effects Kit",
//)

private val drumKitPrograms = intArrayOf(0, 8, 16, 24, 25, 32, 40, 48, 56)
private val drumKitNames = arrayOf(
    "Standard Kit", "Room Kit", "Power Kit", "Electronic Kit", "TR-808 Kit",
    "Jazz Kit", "Brush Kit", "Orchestra Kit", "Sound Effects Kit"
)