package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun KeyMappingSettings() {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "Key Mappings Settings",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight(900),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}