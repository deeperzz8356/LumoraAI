package com.deep.lumoraai.feature.history

import android.app.AlertDialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.HistoryFeedbackReporter
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.databinding.HistoryItemTileBinding
import com.deep.lumoraai.databinding.HistoryScreenBinding
import com.deep.lumoraai.databinding.HistoryViewerBinding
import kotlinx.coroutines.launch
import java.io.File

private enum class HistoryFilter {
    All,
    Images,
    Videos,
    Enhancer,
    Compress,
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
    var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF081020),
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
                .background(Color(0xFF081020))
                .padding(padding)
        ) {
            val viewing = selectedItem
            if (viewing != null) {
                AndroidView(
                    factory = { HistoryViewerBinding.inflate(LayoutInflater.from(it)).root },
                    update = { root ->
                        bindHistoryViewer(
                            binding = HistoryViewerBinding.bind(root),
                            item = viewing,
                            scope = scope,
                            onBack = { selectedItem = null },
                            onDelete = {
                                onDeleteItems(listOf(viewing))
                                selectedItem = null
                            },
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { HistoryScreenBinding.inflate(LayoutInflater.from(it)).root },
                    update = { root ->
                        val binding = HistoryScreenBinding.bind(root)
                        bindHistoryScreen(
                            binding = binding,
                            uiState = uiState,
                            selectedFilter = selectedFilter,
                            selectedIds = selectedIds,
                            unreadCount = unreadCount,
                            onNavigate = onNavigate,
                            onFilterChanged = {
                                selectedFilter = it
                                selectedIds = emptySet()
                            },
                            onSelectedIdsChanged = { selectedIds = it },
                            onOpenItem = { selectedItem = it },
                            onDeleteItems = onDeleteItems,
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun bindHistoryScreen(
    binding: HistoryScreenBinding,
    uiState: HistoryUiState,
    selectedFilter: HistoryFilter,
    selectedIds: Set<String>,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
    onFilterChanged: (HistoryFilter) -> Unit,
    onSelectedIdsChanged: (Set<String>) -> Unit,
    onOpenItem: (HistoryModel) -> Unit,
    onDeleteItems: (List<HistoryModel>) -> Unit,
) {
    val credits = when (uiState) {
        is HistoryUiState.Empty -> uiState.credits
        is HistoryUiState.Success -> uiState.credits
        else -> 0
    }
    bindTopBar(binding, credits, unreadCount, onNavigate)
    bindFilters(binding, selectedFilter, onFilterChanged)

    when (uiState) {
        HistoryUiState.Loading -> showMessage(binding, binding.root.context.getString(R.string.loading))
        is HistoryUiState.Error -> showMessage(binding, uiState.message)
        is HistoryUiState.Empty -> showMessage(binding, binding.root.context.getString(R.string.ui_history_empty_body))
        is HistoryUiState.Success -> {
            val filtered = filterItems(uiState.items, selectedFilter)
            if (filtered.isEmpty()) {
                showMessage(binding, "No items for this filter.")
            } else {
                binding.messageText.visibility = View.GONE
                binding.gridScroll.visibility = View.VISIBLE
                bindSelectionBar(binding, filtered, selectedIds, onSelectedIdsChanged, onDeleteItems)
                bindGrid(binding.historyGrid, filtered, selectedIds, onSelectedIdsChanged, onOpenItem)
            }
        }
    }
}

private fun bindTopBar(
    binding: HistoryScreenBinding,
    credits: Int,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
) {
    binding.avatar.setOnClickListener { onNavigate(Screen.Profile.route) }
    binding.creditsChip.text = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else credits.toString()
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.unreadDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
}

private fun bindFilters(
    binding: HistoryScreenBinding,
    selected: HistoryFilter,
    onSelected: (HistoryFilter) -> Unit,
) {
    if (binding.filterRow.childCount != HistoryFilter.entries.size) {
        binding.filterRow.removeAllViews()
        HistoryFilter.entries.forEach { filter ->
            val button = Button(binding.root.context).apply {
                minWidth = 0
                minHeight = 0
                textSize = 12f
                setPadding(dp(this, 16), 0, dp(this, 16), 0)
                setOnClickListener { onSelected(filter) }
            }
            binding.filterRow.addView(button, ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(button, 40)).apply {
                setMargins(0, 0, dp(button, 12), 0)
            })
        }
    }
    HistoryFilter.entries.forEachIndexed { index, filter ->
        (binding.filterRow.getChildAt(index) as? Button)?.apply {
            text = filter.label(binding.root)
            setTextColor(if (filter == selected) 0xFFD6FF2F.toInt() else 0xFF94A0B8.toInt())
            setBackgroundResource(if (filter == selected) R.drawable.bg_common_action_lime else R.drawable.bg_common_card)
        }
    }
}

private fun bindSelectionBar(
    binding: HistoryScreenBinding,
    items: List<HistoryModel>,
    selectedIds: Set<String>,
    onSelectedIdsChanged: (Set<String>) -> Unit,
    onDeleteItems: (List<HistoryModel>) -> Unit,
) {
    val selectionMode = selectedIds.isNotEmpty()
    binding.selectionBar.visibility = if (selectionMode) View.VISIBLE else View.GONE
    binding.selectionCount.text = "${selectedIds.size} selected"
    val allSelected = items.isNotEmpty() && selectedIds.containsAll(items.map { it.id })
    binding.selectAllButton.text = if (allSelected) "Clear" else "Select all"
    binding.selectAllButton.setOnClickListener {
        onSelectedIdsChanged(if (allSelected) emptySet() else items.map { it.id }.toSet())
    }
    binding.deleteButton.setOnClickListener {
        onDeleteItems(items.filter { it.id in selectedIds })
        onSelectedIdsChanged(emptySet())
    }
    binding.cancelSelectionButton.setOnClickListener { onSelectedIdsChanged(emptySet()) }
}

private fun bindGrid(
    grid: GridLayout,
    items: List<HistoryModel>,
    selectedIds: Set<String>,
    onSelectedIdsChanged: (Set<String>) -> Unit,
    onOpenItem: (HistoryModel) -> Unit,
) {
    grid.removeAllViews()
    val selectionMode = selectedIds.isNotEmpty()
    items.forEachIndexed { index, item ->
        val tile = HistoryItemTileBinding.inflate(LayoutInflater.from(grid.context), grid, false)
        val isVideo = item.type.equals("VIDEO", ignoreCase = true)
        bindMediaThumb(tile.mediaImage, item, if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy)
        tile.playBadge.visibility = if (isVideo) View.VISIBLE else View.GONE
        tile.duration.visibility = if (isVideo) View.VISIBLE else View.GONE
        tile.selectionBadge.visibility = if (selectionMode && item.id in selectedIds) View.VISIBLE else View.GONE
        tile.root.clipToOutline = true
        tile.mediaImage.clipToOutline = true
        tile.root.setOnClickListener {
            if (selectionMode) onSelectedIdsChanged(selectedIds.toggle(item.id)) else onOpenItem(item)
        }
        tile.root.setOnLongClickListener {
            onSelectedIdsChanged(selectedIds + item.id)
            true
        }
        grid.addView(tile.root, gridParams(index, grid, 9))
    }
}

private fun bindHistoryViewer(
    binding: HistoryViewerBinding,
    item: HistoryModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = binding.root.context
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val file = File(mediaPath)
    binding.backButton.setOnClickListener { onBack() }
    binding.mediaTitle.text = item.title.ifBlank { if (isVideo) "Video" else "Image" }
    binding.createdAt.text = item.createdAt
    binding.missingText.visibility = if (file.exists()) View.GONE else View.VISIBLE
    binding.mediaImage.visibility = if (file.exists() && !isVideo) View.VISIBLE else View.GONE
    binding.mediaVideo.visibility = if (file.exists() && isVideo) View.VISIBLE else View.GONE
    if (file.exists() && isVideo) {
        binding.mediaVideo.setVideoURI(Uri.fromFile(file))
        binding.mediaVideo.setOnPreparedListener { player ->
            player.isLooping = true
            binding.mediaVideo.start()
        }
    } else if (file.exists()) {
        binding.mediaImage.setImageURI(Uri.fromFile(file))
    }
    binding.downloadButton.visibility = if (file.exists()) View.VISIBLE else View.GONE
    binding.shareButton.visibility = if (file.exists()) View.VISIBLE else View.GONE
    binding.downloadButton.setOnClickListener {
        Toast.makeText(context, context.getString(R.string.ui_downloading), Toast.LENGTH_SHORT).show()
        scope.launch {
            val result = MediaGallerySaver.saveToGallery(context, mediaPath, mimeTypeFor(item), item.type)
            Toast.makeText(
                context,
                result.fold(
                    onSuccess = { if (isVideo) "Video saved to gallery" else "Image saved to gallery" },
                    onFailure = { it.message ?: "Could not save media to gallery." },
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    binding.shareButton.setOnClickListener {
        MediaShareUtils.shareMedia(context, mediaPath, mimeTypeFor(item))
    }
    binding.feedbackButton.setOnClickListener { showFeedbackDialog(binding.root, item) }
    binding.deleteButton.setOnClickListener { onDelete() }
}

private fun showFeedbackDialog(anchor: View, item: HistoryModel) {
    val context = anchor.context
    val options = arrayOf(
        "Poor quality result",
        "Wrong image or video",
        "Download or share issue",
        "Preview or zoom issue",
        "Other issue",
    )
    AlertDialog.Builder(context)
        .setTitle("Send Feedback")
        .setItems(options) { dialog, which ->
            HistoryFeedbackReporter.submit(context, item, options[which])
            Toast.makeText(context, "Thanks, feedback saved.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        .setNegativeButton(R.string.ui_cancel, null)
        .show()
}

private fun showMessage(binding: HistoryScreenBinding, message: String) {
    binding.selectionBar.visibility = View.GONE
    binding.gridScroll.visibility = View.GONE
    binding.messageText.visibility = View.VISIBLE
    binding.messageText.text = message
}

private fun filterItems(items: List<HistoryModel>, filter: HistoryFilter): List<HistoryModel> =
    items.filter { item ->
        when (filter) {
            HistoryFilter.All -> true
            HistoryFilter.Images -> !item.type.equals("VIDEO", ignoreCase = true)
            HistoryFilter.Videos -> item.type.equals("VIDEO", ignoreCase = true)
            HistoryFilter.Enhancer -> item.title.contains("enhance", ignoreCase = true)
            HistoryFilter.Compress -> item.title.contains("compress", ignoreCase = true)
        }
    }

private fun HistoryFilter.label(view: View): String =
    when (this) {
        HistoryFilter.All -> view.context.getString(R.string.ui_filter_all)
        HistoryFilter.Images -> view.context.getString(R.string.ui_filter_image)
        HistoryFilter.Videos -> view.context.getString(R.string.ui_filter_video)
        HistoryFilter.Enhancer -> view.context.getString(R.string.ui_filter_enhancer)
        HistoryFilter.Compress -> view.context.getString(R.string.ui_filter_compress)
    }.uppercase()

private fun bindMediaThumb(image: android.widget.ImageView, item: HistoryModel, fallbackRes: Int) {
    val path = item.mediaUrl.orEmpty()
    val file = File(path)
    when {
        path.isBlank() || !file.exists() -> image.setImageResource(fallbackRes)
        item.type.equals("VIDEO", ignoreCase = true) -> videoFrame(file)?.let { image.setImageBitmap(it) } ?: image.setImageResource(fallbackRes)
        else -> image.setImageURI(Uri.fromFile(file))
    }
    image.contentDescription = item.title
}

private fun gridParams(index: Int, view: View, gapDp: Int): GridLayout.LayoutParams {
    val gapPx = dp(view, gapDp)
    val availableWidth = (view.width.takeIf { it > 0 } ?: view.resources.displayMetrics.widthPixels) - gapPx
    val columnWidth = (availableWidth / 2).coerceAtLeast(dp(view, 120))
    val tileHeight = (columnWidth * 4f / 3f).toInt()
    return GridLayout.LayoutParams(
        GridLayout.spec(index / 2, 1),
        GridLayout.spec(index % 2, 1)
    ).apply {
        width = columnWidth
        height = tileHeight
        setMargins(
            if (index % 2 == 0) 0 else dp(view, gapDp / 2),
            if (index < 2) 0 else dp(view, gapDp),
            if (index % 2 == 0) dp(view, gapDp / 2) else 0,
            0
        )
    }
}

private fun Set<String>.toggle(id: String): Set<String> = if (id in this) this - id else this + id

private fun dp(view: View, value: Int): Int = (value * view.resources.displayMetrics.density).toInt()

private fun videoFrame(file: File): Bitmap? = runCatching {
    MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}.getOrNull()

private fun mimeTypeFor(item: HistoryModel): String {
    if (item.type.equals("VIDEO", ignoreCase = true)) return "video/mp4"
    val path = item.mediaUrl.orEmpty().lowercase()
    return when {
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        path.endsWith(".webp") -> "image/webp"
        else -> "image/png"
    }
}
