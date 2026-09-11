package com.deep.lumoraai.feature.photoenhance

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementBanner
import com.deep.lumoraai.core.components.ZoomableImageViewer
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.databinding.PhotoEnhanceScreenBinding
import compose.icons.TablerIcons
import compose.icons.tablericons.Adjustments
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Download
import compose.icons.tablericons.Share
import compose.icons.tablericons.Upload
import compose.icons.tablericons.Wand
import compose.icons.tablericons.X
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PhotoEnhanceScreen(
    uiState: PhotoEnhanceUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onResolutionSelected: (EnhanceOption) -> Unit,
    onSharpnessChanged: (Float) -> Unit,
    onLightingSelected: (EnhanceOption) -> Unit,
    onEnhance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }
    var viewerPath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComposeColor(0xFF081020),
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor(0xFF081020))
                .systemBarsPadding()
                .padding(padding)
        ) {
            AndroidView(
                factory = { PhotoEnhanceScreenBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindPhotoEnhance(
                        binding = PhotoEnhanceScreenBinding.bind(root),
                        uiState = uiState,
                        onBack = onBack,
                        onNavigate = onNavigate,
                        onUpload = { imagePicker.launch("image/*") },
                        onResolutionSelected = onResolutionSelected,
                        onSharpnessChanged = onSharpnessChanged,
                        onLightingSelected = onLightingSelected,
                        onEnhance = onEnhance,
                        onOpenViewer = { path -> viewerPath = path },
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            PlacementBanner(
                placement = AdPlacement.BANNER_ENHANCER,
                modifier = Modifier.padding(horizontal = 0.dp),
                applyNavBarPadding = false,
            )
        }
    }

    viewerPath?.let { path ->
        EnhancedResultViewerDialog(
            filePath = path,
            onDismiss = { viewerPath = null }
        )
    }
}

private fun bindPhotoEnhance(
    binding: PhotoEnhanceScreenBinding,
    uiState: PhotoEnhanceUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onUpload: () -> Unit,
    onResolutionSelected: (EnhanceOption) -> Unit,
    onSharpnessChanged: (Float) -> Unit,
    onLightingSelected: (EnhanceOption) -> Unit,
    onEnhance: () -> Unit,
    onOpenViewer: (String) -> Unit,
) {
    val context = binding.root.context
    binding.backButton.setOnClickListener { onBack() }
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
    binding.creditsChip.text = if (uiState.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        uiState.credits.toString()
    }
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.creditsChip.compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.lumora_lime))
    bindTablerIcon(binding.bellIconHost, TablerIcons.Bell, ComposeColor.White)
    bindTablerIcon(binding.uploadIconHost, TablerIcons.Upload, ComposeColor(0xFFD4FF3B))
    bindTablerIcon(binding.enhancementIconHost, TablerIcons.Adjustments, ComposeColor(0xFFD4FF3B))
    bindTablerIcon(binding.enhanceButtonIconHost, TablerIcons.Wand, ComposeColor.Black)

    binding.uploadPanel.clipToOutline = true
    binding.previewImage.clipToOutline = true
    binding.uploadPanel.setOnClickListener { onUpload() }
    val preview = if (uiState.enhancedBitmap == null) uiState.originalBitmap else null
    binding.previewImage.visibility = if (preview == null) View.GONE else View.VISIBLE
    binding.uploadEmpty.visibility = if (preview == null) View.VISIBLE else View.GONE
    preview?.let { binding.previewImage.setImageBitmap(it) }
    bindComparisonResult(binding, uiState, onOpenViewer)

    bindOptionRow(
        selected = uiState.resolution,
        options = listOf(binding.resLow, binding.resMed, binding.resHigh, binding.resUltra),
        onSelected = onResolutionSelected
    )
    bindOptionRow(
        selected = uiState.lighting,
        options = listOf(binding.lightLow, binding.lightMed, binding.lightHigh, binding.lightUltra),
        onSelected = onLightingSelected
    )

    val percent = (uiState.sharpness.coerceIn(0f, 1f) * 100).toInt()
    binding.detailPercent.text = "$percent%"
    binding.sharpnessSlider.setOnSeekBarChangeListener(null)
    binding.sharpnessSlider.progress = percent
    binding.sharpnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) onSharpnessChanged(progress / 100f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    })

    val status = when {
        uiState.error != null -> uiState.error
        uiState.savedPath != null -> "Enhanced image saved to History"
        uiState.isEnhancing -> "Enhancing image..."
        uiState.enhancedBitmap != null -> "Enhanced preview ready"
        uiState.originalBitmap != null -> "Image ready for enhancement"
        else -> null
    }
    binding.statusText.text = status.orEmpty()
    binding.statusText.setTextColor(if (uiState.error != null) 0xFFFF6B6B.toInt() else context.getColor(R.color.lumora_lime))
    binding.statusText.visibility = if (status == null) View.GONE else View.VISIBLE

    binding.enhanceButton.isEnabled = uiState.originalBitmap != null && !uiState.isEnhancing
    binding.enhanceButton.alpha = if (binding.enhanceButton.isEnabled) 1f else 0.42f
    binding.enhanceButtonText.text = if (uiState.isEnhancing) "Enhancing..." else "Enhance Now"
    binding.enhanceProgress.visibility = if (uiState.isEnhancing) View.VISIBLE else View.GONE
    binding.enhanceButtonIconHost.visibility = if (uiState.isEnhancing) View.GONE else View.VISIBLE
    binding.enhanceButton.setOnClickListener {
        if (binding.enhanceButton.isEnabled) onEnhance()
    }
}

private fun bindComparisonResult(
    binding: PhotoEnhanceScreenBinding,
    uiState: PhotoEnhanceUiState,
    onOpenViewer: (String) -> Unit,
) {
    val original = uiState.originalBitmap
    val enhanced = uiState.enhancedBitmap
    val hasResult = original != null && enhanced != null
    binding.resultComparisonPanel.visibility = if (hasResult) View.VISIBLE else View.GONE
    if (!hasResult) return

    binding.originalResultImage.setImageBitmap(original)
    binding.enhancedResultImage.setImageBitmap(enhanced)
    binding.comparisonImageFrame.setOnClickListener {
        uiState.savedPath?.let(onOpenViewer)
    }

    fun updateComparison(progress: Int) {
        val frameWidth = binding.comparisonImageFrame.width
        val frameHeight = binding.comparisonImageFrame.height
        if (frameWidth <= 0 || frameHeight <= 0) return
        val revealWidth = (frameWidth * (progress / 100f)).toInt().coerceIn(0, frameWidth)
        binding.originalResultImage.clipBounds = Rect(0, 0, revealWidth, frameHeight)
        binding.comparisonDivider.translationX = revealWidth.toFloat()
        binding.comparisonImageFrame.tag = progress
        updateComparisonLabels(binding, progress)
    }

    binding.comparisonImageFrame.post {
        updateComparison((binding.comparisonImageFrame.tag as? Int) ?: 50)
    }
    var downX = 0f
    var dragged = false
    binding.comparisonImageFrame.setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                dragged = false
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val width = view.width.takeIf { it > 0 } ?: return@setOnTouchListener true
                dragged = dragged || abs(event.x - downX) > 8f
                val progress = ((event.x.coerceIn(0f, width.toFloat()) / width) * 100f).toInt()
                updateComparison(progress)
                true
            }
            MotionEvent.ACTION_UP -> {
                if (dragged) {
                    val width = view.width.takeIf { it > 0 } ?: return@setOnTouchListener true
                    val progress = ((event.x.coerceIn(0f, width.toFloat()) / width) * 100f).toInt()
                    updateComparison(progress)
                } else {
                    view.performClick()
                }
                true
            }
            else -> false
        }
    }
}

private fun updateComparisonLabels(
    binding: PhotoEnhanceScreenBinding,
    progress: Int,
) {
    val lime = binding.root.context.getColor(R.color.lumora_lime)
    binding.originalLabel.setTextColor(if (progress > 50) lime else Color.WHITE)
    binding.enhancedLabel.setTextColor(if (progress < 50) lime else Color.WHITE)
}

@Composable
private fun EnhancedResultViewerDialog(
    filePath: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .background(ComposeColor(0xFF111827))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        scope.launch {
                            val result = MediaGallerySaver.saveToGallery(
                                context = context,
                                filePath = filePath,
                                mimeType = "image/jpeg",
                                mediaType = "IMAGE"
                            )
                            val message = result.getOrElse { "Download failed" }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(TablerIcons.Download, contentDescription = stringResource(R.string.ui_download), tint = ComposeColor(0xFFD4FF3B))
                }
                IconButton(
                    onClick = { MediaShareUtils.shareImage(context, filePath) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(TablerIcons.Share, contentDescription = stringResource(R.string.ui_share), tint = ComposeColor(0xFFD4FF3B))
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(TablerIcons.X, contentDescription = stringResource(R.string.ui_close), tint = ComposeColor.White)
                }
            }

            ZoomableImageViewer(
                filePath = filePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                showControls = true,
                controlsBackgroundColor = ComposeColor.Black.copy(alpha = 0.68f),
                enableGestureDetection = true,
            )
        }
    }
}

private fun bindTablerIcon(
    host: ComposeView,
    imageVector: ImageVector,
    tint: ComposeColor,
) {
    host.setContent {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun bindOptionRow(
    selected: EnhanceOption,
    options: List<TextView>,
    onSelected: (EnhanceOption) -> Unit,
) {
    val values = EnhanceOption.entries
    options.forEachIndexed { index, view ->
        val option = values[index]
        val isSelected = option == selected
        view.setBackgroundResource(if (isSelected) R.drawable.bg_photo_segment_selected else android.R.color.transparent)
        view.setTextColor(if (isSelected) Color.BLACK else Color.WHITE)
        view.setOnClickListener { onSelected(option) }
    }
}
