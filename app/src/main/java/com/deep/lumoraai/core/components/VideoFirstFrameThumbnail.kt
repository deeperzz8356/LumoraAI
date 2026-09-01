package com.deep.lumoraai.core.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun VideoFirstFrameThumbnail(
    filePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackImageRes: Int? = null,
) {
    val frame by produceState<Bitmap?>(initialValue = null, key1 = filePath) {
        value = withContext(Dispatchers.IO) { extractFirstVideoFrame(filePath) }
    }

    when {
        frame != null -> Image(
            bitmap = frame!!.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        fallbackImageRes != null -> Image(
            painter = painterResource(id = fallbackImageRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        else -> Box(modifier = modifier.background(Color.Black))
    }
}

private fun extractFirstVideoFrame(filePath: String): Bitmap? {
    if (filePath.isBlank() || !File(filePath).exists()) return null

    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(filePath)
        retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (_: Exception) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}
