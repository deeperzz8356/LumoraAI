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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.AppButton
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.utils.GeneratedImage
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.ui.theme.tokens.Spacing
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun QueueScreen(
    uiState: QueueUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF000000), // Pure Black background
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
            .padding(horizontal = Spacing.containerMargin, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        QueueTopBar(onNavigate = onNavigate)
        
        Column {
            Text(
                text = "Active Jobs", 
                style = MaterialTheme.typography.headlineLarge, 
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(Color(0xFF39FF14), RoundedCornerShape(1.5.dp)) // Neon Green accent
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "Real-time status of your creative generations.", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color(0xFF888888)
            )
        }
        
        when (uiState) {
            is QueueUiState.Loading -> AppLoadingScreen(modifier = Modifier.height(200.dp))
            is QueueUiState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    uiState.items.forEachIndexed { index, job -> 
                        AnimatedJobCard(job = job, index = index) 
                    }
                }
            }
            is QueueUiState.Error -> AppErrorScreen(message = uiState.message, modifier = Modifier.height(200.dp))
            is QueueUiState.Empty -> QueueEmptyState(onNavigate = onNavigate)
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
            Text("Lumora AI", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(Spacing.sm))
            // Live badge
            Row(
                modifier = Modifier
                    .background(Color(0xFF2D1B69), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(Color(0xFF39FF14), CircleShape))
                Text("LIVE", color = Color(0xFFA78BFA), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onNavigate(Screen.History.route) }) {
                Icon(Icons.Default.List, contentDescription = "History", tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("History", color = Color(0xFFA78BFA), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun QueueEmptyState(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF7E50EF).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width / 1.5f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF39FF14),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = "All Clear", 
            style = MaterialTheme.typography.headlineMedium, 
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "No jobs in the pipeline. Start generating to see your work here.", 
            style = MaterialTheme.typography.bodyMedium, 
            color = Color(0xFF888888),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        AppButton(
            text = "Start Creating",
            onClick = { onNavigate(Screen.CreateHub.route) },
            modifier = Modifier.border(1.dp, Color(0xFFA78BFA), RoundedCornerShape(24.dp))
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)), // Near black
        border = BorderStroke(1.dp, Color(0xFF1A1A1A)), // Subtle border
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
                JobDetails(job = job)
            }
            JobRightControl(progressPercent = job.progressPercent, isCompleted = job.isCompleted, onCancel = {})
        }
    }
}

@Composable
private fun JobThumbnail(job: ActiveJobInfo, isVideo: Boolean) {
    val thumbPath = when {
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
        if (!thumbPath.isNullOrBlank()) {
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
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF7E50EF).copy(alpha = 0.4f))))
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
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun JobDetails(job: ActiveJobInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF2D1B69), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(job.badgeText, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold)
            }
            Text(
                text = "•  ${job.statusText}",
                style = MaterialTheme.typography.labelSmall,
                color = if (job.isCompleted) Color(0xFF39FF14) else Color(0xFF888888)
            )
        }
        Text(job.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1)
        Text(job.subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
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
                .background(Color(0xFF39FF14), CircleShape) // Neon green download
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Download",
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
                    color = Color(0xFF39FF14), // Neon green
                    trackColor = Color(0xFF1A1A1A), // Dimmed track
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
                if (progressPercent != null) {
                    Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = Color(0xFF888888),
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onCancel)
            )
        }
    }
}