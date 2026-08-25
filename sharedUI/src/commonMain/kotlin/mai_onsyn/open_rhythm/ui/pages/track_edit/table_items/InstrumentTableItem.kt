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
    onChanged: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var value by remember { mutableStateOf(initial) }

    val instItems = remember { mutableListOf<ContextDropDownMenuItem>().apply {
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
            onChanged(value)
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
                    text = instNames[value],
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value.toString(),
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