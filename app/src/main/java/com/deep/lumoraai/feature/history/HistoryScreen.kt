package com.deep.lumoraai.feature.history

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.LumoraTopBar
import com.deep.lumoraai.core.components.VideoFirstFrameThumbnail
import com.deep.lumoraai.core.components.ZoomableImageViewer
import com.deep.lumoraai.core.components.ZoomableVideoPlayer
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import java.io.File

private val HistoryBackground = Color(0xFF081020)
private val HistoryPanel = Color(0xFF0E172A)
private val HistoryStroke = Color(0xFF1B2A44)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF94A0B8)
private val FilterIdle = Color(0xFF111A2D)

private enum class HistoryFilter(val label: String) {
    All("ALL"),
    Images("IMAGE"),
    Videos("VIDEO"),
    Enhancer("ENHANCER"),
    Compress("COMPRESS"),
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<HistoryModel?>(null) }
    var selectedItemsList by remember { mutableStateOf<List<HistoryModel>>(emptyList()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HistoryBackground,
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
                .background(HistoryBackground)
                .padding(padding)
        ) {
            val viewing = selectedItem
            val credits = (uiState as? HistoryUiState.Success)?.credits ?: 0
            if (viewing != null) {
                HistoryMediaViewer(
                    item = viewing,
                    itemsList = selectedItemsList,
                    credits = credits,
                    onBack = { selectedItem = null },
                    onNavigate = onNavigate,
                    onItemChanged = { selectedItem = it }
                )
            } else {
                when (uiState) {
                    HistoryUiState.Loading -> AppLoadingScreen()
                    is HistoryUiState.Empty -> HistoryEmpty(credits = uiState.credits, onNavigate = onNavigate)
                    is HistoryUiState.Error -> AppErrorScreen(message = uiState.message)
                    is HistoryUiState.Success -> HistoryGallery(
                        items = uiState.items,
                        credits = uiState.credits,
                        onNavigate = onNavigate,
                        onSelected = { item, list ->
                            selectedItem = item
                            selectedItemsList = list
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryGallery(
    items: List<HistoryModel>,
    credits: Int,
    onNavigate: (String) -> Unit,
    onSelected: (HistoryModel, List<HistoryModel>) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    val filteredItems = remember(items, selectedFilter) {
        items.filter { item ->
            when (selectedFilter) {
                HistoryFilter.All -> true
                HistoryFilter.Images -> !item.type.equals("VIDEO", ignoreCase = true)
                HistoryFilter.Videos -> item.type.equals("VIDEO", ignoreCase = true)
                HistoryFilter.Enhancer -> item.title.contains("enhance", ignoreCase = true)
                HistoryFilter.Compress -> item.title.contains("compress", ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate)
        FilterRow(selectedFilter = selectedFilter, onSelected = { selectedFilter = it })
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(filteredItems, key = { it.id }) { item ->
                HistoryTile(item = item, onClick = { onSelected(item, filteredItems) })
            }
        }
    }
}

@Composable
private fun HistoryTopBar(credits: Int, onNavigate: (String) -> Unit) {
    LumoraTopBar(
        credits = credits,
        onProfileClick = { onNavigate(Screen.Profile.route) },
        onCreditsClick = { onNavigate(Screen.Credits.route) },
        onNotificationsClick = { onNavigate(Screen.Notifications.route) },
    )
}

@Composable
private fun FilterRow(
    selectedFilter: HistoryFilter,
    onSelected: (HistoryFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HistoryFilter.entries.forEach { filter ->
            val selected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) Lime.copy(alpha = 0.16f) else FilterIdle)
                    .border(
                        1.dp,
                        if (selected) Lime.copy(alpha = 0.8f) else HistoryStroke,
                        RoundedCornerShape(50)
                    )
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    color = if (selected) Lime else Muted,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HistoryTile(item: HistoryModel, onClick: () -> Unit) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val file = remember(mediaPath) { File(mediaPath) }
    val fallbackRes = if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(103.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(HistoryPanel)
            .border(1.dp, HistoryStroke, RoundedCornerShape(6.dp))
            .clickable(enabled = mediaPath.isNotBlank()) { onClick() }
    ) {
        if (isVideo && mediaPath.isNotBlank() && file.exists()) {
            VideoFirstFrameThumbnail(
                filePath = mediaPath,
                contentDescription = item.title,
                fallbackImageRes = fallbackRes,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!isVideo && mediaPath.isNotBlank() && file.exists()) {
            AsyncImage(
                model = file,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = fallbackRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                    )
                )
        )

        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp)
                    .background(Color.Black.copy(alpha = 0.36f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "0:24",
                color = Color.White,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun HistoryMediaViewer(
    item: HistoryModel,
    itemsList: List<HistoryModel>,
    credits: Int,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onItemChanged: (HistoryModel) -> Unit,
) {
    val context = LocalContext.current
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val file = remember(mediaPath) { File(mediaPath) }
    var currentIndex by remember(item) { 
        mutableStateOf(itemsList.indexOfFirst { it.id == item.id }.let { if (it < 0) 0 else it })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("History", color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (file.exists()) {
                TextButton(
                    onClick = {
                        MediaShareUtils.shareMedia(
                            context = context,
                            filePath = mediaPath,
                            mimeType = if (isVideo) "video/mp4" else "image/png"
                        )
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", color = Lime, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = item.title.ifBlank { if (isVideo) "Video" else "Image" },
            color = Color.White,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Scrollable media viewer with carousel support
        var lastSwipeIndex by remember { mutableStateOf(-1) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, HistoryStroke, RoundedCornerShape(12.dp))
                .pointerInput(itemsList.size) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Reset the swipe tracker when drag ends
                            lastSwipeIndex = -1
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Only navigate if dragging more than 50 pixels threshold
                            // and we haven't already navigated for this drag
                            if (dragAmount > 50 && lastSwipeIndex != currentIndex) {
                                // Swipe right = previous (only if not at start)
                                if (currentIndex > 0) {
                                    currentIndex--
                                    lastSwipeIndex = currentIndex
                                    onItemChanged(itemsList[currentIndex])
                                }
                            } else if (dragAmount < -50 && lastSwipeIndex != currentIndex) {
                                // Swipe left = next (only if not at end)
                                if (currentIndex < itemsList.size - 1) {
                                    currentIndex++
                                    lastSwipeIndex = currentIndex
                                    onItemChanged(itemsList[currentIndex])
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (!file.exists()) {
                Text(
                    text = "Saved media file is missing.",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (isVideo) {
                ZoomableVideoPlayer(
                    filePath = mediaPath,
                    modifier = Modifier.fillMaxSize(),
                    showControls = true,
                    enableGestureDetection = false  // Parent handles carousel swipe
                )
            } else {
                ZoomableImageViewer(
                    filePath = mediaPath,
                    modifier = Modifier.fillMaxSize(),
                    showControls = true,
                    enableGestureDetection = false  // Parent handles carousel swipe
                )
            }
        }
        
        Text(
            text = item.createdAt,
            color = Muted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryEmpty(credits: Int, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate)
        FilterRow(selectedFilter = HistoryFilter.All, onSelected = {})
        AppEmptyScreen(
            title = "No Creations Yet",
            body = "Generated images and videos will appear here after you create them.",
        )
    }
}

