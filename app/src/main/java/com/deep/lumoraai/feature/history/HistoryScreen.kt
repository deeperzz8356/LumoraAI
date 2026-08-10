package com.deep.lumoraai.feature.history

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.data.model.HistoryModel
import java.io.File

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<HistoryModel?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "History") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "history",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        when (uiState) {
            HistoryUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            HistoryUiState.Empty -> EmptyState(
                title = "History",
                message = "No saved creations yet. Generate an image or video to see it here.",
                modifier = Modifier.padding(padding)
            )
            is HistoryUiState.Error -> ErrorState(
                title = "History",
                message = uiState.message,
                modifier = Modifier.padding(padding)
            )
            is HistoryUiState.Success -> HistoryContent(
                items = uiState.items,
                onItemClick = { selectedItem = it },
                modifier = Modifier.padding(padding)
            )
        }
    }

    selectedItem?.let { item ->
        val path = item.mediaUrl
        if (!path.isNullOrBlank()) {
            MediaViewerDialog(
                filePath = path,
                mediaType = item.type,
                mimeType = if (item.type.equals("VIDEO", ignoreCase = true)) "video/mp4" else "image/png",
                title = item.title,
                onDismiss = { selectedItem = null },
            )
        } else {
            selectedItem = null
        }
    }
}

@Composable
private fun HistoryContent(
    items: List<HistoryModel>,
    onItemClick: (HistoryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Your Creations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Saved on this device. Tap to play or share.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp
        )
        val rows = items.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    HistoryMediaCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HistoryMediaCard(
    item: HistoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val path = item.mediaUrl
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161838))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!path.isNullOrBlank() && File(path).exists() && !isVideo) {
                AsyncImage(
                    model = File(path),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (isVideo) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Text("No media", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${item.type} • ${item.createdAt}",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
