package com.deep.lumoraai.feature.templates.components

import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.feature.templates.model.TemplateListItem
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import java.util.Locale

private val CardColor = Color(0xFF0E172A)
private val CardStroke = Color(0xFF1B2A44)
private val Muted = Color(0xFF9BA6BA)
private val Lime = Color(0xFFD6FF2F)

@Composable
fun FeatureCard(
    item: TemplateListItem,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    val isVideo = remember(item.assetFileName) {
        isVideoFile(item.assetFileName)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardColor,
        border = BorderStroke(1.dp, CardStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            if (isVideo) {
                AutoPlayVideo(
                    fileName = item.assetFileName
                )
            } else {
                AutoSizeImage(
                    fileName = item.assetFileName,
                    contentDescription = item.title
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = item.subtitle,
                        color = Muted,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.055f)
                        )
                        .clickable {
                            onCopy()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy prompt",
                        tint = Lime.copy(alpha = 0.95f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoSizeImage(
    fileName: String,
    contentDescription: String
) {
    val context = LocalContext.current

    var aspectRatio by remember {
        mutableFloatStateOf(1f)
    }

    val imageRequest = remember(fileName) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/templates/$fileName")
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatioSafe(aspectRatio),
        onState = { state ->
            if (state is AsyncImagePainter.State.Success) {
                val drawable = state.result.drawable

                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight

                if (width > 0 && height > 0) {
                    aspectRatio =
                        width.toFloat() / height.toFloat()
                }
            }
        }
    )
}

@Composable
private fun AutoPlayVideo(
    fileName: String
) {
    val context = LocalContext.current

    var aspectRatio by remember {
        mutableFloatStateOf(16f / 9f)
    }

    val mediaPlayer = remember {
        MediaPlayer()
    }

    val surfaceView = remember {
        SurfaceView(context)
    }

    AndroidView(
        factory = {

            surfaceView.apply {

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                holder.addCallback(
                    object : SurfaceHolder.Callback {

                        override fun surfaceCreated(
                            holder: SurfaceHolder
                        ) {
                            try {

                                val afd =
                                    context.assets.openFd(
                                        "templates/$fileName"
                                    )

                                mediaPlayer.reset()

                                mediaPlayer.setDataSource(
                                    afd.fileDescriptor,
                                    afd.startOffset,
                                    afd.length
                                )

                                afd.close()

                                mediaPlayer.setDisplay(holder)

                                mediaPlayer.isLooping = true

                                mediaPlayer.setVolume(
                                    0f,
                                    0f
                                )

                                mediaPlayer.setOnPreparedListener { player ->

                                    val width =
                                        player.videoWidth

                                    val height =
                                        player.videoHeight

                                    if (
                                        width > 0 &&
                                        height > 0
                                    ) {
                                        aspectRatio =
                                            width.toFloat() /
                                                height.toFloat()
                                    }

                                    player.start()
                                }

                                mediaPlayer.prepareAsync()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                        }

                        override fun surfaceDestroyed(
                            holder: SurfaceHolder
                        ) {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                            }
                        }
                    }
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatioSafe(aspectRatio)
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
    )

    DisposableEffect(fileName) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            } catch (_: Exception) {
            }

            mediaPlayer.reset()
        }
    }
}

private fun Modifier.aspectRatioSafe(
    ratio: Float
): Modifier {

    val safeRatio =
        if (ratio.isFinite() && ratio > 0f) {
            ratio
        } else {
            1f
        }

    return this.then(
        Modifier.aspectRatio(safeRatio)
    )
}

private fun isVideoFile(
    fileName: String
): Boolean {

    return when (
        fileName
            .substringAfterLast('.', "")
            .lowercase(Locale.US)
    ) {

        "mp4",
        "webm",
        "mkv",
        "3gp",
        "avi",
        "mov" -> true

        else -> false
    }
}