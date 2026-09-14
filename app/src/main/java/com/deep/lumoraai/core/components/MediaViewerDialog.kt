package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val configuration = LocalConfiguration.current
    val file = remember(filePath) { File(filePath) }
    val exists = file.exists()
    val viewerHeight = (configuration.screenHeightDp * 0.68f).dp.coerceIn(320.dp, 620.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                        .height(viewerHeight)
                        .background(Color.Black)
                ) {
                    if (!exists) {
                        Text(
                            text = stringResource(com.deep.lumoraai.R.string.ui_saved_media_file_is_missing),
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (mediaType.equals("VIDEO", ignoreCase = true)) {
                        ZoomableVideoPlayer(filePath = filePath)
                    } else {
                        ZoomableImageViewer(filePath = filePath)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (exists) {
                        IconButton(
                            onClick = {
                                MediaShareUtils.shareMedia(context, filePath, mimeType)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(com.deep.lumoraai.R.string.ui_share),
                                tint = Color(0xFFCFBDFF)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(42.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_close),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocalVideoPlayer(
    filePath: String,
    modifier: Modifier = Modifier,
) {
    ZoomableVideoPlayer(
        filePath = filePath,
        modifier = modifier,
        showControls = false,
        enableGestureDetection = false,
    )
}
