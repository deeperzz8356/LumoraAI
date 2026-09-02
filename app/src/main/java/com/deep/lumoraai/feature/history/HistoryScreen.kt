package com.deep.lumoraai.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
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
import com.deep.lumoraai.core.utils.HistoryFeedbackReporter
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.OptIn

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
    onDeleteItems: (List<HistoryModel>) -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<HistoryModel?>(null) }

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
                    credits = credits,
                    onBack = { selectedItem = null },
                    onNavigate = onNavigate,
                    unreadCount = unreadCount,
                    onDelete = {
                        onDeleteItems(listOf(viewing))
                        selectedItem = null
                    }
                )
            } else {
                when (uiState) {
                    HistoryUiState.Loading -> AppLoadingScreen()
                    is HistoryUiState.Empty -> HistoryEmpty(credits = uiState.credits, onNavigate = onNavigate, unreadCount = unreadCount)
                    is HistoryUiState.Error -> AppErrorScreen(message = uiState.message)
                    is HistoryUiState.Success -> HistoryGallery(
                        items = uiState.items,
                        credits = uiState.credits,
                        onNavigate = onNavigate,
                        onDeleteItems = onDeleteItems,
                        unreadCount = unreadCount,
                        onSelected = { item ->
                            selectedItem = item
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
    onDeleteItems: (List<HistoryModel>) -> Unit,
    unreadCount: Int,
    onSelected: (HistoryModel) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
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
    val selectedItems = remember(filteredItems, selectedIds) {
        filteredItems.filter { it.id in selectedIds }
    }
    val selectionMode = selectedIds.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate, unreadCount = unreadCount)
        FilterRow(selectedFilter = selectedFilter, onSelected = { selectedFilter = it })
        if (selectionMode) {
            SelectionBar(
                selectedCount = selectedIds.size,
                allSelected = filteredItems.isNotEmpty() && selectedIds.containsAll(filteredItems.map { it.id }),
                onSelectAll = {
                    selectedIds = if (filteredItems.isNotEmpty() && selectedIds.containsAll(filteredItems.map { it.id })) {
                        emptySet()
                    } else {
                        filteredItems.map { it.id }.toSet()
                    }
                },
                onDelete = {
                    onDeleteItems(selectedItems)
                    selectedIds = emptySet()
                },
                onCancel = { selectedIds = emptySet() }
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val selected = item.id in selectedIds
                HistoryTile(
                    item = item,
                    selected = selected,
                    selectionMode = selectionMode,
                    onClick = {
                        if (selectionMode) {
                            selectedIds = selectedIds.toggle(item.id)
                        } else {
                            onSelected(item)
                        }
                    },
                    onLongPress = { selectedIds = selectedIds + item.id },
                )
            }
        }
    }
}

@Composable
private fun HistoryTopBar(credits: Int, onNavigate: (String) -> Unit, unreadCount: Int = 0) {
    LumoraTopBar(
        credits = credits,
        title = "History",
        onProfileClick = { onNavigate(Screen.Profile.route) },
        onCreditsClick = { onNavigate(Screen.Credits.route) },
        onNotificationsClick = { onNavigate(Screen.Notifications.route) },
        hasUnreadNotifications = unreadCount > 0,
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
private fun SelectionBar(
    selectedCount: Int,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HistoryPanel)
            .border(1.dp, Lime.copy(alpha = 0.24f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$selectedCount selected",
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onSelectAll) {
                Text(if (allSelected) "Clear" else "Select all", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDelete, enabled = selectedCount > 0) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF7A7A), modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = Color(0xFFFF7A7A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onCancel) {
                Text("Done", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryTile(
    item: HistoryModel,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val file = remember(mediaPath) { File(mediaPath) }
    val fallbackRes = if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(6.dp))
            .background(HistoryPanel)
            .border(
                1.dp,
                if (selected) Lime.copy(alpha = 0.9f) else HistoryStroke,
                RoundedCornerShape(6.dp)
            )
            .combinedClickable(
                enabled = mediaPath.isNotBlank(),
                onClick = onClick,
                onLongClick = onLongPress,
            )
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

        if (selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.58f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (selected) "Selected" else "Not selected",
                    tint = if (selected) Lime else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryMediaViewer(
    item: HistoryModel,
    credits: Int,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    unreadCount: Int,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val file = remember(mediaPath) { File(mediaPath) }
    var showFeedbackDialog by remember(item.id) { mutableStateOf(false) }

    if (showFeedbackDialog) {
        FeedbackDialog(
            item = item,
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { reason ->
                HistoryFeedbackReporter.submit(context, item, reason)
                Toast.makeText(context, "Thanks, feedback saved.", Toast.LENGTH_SHORT).show()
                showFeedbackDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate, unreadCount = unreadCount)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewerActionButton(
                label = "History",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                tint = Color.White,
                onClick = onBack
            )
            if (file.exists()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    ViewerIconButton(
                        icon = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Lime,
                        onClick = {
                            scope.launch {
                                val result = MediaGallerySaver.saveToGallery(
                                    context = context,
                                    filePath = mediaPath,
                                    mimeType = mimeTypeFor(item),
                                    mediaType = item.type,
                                )
                                Toast.makeText(
                                    context,
                                    result.fold(
                                        onSuccess = {
                                            if (isVideo) "Video saved to gallery" else "Image saved to gallery"
                                        },
                                        onFailure = {
                                            it.message ?: if (isVideo) {
                                                "Could not save video to gallery."
                                            } else {
                                                "Could not save image to gallery."
                                            }
                                        }
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    ViewerIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Lime,
                        onClick = {
                            MediaShareUtils.shareMedia(
                                context = context,
                                filePath = mediaPath,
                                mimeType = mimeTypeFor(item)
                            )
                        }
                    )
                    ViewerIconButton(
                        icon = Icons.Default.Feedback,
                        contentDescription = "Feedback",
                        tint = Color(0xFFCFBDFF),
                        onClick = { showFeedbackDialog = true }
                    )
                    ViewerIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF7A7A),
                        onClick = onDelete
                    )
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
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, HistoryStroke, RoundedCornerShape(12.dp)),
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
                    enableGestureDetection = true
                )
            } else {
                ZoomableImageViewer(
                    filePath = mediaPath,
                    modifier = Modifier.fillMaxSize(),
                    showControls = true,
                    enableGestureDetection = true
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
private fun FeedbackDialog(
    item: HistoryModel,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val options = listOf(
        "Poor quality result",
        "Wrong image or video",
        "Download or share issue",
        "Preview or zoom issue",
        "Other issue",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HistoryPanel,
        title = {
            Text(
                text = "Send Feedback",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.title.ifBlank { if (item.type.equals("VIDEO", ignoreCase = true)) "Video" else "Image" },
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .clickable { onSubmit(option) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = null,
                            tint = Lime,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = option,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Muted, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ViewerActionButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = tint, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ViewerIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

private fun Set<String>.toggle(id: String): Set<String> =
    if (id in this) this - id else this + id

private fun mimeTypeFor(item: HistoryModel): String {
    if (item.type.equals("VIDEO", ignoreCase = true)) return "video/mp4"
    val path = item.mediaUrl.orEmpty().lowercase()
    return when {
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        path.endsWith(".webp") -> "image/webp"
        else -> "image/png"
    }
}

@Composable
private fun HistoryEmpty(credits: Int, onNavigate: (String) -> Unit, unreadCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HistoryTopBar(credits = credits, onNavigate = onNavigate, unreadCount = unreadCount)
        FilterRow(selectedFilter = HistoryFilter.All, onSelected = {})
        AppEmptyScreen(
            title = "No Creations Yet",
            body = "Generated images and videos will appear here after you create them.",
        )
    }
}

