package mai_onsyn.open_rhythm.ui.pages.track_edit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_music_note
import mai_onsyn.open_rhythm.ui.icons.ic_refresh
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface
import mai_onsyn.open_rhythm.ui.modules.dialog.ConfirmDialog
import mai_onsyn.open_rhythm.ui.utility.str
import openrhythm.sharedui.generated.resources.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackEditPage(
    midi: Midi?,
    midiPath: String,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    if (midi == null) Box(Modifier.fillMaxSize()) {
        Text(
            text = str(Res.string.track_loadingMidi),
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
        var refreshVersion by remember { mutableStateOf(0) }
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
                    text = str(Res.string.track_pageTitle),
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

                var showRefreshConfirmDialog by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        showRefreshConfirmDialog = true
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
                            text = str(Res.string.track_reset),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                ConfirmDialog(
                    visible = showRefreshConfirmDialog,
                    onDismissRequest = { showRefreshConfirmDialog = false },
                    onConfirm = {
                        showRefreshConfirmDialog = false
                        Global.settings.midiFileSettings.remove(midiPath)
                        refreshVersion++
                    },
                    title = str(Res.string.track_resetTitle),
                    message = str(Res.string.track_resetMessage),
                    isDangerous = true
                )
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        key(refreshVersion) {
            TrackTable(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                midiPath = midiPath,
                midi = midi
            )
        }
    }
}