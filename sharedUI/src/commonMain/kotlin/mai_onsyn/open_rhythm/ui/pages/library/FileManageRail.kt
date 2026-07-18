package mai_onsyn.open_rhythm.ui.pages.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.ui.utility.UiState

@Composable
fun FileManageRail(
    modifier: Modifier = Modifier,
    path: String,
    onFileCountAvailable: (Int) -> Unit
) {
    val uiState by produceState<UiState<List<UIMidiData>>>(
        initialValue = UiState.Loading,
        key1 = path
    ) {
        value = try {
            val result = getFilesInFolder(path)
            onFileCountAvailable(result.size)
            UiState.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            UiState.Error(e.message ?: "加载失败")
        }
    }

    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = modifier) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier) {
                Text(
                    text = "Failed to load midi from $path because ${(uiState as UiState.Error).message}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is UiState.Success -> {
            val midiFiles = (uiState as UiState.Success<List<UIMidiData>>).data
            Box(modifier = modifier) {
                Text(
                    text = "${midiFiles.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}