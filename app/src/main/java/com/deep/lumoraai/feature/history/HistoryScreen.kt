package com.deep.lumoraai.feature.history

import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.PlacementNativeAd
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
import com.deep.lumoraai.databinding.HistoryViewerBinding
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import java.io.File

private enum class HistoryFilter(val label: String) {
    All("All"), Images("Images"), Videos("Videos"), Enhancer("Enhanced"), Compress("Compressed")
}

private sealed interface HistoryGridItem {
    data class Content(val model: HistoryModel) : HistoryGridItem
    data class Ad(val slotIndex: Int) : HistoryGridItem
}

private val HeaderNotificationDot = Color(0xFFCFBDFF)

private data class ImageViewerTransform(
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

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
                        HistoryGrid(
                            items = filteredItems,
                            selectedIds = selectedIds,
                            onToggleSelected = onToggleSelected,
                            onOpen = onOpen,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryGrid(
    items: List<HistoryModel>,
    selectedIds: Set<String>,
    onToggleSelected: (String) -> Unit,
    onOpen: (HistoryModel) -> Unit,
) {
    val adInterval = LocalAdsConfigStore.current?.current?.nativeHistoryInterval ?: 3

    val columns = 2
    val gridItems: List<HistoryGridItem> = remember(items, adInterval) {
        buildList {
            var completedRows = 0
            var adSlotIndex = 0
            items.forEachIndexed { i, model ->
                add(HistoryGridItem.Content(model))
                val count = i + 1
                if (count % columns == 0) {
                    completedRows++
                    val isLastRow = count >= items.size
                    if (completedRows % adInterval == 0 && !isLastRow) {
                        add(HistoryGridItem.Ad(adSlotIndex++))
                    }
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(156.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        gridItems.forEach { gridItem ->
            when (gridItem) {
                is HistoryGridItem.Content -> {
                    val item = gridItem.model
                    item(key = item.id) {
                        HistoryTile(
                            item = item,
                            selected = item.id in selectedIds,
                            onToggleSelected = { onToggleSelected(item.id) },
                            onOpen = { onOpen(item) },
                        )
                    }
                }
                is HistoryGridItem.Ad -> {
                    item(
                        key = "native_ad_${gridItem.slotIndex}",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        PlacementNativeAd(
                            placement = AdPlacement.NATIVE_HISTORY,
                            slotKey = "native_history_${gridItem.slotIndex}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        )
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
            Text(stringResource(R.string.ui_history), color = PremiumText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        }
        Surface(
            onClick = { onNavigate(Screen.Credits.route) },
            modifier = Modifier.widthIn(min = 90.dp).height(30.dp),
            shape = RoundedCornerShape(50.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        ) {
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(painterResource(R.drawable.ic_lumora_star), null, tint = PremiumLime, modifier = Modifier.size(15.dp))
                Text(
                    if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else credits.toString(),
                    color = PremiumLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        Surface(
            onClick = { onNavigate(Screen.Notifications.route) },
            modifier = Modifier.padding(start = 14.dp).size(38.dp),
            shape = RoundedCornerShape(50.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_lumora_bell), "Open notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                if (unreadCount > 0) Box(Modifier.align(Alignment.TopEnd).padding(top = 7.dp, end = 7.dp).size(8.dp).clip(CircleShape).background(HeaderNotificationDot))
            }
        }
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
            Text(stringResource(R.string.ui_selected_count, selectedCount), color = PremiumText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onSelectAll) { Text(if (allSelected) stringResource(R.string.ui_clear_all) else stringResource(R.string.ui_select_all), color = PremiumLime) }
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
                Text(stringResource(R.string.ui_history_empty_title), color = PremiumText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.ui_history_empty_body), color = PremiumMuted, fontSize = 13.sp)
                Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = PremiumLime, contentColor = PremiumBackground), shape = RoundedCornerShape(14.dp)) { Text(stringResource(R.string.ui_start_creating), fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

@Composable
private fun FilterEmpty(filter: HistoryFilter) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.ui_no_filter_items, filter.label.lowercase()), color = PremiumMuted, fontSize = 14.sp)
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

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { androidContext ->
            HistoryViewerBinding.inflate(LayoutInflater.from(androidContext)).apply {
                root.tag = this
                root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) = Unit
                    override fun onViewDetachedFromWindow(v: View) {
                        releaseHistoryPlayer(this@apply)
                    }
                })
            }.root
        },
        update = { root ->
            val binding = root.tag as HistoryViewerBinding
            val exists = file.exists()

            binding.mediaTitle.text = item.title.ifBlank { if (isVideo) "Video" else "Image" }
            binding.createdAt.text = item.createdAt
            binding.backButton.setOnClickListener { onBack() }
            binding.feedbackButton.setOnClickListener { showFeedback = true }
            binding.deleteButton.setOnClickListener { onDelete() }

            binding.downloadButton.isEnabled = exists
            binding.downloadButton.alpha = if (exists) 1f else 0.35f
            binding.downloadButton.setOnClickListener {
                if (!file.exists()) return@setOnClickListener
                scope.launch {
                    val result = MediaGallerySaver.saveToGallery(context, file.absolutePath, mimeTypeFor(item), item.type)
                    Toast.makeText(
                        context,
                        result.fold({ "Saved to gallery" }, { it.message ?: "Could not save media." }),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            binding.shareButton.isEnabled = exists
            binding.shareButton.alpha = if (exists) 1f else 0.35f
            binding.shareButton.setOnClickListener {
                if (file.exists()) MediaShareUtils.shareMedia(context, file.absolutePath, mimeTypeFor(item))
            }

            binding.missingText.visibility = if (exists) View.GONE else View.VISIBLE
            binding.mediaImage.visibility = View.GONE
            binding.mediaVideo.visibility = View.GONE
            binding.imageControls.visibility = View.GONE
            binding.videoControls.visibility = View.GONE

            if (!exists) {
                releaseHistoryPlayer(binding)
                binding.mediaImage.setImageDrawable(null)
                binding.mediaFrame.tag = null
                return@AndroidView
            }

            val mediaKey = "${item.id}:${file.absolutePath}:${file.lastModified()}"
            val isNewMedia = binding.mediaFrame.tag != mediaKey
            if (isNewMedia) {
                binding.mediaFrame.tag = mediaKey
            }

            if (isVideo) {
                binding.mediaImage.setImageDrawable(null)
                binding.mediaImage.setOnTouchListener(null)
                binding.imageControls.visibility = View.GONE
                binding.videoControls.visibility = View.VISIBLE
                binding.mediaVideo.visibility = View.VISIBLE
                if (isNewMedia) {
                    releaseHistoryPlayer(binding)
                    val player = ExoPlayer.Builder(binding.root.context).build().apply {
                        repeatMode = Player.REPEAT_MODE_OFF
                        volume = 1f
                        setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                        addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                binding.playPauseButton.setImageResource(
                                    if (isPlaying) R.drawable.ic_lumora_pause else R.drawable.ic_lumora_play
                                )
                                binding.playPauseButton.contentDescription = binding.root.context.getString(
                                    if (isPlaying) R.string.ui_pause else R.string.ui_play
                                )
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                binding.mediaVideo.visibility = View.GONE
                                binding.videoControls.visibility = View.GONE
                                binding.missingText.visibility = View.VISIBLE
                                binding.missingText.text = "Could not play this video."
                                releaseHistoryPlayer(binding)
                            }
                        })
                        prepare()
                        playWhenReady = true
                    }
                    binding.mediaVideo.player = player
                    binding.mediaVideo.setTag(R.id.history_viewer_media_player, player)
                    binding.playPauseButton.setImageResource(R.drawable.ic_lumora_pause)
                    binding.playPauseButton.contentDescription = binding.root.context.getString(R.string.ui_pause)
                    binding.muteButton.isSelected = false
                    binding.muteButton.setImageResource(R.drawable.ic_lumora_volume_on)
                    binding.muteButton.contentDescription = binding.root.context.getString(R.string.ui_mute)
                }
                binding.playPauseButton.setOnClickListener {
                    val player = binding.mediaVideo.getTag(R.id.history_viewer_media_player) as? ExoPlayer ?: return@setOnClickListener
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
                binding.muteButton.setOnClickListener {
                    val player = binding.mediaVideo.getTag(R.id.history_viewer_media_player) as? ExoPlayer ?: return@setOnClickListener
                    val muted = !binding.muteButton.isSelected
                    binding.muteButton.isSelected = muted
                    player.volume = if (muted) 0f else 1f
                    binding.muteButton.setImageResource(
                        if (muted) R.drawable.ic_lumora_volume_off else R.drawable.ic_lumora_volume_on
                    )
                    binding.muteButton.contentDescription = binding.root.context.getString(
                        if (muted) R.string.ui_unmute else R.string.ui_mute
                    )
                }
            } else {
                releaseHistoryPlayer(binding)
                binding.videoControls.visibility = View.GONE
                binding.imageControls.visibility = View.VISIBLE
                binding.mediaImage.visibility = View.VISIBLE
                val gestureController = binding.mediaImage.getTag(R.id.history_viewer_image_gesture)
                    as? HistoryImageGestureController
                    ?: HistoryImageGestureController(binding) { next ->
                        binding.mediaImage.setTag(R.id.history_viewer_image_transform, next)
                        applyImageTransform(binding, next)
                    }.also {
                        binding.mediaImage.setTag(R.id.history_viewer_image_gesture, it)
                        binding.mediaImage.setOnTouchListener(it)
                    }
                if (isNewMedia) {
                    binding.mediaImage.setImageURI(Uri.fromFile(file))
                    gestureController.reset()
                }

                binding.zoomOutButton.setOnClickListener {
                    gestureController.zoomBy(1f / 1.2f)
                }
                binding.zoomInButton.setOnClickListener {
                    gestureController.zoomBy(1.2f)
                }
                binding.rotateLeftButton.setOnClickListener {
                    gestureController.rotateBy(-90f)
                }
                binding.rotateRightButton.setOnClickListener {
                    gestureController.rotateBy(90f)
                }
                binding.resetImageButton.setOnClickListener {
                    gestureController.reset()
                }
            }
        }
    )
}

private fun releaseHistoryPlayer(binding: HistoryViewerBinding) {
    (binding.mediaVideo.getTag(R.id.history_viewer_media_player) as? ExoPlayer)?.release()
    binding.mediaVideo.setTag(R.id.history_viewer_media_player, null)
    binding.mediaVideo.player = null
}

private fun applyImageTransform(binding: HistoryViewerBinding, transform: ImageViewerTransform) {
    binding.mediaImage.scaleX = transform.scale
    binding.mediaImage.scaleY = transform.scale
    binding.mediaImage.rotation = transform.rotation
    binding.mediaImage.translationX = transform.offsetX
    binding.mediaImage.translationY = transform.offsetY
    binding.zoomOutButton.isEnabled = transform.scale > 1f
    binding.zoomOutButton.alpha = if (transform.scale > 1f) 1f else 0.45f
}

private class HistoryImageGestureController(
    private val binding: HistoryViewerBinding,
    private val onTransform: (ImageViewerTransform) -> Unit,
) : View.OnTouchListener {
    private var transform = ImageViewerTransform()
    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false
    private var lastTapTime = 0L

    private val scaleDetector = ScaleGestureDetector(
        binding.mediaImage.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                zoomBy(detector.scaleFactor)
                return true
            }
        }
    )

    fun reset() {
        transform = ImageViewerTransform()
        publish()
    }

    fun zoomBy(factor: Float) {
        val nextScale = (transform.scale * factor).coerceIn(1f, 4f)
        transform = if (nextScale <= 1.01f) {
            transform.copy(scale = 1f, offsetX = 0f, offsetY = 0f)
        } else {
            transform.copy(scale = nextScale)
        }
        publish()
    }

    fun rotateBy(degrees: Float) {
        transform = transform.copy(rotation = transform.rotation + degrees)
        publish()
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                dragging = true
                handleDoubleTap(event.eventTime)
            }

            MotionEvent.ACTION_POINTER_DOWN -> dragging = false

            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && dragging && transform.scale > 1f) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    transform = transform.copy(
                        offsetX = clampOffset(transform.offsetX + dx, binding.mediaImage.width, transform.scale),
                        offsetY = clampOffset(transform.offsetY + dy, binding.mediaImage.height, transform.scale),
                    )
                    publish()
                    lastX = event.x
                    lastY = event.y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        view.parent?.requestDisallowInterceptTouchEvent(transform.scale > 1f || scaleDetector.isInProgress)
        return true
    }

    private fun handleDoubleTap(eventTime: Long) {
        if (eventTime - lastTapTime < DOUBLE_TAP_MS) {
            transform = if (transform.scale > 1f) ImageViewerTransform() else transform.copy(scale = 2f)
            publish()
            lastTapTime = 0L
        } else {
            lastTapTime = eventTime
        }
    }

    private fun publish() {
        binding.mediaImage.setTag(R.id.history_viewer_image_transform, transform)
        onTransform(transform)
    }

    private fun clampOffset(offset: Float, size: Int, scale: Float): Float {
        val max = (size * (scale - 1f) / 2f).coerceAtLeast(0f)
        return offset.coerceIn(-max, max)
    }

    private companion object {
        const val DOUBLE_TAP_MS = 280L
    }
}

@Composable
private fun FeedbackDialog(item: HistoryModel, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val options = listOf("Poor quality result", "Wrong image or video", "Download or share issue", "Preview or zoom issue", "Other issue")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurface,
        title = { Text(stringResource(R.string.ui_send_feedback), color = PremiumText, fontWeight = FontWeight.ExtraBold) },
        text = { Column { options.forEach { option -> TextButton(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) { Text(option, color = PremiumText, modifier = Modifier.fillMaxWidth()) } } } },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ui_cancel), color = PremiumMuted) } },
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
