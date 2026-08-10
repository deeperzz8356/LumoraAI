package com.deep.lumoraai.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "History") },
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "history",
                onSelected = { route -> if (route.isNotBlank()) onNavigate(route) else onNext() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            when (uiState) {
                HistoryUiState.Loading -> AppLoadingScreen()
                HistoryUiState.Empty -> AppEmptyScreen(
                    title = "No Creations Yet",
                    body = "Generated images and videos will appear here after you create them.",
                )
                is HistoryUiState.Error -> AppErrorScreen(message = uiState.message)
                is HistoryUiState.Success -> HistoryGrid(items = uiState.items)
            }
        }
    }
}

@Composable
private fun HistoryGrid(items: List<HistoryModel>) {
    val selected = remember { mutableStateOf<HistoryModel?>(null) }
    val viewing = selected.value

    if (viewing != null && !viewing.mediaUrl.isNullOrBlank()) {
        MediaViewerDialog(
            filePath = viewing.mediaUrl.orEmpty(),
            mediaType = viewing.type,
            title = viewing.title.ifBlank { if (viewing.type.equals("VIDEO", true)) "Video" else "Image" },
            onDismiss = { selected.value = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your Creations",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Tap any item to play or share.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items, key = { it.id }) { item ->
                HistoryTile(
                    item = item,
                    onClick = {
                        if (!item.mediaUrl.isNullOrBlank() && File(item.mediaUrl).exists()) {
                            selected.value = item
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryTile(
    item: HistoryModel,
    onClick: () -> Unit,
) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161838))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        val path = item.mediaUrl
        if (!isVideo && !path.isNullOrBlank() && File(path).exists()) {
            AsyncImage(
                model = File(path),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1F3A)),
                contentAlignment = Alignment.Center,
            ) {
                if (isVideo) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color(0xFFCFBDFF),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                .padding(10.dp)
        ) {
            Text(
                text = item.title.ifBlank { if (isVideo) "Video" else "Image" },
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isVideo) "VIDEO" else "IMAGE",
                    color = Color(0xFFCFBDFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "  •  ${item.createdAt}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
