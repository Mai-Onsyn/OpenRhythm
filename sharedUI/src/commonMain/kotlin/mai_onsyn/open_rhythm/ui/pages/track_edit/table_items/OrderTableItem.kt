package mai_onsyn.open_rhythm.ui.pages.track_edit.table_items

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface

@Composable
fun BoxScope.OrderTableItem(
    index: Int
) {
    OpacitySurface(
        contentPadding = 8.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .align(Alignment.CenterStart)
    ) {
        Text(
            text = (index + 1).toString(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}