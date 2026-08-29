package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.bridge.Global

@Composable
fun BoxScope.BackgroundImage() {
    val bgImageRequest by rememberBGImageRequestState()
    AsyncImage(
        model = bgImageRequest,
        contentDescription = "background image",
        modifier = Modifier
            .alpha(Global.settings.BackgroundImageOpacity)
            .blur(Global.settings.BackgroundImageBlurDp.dp)
            .matchParentSize(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun rememberBGImageRequestState(): State<ImageRequest?> {
    val platformContext = LocalPlatformContext.current
    val bgImageRequest = produceState<ImageRequest?>(null, Global.settings.BackgroundImageDir) {
        val file = PlatformFile(Global.settings.BackgroundImageDir)
        withContext(Dispatchers.IO) {
            value = if (file.exists() && file.isRegularFile()) {
                if (Global.settings.OriginalBackgroundImageSize) ImageRequest.Builder(platformContext)
                    .data(file.readBytes())
                    .size(Size.ORIGINAL)
                    .build()
                else ImageRequest.Builder(platformContext)
                    .data(file.readBytes())
                    .build()
            } else null
        }
    }
    return bgImageRequest
}