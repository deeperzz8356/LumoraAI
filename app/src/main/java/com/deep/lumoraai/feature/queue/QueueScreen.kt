package com.deep.lumoraai.feature.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppButton
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.core.components.VideoFirstFrameThumbnail
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.utils.GeneratedImage
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.ui.theme.tokens.Spacing
import java.io.File
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

private val QueueBackground = Color(0xFF081020)
private val QueueCard = Color(0xFF10192D)
private val QueueStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun QueueScreen(
    uiState: QueueUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = QueueBackground,
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "queue",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(QueueBackground)
                .padding(padding)
        ) {
            QueueContent(uiState = uiState, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun QueueContent(uiState: QueueUiState, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        QueueTopBar(onNavigate = onNavigate)
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = QueueCard,
            border = BorderStroke(1.dp, QueueStroke.copy(alpha = 0.64f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Lime.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Lime, modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Active Jobs",
                        color = Color.White,
                        fontSize = 20.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Real-time status of your creative generations.",
                        color = Muted,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (uiState) {
                is QueueUiState.Loading -> AppLoadingScreen(modifier = Modifier.height(200.dp))
                is QueueUiState.Success -> {
                    uiState.items.forEachIndexed { index, job ->
                        AnimatedJobCard(job = job, index = index)
                    }
                }
                is QueueUiState.Error -> AppErrorScreen(message = uiState.message, modifier = Modifier.height(200.dp))
                is QueueUiState.Empty -> QueueEmptyState(onNavigate = onNavigate)
            }
        }
    }
}

@Composable
private fun QueueTopBar(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_queue), color = Color.White, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Row(
                modifier = Modifier
                    .background(Purple.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(Lime, CircleShape))
                Text(stringResource(com.deep.lumoraai.R.string.ui_live), color = Purple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onNavigate(Screen.History.route) }) {
                Icon(Icons.Default.List, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_history), tint = Cyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(com.deep.lumoraai.R.string.ui_history), color = Cyan, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QueueEmptyState(onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = CardShape,
        color = QueueCard,
        border = BorderStroke(1.dp, QueueStroke.copy(alpha = 0.64f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Lime.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Lime, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(stringResource(com.deep.lumoraai.R.string.ui_all_clear), color = Color.White, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No jobs in the pipeline. Start generating to see your work here.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            AppButton(
                text = "Start Creating",
                onClick = { onNavigate(Screen.CreateHub.route) },
                modifier = Modifier.border(1.dp, Purple.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
            )
        }
    }
}

@Composable
private fun AnimatedJobCard(job: ActiveJobInfo, index: Int) {
    val visible = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible.value = true
    }
    
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { it / 2 })
    ) {
        JobCardItem(job = job)
    }
}

@Composable
private fun JobCardItem(job: ActiveJobInfo) {
    val showMediaDialog = remember { mutableStateOf(false) }
    val isVideo = job.mediaType.equals("VIDEO", ignoreCase = true)
    val mediaPath = job.localMediaPath ?: job.videoUrl ?: job.imageUrl
    val canOpenMedia = job.isCompleted && !mediaPath.isNullOrBlank() && File(mediaPath).exists()

    if (showMediaDialog.value && canOpenMedia && mediaPath != null) {
        MediaViewerDialog(
            filePath = mediaPath,
            mediaType = if (isVideo) "VIDEO" else "IMAGE",
            title = if (isVideo) "Video Ready" else "Image Ready",
            onDismiss = { showMediaDialog.value = false },
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = QueueCard),
        border = BorderStroke(1.dp, QueueStroke.copy(alpha = 0.64f)),
        onClick = {
            if (canOpenMedia) {
                showMediaDialog.value = true
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                JobThumbnail(job = job, isVideo = isVideo)
                JobDetails(job = job, modifier = Modifier.weight(1f))
            }
            JobRightControl(progressPercent = job.progressPercent, isCompleted = job.isCompleted, onCancel = {})
        }
    }
}

@Composable
private fun JobThumbnail(job: ActiveJobInfo, isVideo: Boolean) {
    val thumbPath = when {
        isVideo && !job.localMediaPath.isNullOrBlank() -> job.localMediaPath
        !isVideo && !job.localMediaPath.isNullOrBlank() -> job.localMediaPath
        !isVideo && !job.imageUrl.isNullOrBlank() -> job.imageUrl
        else -> null
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo && !thumbPath.isNullOrBlank()) {
            VideoFirstFrameThumbnail(
                filePath = thumbPath,
                contentDescription = null,
                fallbackImageRes = job.imageRes,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!thumbPath.isNullOrBlank()) {
            GeneratedImage(
                imagePayload = thumbPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = job.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Purple gradient overlay for incomplete jobs
        if (!job.isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Purple.copy(alpha = 0.42f))))
            )
        }
        
        if (isVideo && job.isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_play_video),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun JobDetails(job: ActiveJobInfo, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Purple.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(job.badgeText, style = MaterialTheme.typography.labelSmall, color = Purple, fontWeight = FontWeight.Bold)
            }
            Text(
                text = job.statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (job.isCompleted) Lime else Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(job.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(job.subtitle, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun JobRightControl(
    progressPercent: Float?,
    isCompleted: Boolean,
    onCancel: () -> Unit
) {
    if (isCompleted) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Lime, CircleShape)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(com.deep.lumoraai.R.string.ui_download),
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progressPercent ?: 0.25f,
                    color = Lime,
                    trackColor = QueueStroke,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
                if (progressPercent != null) {
                    Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(com.deep.lumoraai.R.string.ui_cancel),
                tint = Muted,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onCancel)
            )
        }
    }
}
