package com.deep.lumoraai.core.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.deep.lumoraai.core.utils.MediaShareUtils
import java.io.File
import androidx.compose.ui.res.stringResource

@Composable
fun MediaViewerDialog(
    filePath: String,
    mediaType: String,
    mimeType: String = if (mediaType.equals("VIDEO", ignoreCase = true)) "video/mp4" else "image/png",
    title: String = if (mediaType.equals("VIDEO", ignoreCase = true)) "Video Ready" else "Image Ready",
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }
    val exists = file.exists()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131524))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(Color.Black)
                ) {
                    if (!exists) {
                        Text(
                            text = "Saved media file is missing.",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (mediaType.equals("VIDEO", ignoreCase = true)) {
                        LocalVideoPlayer(filePath = filePath)
                    } else {
                        AsyncImage(
                            model = file,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (exists) {
                        TextButton(
                            onClick = {
                                MediaShareUtils.shareMedia(context, filePath, mimeType)
                            }
                        ) {
                            Text(stringResource(com.deep.lumoraai.R.string.ui_share), color = Color(0xFFCFBDFF))
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(com.deep.lumoraai.R.string.ui_close), color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LocalVideoPlayer(filePath: String) {
    val context = LocalContext.current
    val exoPlayer = remember(filePath) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(File(filePath))))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(filePath) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
