package com.deep.lumoraai.core.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                    if (exists) {
                        IconButton(
                            onClick = { MediaShareUtils.shareMedia(context, filePath, mimeType) },
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
