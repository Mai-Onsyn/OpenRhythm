package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mai_onsyn.open_rhythm.core.midi.Midi

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackEditPage(
    midi: Midi?,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    Box(Modifier.fillMaxSize()) {
        Text(
            text = midi?.name ?: "Unknown midi",
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}