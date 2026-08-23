package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.icons.ic_refresh
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackEditPage(
    midi: Midi?,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    if (midi == null) Box(Modifier.fillMaxSize()) {
        Text(
            text = "Loading MIDI...",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    } else Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        BoxWithConstraints {
            val showNameOnTitle = maxWidth > 520.dp
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .size(48.dp, 32.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = ic_arrow_back,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Tracks",
                    style = MaterialTheme.typography.headlineMedium
                )

                if (showNameOnTitle) {
                    Spacer(Modifier.width(24.dp))
                    OpacitySurface(
                        contentPadding = 8.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = ic_music_note,
                            contentDescription = midi.name,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = midi.name,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else Spacer(Modifier.weight(1f))
                Button(
                    onClick = {

                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = ic_refresh,
                            contentDescription = "reset",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        TrackTable(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            midi = midi
        )
    }
}