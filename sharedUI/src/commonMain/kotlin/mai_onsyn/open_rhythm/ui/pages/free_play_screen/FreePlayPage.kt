package mai_onsyn.open_rhythm.ui.pages.free_play_screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FreePlayPage(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    Text("Free Play Page", style = MaterialTheme.typography.titleLarge)
}