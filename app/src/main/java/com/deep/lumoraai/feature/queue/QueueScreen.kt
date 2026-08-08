package com.deep.lumoraai.feature.queue

import com.deep.lumoraai.core.utils.GeneratedImage
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppCard
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.ui.theme.tokens.Spacing

@Composable
fun QueueScreen(
    uiState: QueueUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
            QueueContent(uiState = uiState)
        }
    }
}

@Composable
private fun QueueContent(uiState: QueueUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.containerMargin, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        QueueTopBar()
        Column {
            Text("Active Jobs", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("Real-time status of your creative generations.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        when (uiState) {
            is QueueUiState.Loading -> AppLoadingScreen(modifier = Modifier.height(200.dp))
            is QueueUiState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    uiState.items.forEach { job -> JobCardItem(job = job) }
                }
            }
            is QueueUiState.Error -> AppErrorScreen(message = uiState.message, modifier = Modifier.height(200.dp))
            is QueueUiState.Empty -> AppEmptyScreen(title = "No Jobs", body = "No active or queued jobs.", modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
private fun QueueTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(Spacing.md))
            Text("Lumora AI", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(Spacing.lg))
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            )
        }
    }
}

@Composable
private fun JobCardItem(job: ActiveJobInfo) {
    val showImageDialog = remember { mutableStateOf(false) }

    if (showImageDialog.value && !job.imageUrl.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { showImageDialog.value = false },
            confirmButton = {
                Button(onClick = { showImageDialog.value = false }) {
                    Text("Close")
                }
            },
            title = { Text("Generated Image", style = MaterialTheme.typography.titleLarge) },
            text = {
                GeneratedImage(
                    imagePayload = job.imageUrl.orEmpty(),
                    contentDescription = "Full Generated Image",
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (job.isCompleted && !job.imageUrl.isNullOrEmpty()) {
                showImageDialog.value = true
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (!job.imageUrl.isNullOrEmpty()) {
                    GeneratedImage(
                        imagePayload = job.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = job.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
                JobDetails(job = job)
            }
            JobRightControl(progressPercent = job.progressPercent, isCompleted = job.isCompleted, onCancel = {})
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(job.badgeText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "•  ${job.statusText}",
                style = MaterialTheme.typography.labelSmall,
                color = if (job.isCompleted) Color(0xFFADF021) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(job.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(job.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraLarge)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onPrimary,
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
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
                if (progressPercent != null) {
                    Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onCancel)
            )
        }
    }
}