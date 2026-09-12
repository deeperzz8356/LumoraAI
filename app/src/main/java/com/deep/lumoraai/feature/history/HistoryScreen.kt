package com.deep.lumoraai.feature.history

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.PremiumBackground
import com.deep.lumoraai.core.components.PremiumDanger
import com.deep.lumoraai.core.components.PremiumLime
import com.deep.lumoraai.core.components.PremiumMuted
import com.deep.lumoraai.core.components.PremiumStroke
import com.deep.lumoraai.core.components.PremiumSurface
import com.deep.lumoraai.core.components.PremiumText
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.HistoryFeedbackReporter
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import kotlinx.coroutines.launch
import java.io.File

private enum class HistoryFilter(val label: String) {
    All("All"), Images("Images"), Videos("Videos"), Enhancer("Enhanced"), Compress("Compressed")
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onDeleteItems: (List<HistoryModel>) -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    var selectedItem by remember { mutableStateOf<HistoryModel?>(null) }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PremiumBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "history", onNavigate) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().background(PremiumBackground).padding(innerPadding)) {
            selectedItem?.let { item ->
                HistoryViewer(
                    item = item,
                    onBack = { selectedItem = null },
                    onDelete = {
                        onDeleteItems(listOf(item))
                        selectedItem = null
                    },
                )
            } ?: HistoryGallery(
                uiState = uiState,
                selectedFilter = selectedFilter,
                selectedIds = selectedIds,
                unreadCount = unreadCount,
                onNavigate = onNavigate,
                onFilterChange = {
                    selectedFilter = it
                    selectedIds = emptySet()
                },
                onToggleSelected = { id -> selectedIds = selectedIds.toggle(id) },
                onClearSelection = { selectedIds = emptySet() },
                onOpen = { selectedItem = it },
                onDelete = { items ->
                    onDeleteItems(items)
                    selectedIds = emptySet()
                },
            )
        }
    }
}

@Composable
private fun HistoryGallery(
    uiState: HistoryUiState,
    selectedFilter: HistoryFilter,
    selectedIds: Set<String>,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
    onFilterChange: (HistoryFilter) -> Unit,
    onToggleSelected: (String) -> Unit,
    onClearSelection: () -> Unit,
    onOpen: (HistoryModel) -> Unit,
    onDelete: (List<HistoryModel>) -> Unit,
) {
    val credits = when (uiState) {
        is HistoryUiState.Success -> uiState.credits
        is HistoryUiState.Empty -> uiState.credits
        else -> 0
    }
    val allItems = (uiState as? HistoryUiState.Success)?.items.orEmpty()
    val filteredItems = remember(allItems, selectedFilter) { filterItems(allItems, selectedFilter) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sidePadding = if (maxWidth >= 600.dp) 32.dp else 18.dp
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxSize().widthIn(max = 980.dp).padding(horizontal = sidePadding).padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HistoryHeader(credits, unreadCount, onNavigate)
            FilterRow(selectedFilter, onFilterChange)
            if (selectedIds.isNotEmpty()) {
                SelectionBar(
                    selectedCount = selectedIds.size,
                    allSelected = filteredItems.isNotEmpty() && filteredItems.all { it.id in selectedIds },
                    onSelectAll = {
                        if (filteredItems.all { it.id in selectedIds }) onClearSelection()
                        else filteredItems.filterNot { it.id in selectedIds }.forEach { onToggleSelected(it.id) }
                    },
                    onDelete = { onDelete(filteredItems.filter { it.id in selectedIds }) },
                    onClear = onClearSelection,
                )
            }
            when (uiState) {
                HistoryUiState.Loading -> AppLoadingScreen()
                is HistoryUiState.Error -> AppErrorScreen(uiState.message)
                is HistoryUiState.Empty -> HistoryEmpty { onNavigate(Screen.Home.route) }
                is HistoryUiState.Success -> {
                    if (filteredItems.isEmpty()) {
                        FilterEmpty(selectedFilter)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(156.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filteredItems, key = { it.id }) { item ->
                                HistoryTile(item, item.id in selectedIds, { onToggleSelected(item.id) }, { onOpen(item) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(credits: Int, unreadCount: Int, onNavigate: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("YOUR ARCHIVE", color = PremiumLime, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Text("History", color = PremiumText, fontSize = 30.sp, lineHeight = 33.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp, modifier = Modifier.semantics { heading() })
            Text("Everything you've made, ready to revisit.", color = PremiumMuted, fontSize = 11.sp)
        }
        Surface(
            onClick = { onNavigate(Screen.Credits.route) },
            shape = RoundedCornerShape(12.dp),
            color = PremiumLime.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, PremiumLime.copy(alpha = 0.24f)),
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CreditCard, null, tint = PremiumLime, modifier = Modifier.size(17.dp))
                Text(if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "∞" else credits.toString(), color = PremiumLime, fontWeight = FontWeight.Black)
            }
        }
        Box {
            IconButton(onClick = { onNavigate(Screen.Notifications.route) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Notifications, "Notifications", tint = PremiumText) }
            if (unreadCount > 0) Box(Modifier.align(Alignment.TopEnd).size(9.dp).clip(CircleShape).background(PremiumLime))
        }
        IconButton(onClick = { onNavigate(Screen.Profile.route) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Person, "Profile", tint = PremiumText) }
    }
}

@Composable
private fun FilterRow(selected: HistoryFilter, onSelected: (HistoryFilter) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryFilter.entries.forEach { filter ->
            val active = filter == selected
            Surface(
                onClick = { onSelected(filter) },
                shape = RoundedCornerShape(12.dp),
                color = if (active) PremiumLime else PremiumSurface,
                border = BorderStroke(1.dp, if (active) PremiumLime else PremiumStroke),
            ) {
                Text(filter.label, color = if (active) PremiumBackground else PremiumMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun SelectionBar(selectedCount: Int, allSelected: Boolean, onSelectAll: () -> Unit, onDelete: () -> Unit, onClear: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumLime.copy(alpha = 0.25f))) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$selectedCount selected", color = PremiumText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onSelectAll) { Text(if (allSelected) "Clear all" else "Select all", color = PremiumLime) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete selected", tint = PremiumDanger) }
            IconButton(onClick = onClear) { Icon(Icons.Default.Close, "Cancel selection", tint = PremiumMuted) }
        }
    }
}

@Composable
private fun HistoryTile(item: HistoryModel, selected: Boolean, onToggleSelected: () -> Unit, onOpen: () -> Unit) {
    val file = item.mediaUrl?.let(::File)
    val isVideo = item.type.equals("VIDEO", true)
    val thumbnail = remember(item.mediaUrl, item.type) { if (file?.exists() == true && isVideo) videoFrame(file) else null }
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PremiumSurface,
        border = BorderStroke(1.dp, if (selected) PremiumLime else PremiumStroke.copy(alpha = 0.75f)),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.78f).background(PremiumStroke)) {
            when {
                thumbnail != null -> Image(thumbnail.asImageBitmap(), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                file?.exists() == true && !isVideo -> AsyncImage(Uri.fromFile(file), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else -> Image(painterResource(if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9080D18)))))
            if (isVideo) Box(Modifier.align(Alignment.Center).size(42.dp).clip(CircleShape).background(PremiumLime), contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, null, tint = PremiumBackground) }
            IconButton(
                onClick = onToggleSelected,
                modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(48.dp),
            ) {
                Icon(if (selected) Icons.Default.CheckCircle else Icons.Default.Check, if (selected) "Deselect ${item.title}" else "Select ${item.title}", tint = if (selected) PremiumLime else PremiumText)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(11.dp)) {
                Text(item.title.ifBlank { if (isVideo) "Video" else "Image" }, color = PremiumText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.createdAt, color = PremiumMuted, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HistoryEmpty(onCreate: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(26.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumStroke)) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(64.dp).background(PremiumLime.copy(alpha = 0.10f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = PremiumLime, modifier = Modifier.size(32.dp)) }
                Text("Your archive starts here", color = PremiumText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Create an image or video and it will appear here.", color = PremiumMuted, fontSize = 13.sp)
                Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = PremiumLime, contentColor = PremiumBackground), shape = RoundedCornerShape(14.dp)) { Text("Start creating", fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

@Composable
private fun FilterEmpty(filter: HistoryFilter) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No ${filter.label.lowercase()} yet.", color = PremiumMuted, fontSize = 14.sp)
    }
}

@Composable
private fun HistoryViewer(item: HistoryModel, onBack: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val file = remember(item.mediaUrl) { File(item.mediaUrl.orEmpty()) }
    val isVideo = item.type.equals("VIDEO", true)
    var showFeedback by remember { mutableStateOf(false) }
    if (showFeedback) FeedbackDialog(item, { showFeedback = false }) { reason ->
        HistoryFeedbackReporter.submit(context, item, reason)
        Toast.makeText(context, "Thanks, feedback saved.", Toast.LENGTH_SHORT).show()
        showFeedback = false
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).padding(top = 14.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to history", tint = PremiumText) }
            Column(Modifier.weight(1f)) {
                Text(item.title.ifBlank { if (isVideo) "Video" else "Image" }, color = PremiumText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.createdAt, color = PremiumMuted, fontSize = 11.sp)
            }
        }
        Surface(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumStroke)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    !file.exists() -> Text("This media file is no longer available.", color = PremiumMuted)
                    isVideo -> AndroidView(
                        factory = { VideoView(it).apply { setVideoURI(Uri.fromFile(file)); setOnPreparedListener { player -> player.isLooping = true; start() } } },
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> AsyncImage(Uri.fromFile(file), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewerAction("Save", Icons.Default.Download, file.exists(), Modifier.weight(1f)) {
                scope.launch {
                    val result = MediaGallerySaver.saveToGallery(context, file.absolutePath, mimeTypeFor(item), item.type)
                    Toast.makeText(context, result.fold({ "Saved to gallery" }, { it.message ?: "Could not save media." }), Toast.LENGTH_LONG).show()
                }
            }
            ViewerAction("Share", Icons.Default.Share, file.exists(), Modifier.weight(1f)) { MediaShareUtils.shareMedia(context, file.absolutePath, mimeTypeFor(item)) }
            ViewerAction("Feedback", Icons.Default.Feedback, true, Modifier.weight(1f)) { showFeedback = true }
            ViewerAction("Delete", Icons.Default.DeleteOutline, true, Modifier.weight(1f), danger = true, onClick = onDelete)
        }
    }
}

@Composable
private fun ViewerAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, modifier: Modifier, danger: Boolean = false, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.height(62.dp), shape = RoundedCornerShape(15.dp), color = PremiumSurface, border = BorderStroke(1.dp, if (danger) PremiumDanger.copy(alpha = 0.35f) else PremiumStroke)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (danger) PremiumDanger else if (enabled) PremiumLime else PremiumMuted, modifier = Modifier.size(20.dp))
            Text(label, color = if (danger) PremiumDanger else PremiumText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeedbackDialog(item: HistoryModel, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("Poor quality result", "Wrong image or video", "Download or share issue", "Preview or zoom issue", "Other issue")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurface,
        title = { Text("Send feedback", color = PremiumText, fontWeight = FontWeight.ExtraBold) },
        text = { Column { options.forEach { option -> TextButton(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) { Text(option, color = PremiumText, modifier = Modifier.fillMaxWidth()) } } } },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumMuted) } },
    )
}

private fun filterItems(items: List<HistoryModel>, filter: HistoryFilter): List<HistoryModel> = items.filter { item ->
    when (filter) {
        HistoryFilter.All -> true
        HistoryFilter.Images -> !item.type.equals("VIDEO", true)
        HistoryFilter.Videos -> item.type.equals("VIDEO", true)
        HistoryFilter.Enhancer -> item.title.contains("enhance", true)
        HistoryFilter.Compress -> item.title.contains("compress", true)
    }
}

private fun Set<String>.toggle(id: String) = if (id in this) this - id else this + id

private fun videoFrame(file: File) = runCatching {
    android.media.MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}.getOrNull()

private fun mimeTypeFor(item: HistoryModel): String {
    if (item.type.equals("VIDEO", true)) return "video/mp4"
    return when {
        item.mediaUrl.orEmpty().endsWith(".jpg", true) || item.mediaUrl.orEmpty().endsWith(".jpeg", true) -> "image/jpeg"
        item.mediaUrl.orEmpty().endsWith(".webp", true) -> "image/webp"
        else -> "image/png"
    }
}
