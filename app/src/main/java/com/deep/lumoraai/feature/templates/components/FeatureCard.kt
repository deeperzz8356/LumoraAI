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
import androidx.compose.runtime.remember
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
import coil.request.ImageRequest
import java.util.Locale

// Shared design-system palette (matches History and the rest of the app).
private val CardColor = Color(0xFF0E172A)
private val CardStroke = Color(0xFF1B2A44)
private val Muted = Color(0xFF94A0B8)
private val Lime = Color(0xFFD6FF2F)

// Every template card uses the same media aspect ratio so all cards render at a
// uniform height regardless of the intrinsic size of the image/video. 4:3 works
// well for the mixed landscape/portrait template art without harsh cropping.
private const val CARD_MEDIA_ASPECT_RATIO = 4f / 3f

// Fixed height for the title/subtitle strip so the text area is identical on
// every card even when a subtitle is one line vs two.
private val CardInfoHeight = 56.dp

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
                    .height(CardInfoHeight)
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
                        maxLines = 1,
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

    val imageRequest = remember(fileName) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/templates/$fileName")
            .crossfade(true)
            .build()
    }

    // Fixed aspect ratio + crop keeps every card the same height.
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatioSafe(CARD_MEDIA_ASPECT_RATIO)
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
    )
}

@Composable
private fun AutoPlayVideo(
    fileName: String
) {
    val context = LocalContext.current

    // Key the player and surface to the file. Without this key the same
    // MediaPlayer/SurfaceView instance was reused when switching tabs (e.g.
    // Video -> Promo Video), so the surface callback fired against a stale
    // player and the new clip never rendered until the card was fully recreated
    // (the "visit Logo then come back" workaround). Re-keying rebuilds them
    // cleanly for each file.
    val mediaPlayer = remember(fileName) { MediaPlayer() }
    val surfaceView = remember(fileName) { SurfaceView(context) }

    AndroidView(
        // Keyed factory: recreated when fileName changes.
        factory = {
            surfaceView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                fun startPlayback(holder: SurfaceHolder) {
                    try {
                        val afd = context.assets.openFd("templates/$fileName")
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(
                            afd.fileDescriptor,
                            afd.startOffset,
                            afd.length
                        )
                        afd.close()
                        mediaPlayer.setDisplay(holder)
                        mediaPlayer.isLooping = true
                        mediaPlayer.setVolume(0f, 0f)
                        mediaPlayer.setOnPreparedListener { player -> player.start() }
                        mediaPlayer.prepareAsync()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            startPlayback(holder)
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            try {
                                if (mediaPlayer.isPlaying) mediaPlayer.pause()
                            } catch (_: Exception) {
                            }
                        }
                    }
                )

                // If the surface already exists (view reused before the callback
                // fires), start immediately so playback never gets stuck waiting
                // for a surfaceCreated that won't come again.
                if (holder.surface?.isValid == true) {
                    startPlayback(holder)
                }
            }
        },
        // Fixed aspect ratio so all cards are the same height as the images.
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatioSafe(CARD_MEDIA_ASPECT_RATIO)
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
            mediaPlayer.release()
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