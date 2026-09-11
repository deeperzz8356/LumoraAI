package com.deep.lumoraai.feature.generation

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementBanner
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.CreditBalanceStore
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.databinding.GenerationRatioItemBinding
import com.deep.lumoraai.databinding.GenerationResultItemBinding
import com.deep.lumoraai.databinding.GenerationScreenBinding
import com.deep.lumoraai.databinding.GenerationSourceItemBinding
import com.deep.lumoraai.databinding.GenerationStyleItemBinding
import com.deep.lumoraai.databinding.ProfileMediaViewerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

data class NativeGenerationSource(
    val id: String,
    val bitmap: Bitmap,
)

data class NativeGenerationConfig(
    val title: String,
    val promptHint: String,
    val promptOptional: Boolean,
    val showPromptSection: Boolean = true,
    val showSingleUpload: Boolean,
    val singleUploadBitmap: Bitmap?,
    val onSingleUpload: (() -> Unit)?,
    val multiSources: List<NativeGenerationSource> = emptyList(),
    val maxSources: Int = 0,
    val isSourceBusy: Boolean = false,
    val onAddSources: (() -> Unit)? = null,
    val onRemoveSource: ((String) -> Unit)? = null,
    val prompt: String,
    val negativePrompt: String,
    val isImprovingPrompt: Boolean,
    val selectedAspectRatio: GenerationAspectRatio,
    val aspectRatioOptions: List<GenerationAspectRatio>,
    val sliderLabel: String?,
    val sliderValue: Float?,
    val onSliderChanged: ((Float) -> Unit)?,
    val duration: Int?,
    val onDurationChanged: ((Int) -> Unit)?,
    val styleItems: List<StyleItem>,
    val isGenerating: Boolean,
    val generationProgress: Float?,
    val generationStatusText: String?,
    val generatedPath: String?,
    val generatedPaths: List<String>,
    val generatedMimeType: String,
    val mediaType: String,
    val generateEnabled: Boolean,
    val creditCost: Int,
    val bannerPlacement: AdPlacement,
    val showRatio: Boolean = true,
    val generateButtonText: String? = null,
)

@Composable
fun NativeGenerationScreen(
    config: NativeGenerationConfig,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    var showPrompt by remember(config.promptOptional) { mutableStateOf(!config.promptOptional || config.prompt.isNotBlank()) }
    var selectorsOpen by remember { mutableStateOf(false) }
    val credits by CreditBalanceStore.balance.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF081020),
        contentWindowInsets = WindowInsets(0.dp),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081020))
                .systemBarsPadding()
                .padding(padding)
        ) {
            AndroidView(
                factory = { GenerationScreenBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindGeneration(
                        binding = GenerationScreenBinding.bind(root),
                        config = config,
                        credits = credits ?: 0,
                        showPrompt = showPrompt,
                        selectorsOpen = selectorsOpen,
                        scope = scope,
                        onBack = onBack,
                        onNavigate = onNavigate,
                        onPromptChanged = onPromptChanged,
                        onNegativePromptChanged = onNegativePromptChanged,
                        onAspectRatioChanged = onAspectRatioChanged,
                        onImprovePrompt = onImprovePrompt,
                        onGenerate = onGenerate,
                        onEditResult = onEditResult,
                        onDismissError = onDismissError,
                        onShowPromptChanged = { showPrompt = it },
                        onSelectorsOpenChanged = { selectorsOpen = it },
                        error = error,
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            PlacementBanner(
                placement = config.bannerPlacement,
                modifier = Modifier.padding(horizontal = 0.dp),
                applyNavBarPadding = false,
            )
        }
    }
}

private fun bindGeneration(
    binding: GenerationScreenBinding,
    config: NativeGenerationConfig,
    credits: Int,
    showPrompt: Boolean,
    selectorsOpen: Boolean,
    scope: CoroutineScope,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onImprovePrompt: () -> Unit,
    onGenerate: () -> Unit,
    onEditResult: () -> Unit,
    onDismissError: () -> Unit,
    onShowPromptChanged: (Boolean) -> Unit,
    onSelectorsOpenChanged: (Boolean) -> Unit,
    error: String?,
) {
    val context = binding.root.context
    binding.title.text = config.title
    binding.backButton.setOnClickListener { onBack() }
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
    binding.creditsChip.text = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else credits.toString()
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }

    bindSingleUpload(binding, config)
    bindMultiSources(binding, config)
    bindPrompt(binding, config, showPrompt, onPromptChanged, onImprovePrompt, onShowPromptChanged)
    bindLoading(binding, config)
    bindResult(binding, config, scope, onEditResult)
    bindError(binding, error, onDismissError)
    bindBottomBar(binding, config, selectorsOpen, onAspectRatioChanged, onGenerate, onSelectorsOpenChanged)
    if (config.isGenerating || config.generatedPaths.isNotEmpty() || config.generatedPath != null) {
        binding.contentScroll.post { binding.contentScroll.smoothScrollTo(0, binding.contentScroll.getChildAt(0).bottom) }
    }
}

private fun bindSingleUpload(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    binding.singleUploadPanel.visibility = if (config.showSingleUpload) View.VISIBLE else View.GONE
    if (!config.showSingleUpload) return
    binding.singleUploadPanel.clipToOutline = true
    binding.uploadImage.clipToOutline = true
    binding.singleUploadPanel.post {
        val maxSquare = dp(binding.root, 250)
        val minSquare = dp(binding.root, 176)
        val available = binding.content.width.takeIf { it > 0 }
            ?: binding.root.width.takeIf { it > 0 }
            ?: binding.root.resources.displayMetrics.widthPixels
        val size = (available - dp(binding.root, 48)).coerceIn(minSquare, maxSquare)
        val params = binding.singleUploadPanel.layoutParams as? LinearLayout.LayoutParams
        if (params != null && (params.width != size || params.height != size)) {
            params.width = size
            params.height = size
            params.gravity = android.view.Gravity.CENTER_HORIZONTAL
            binding.singleUploadPanel.layoutParams = params
        }
    }
    binding.singleUploadPanel.isEnabled = !config.isGenerating
    binding.singleUploadPanel.setOnClickListener { if (!config.isGenerating) config.onSingleUpload?.invoke() }
    val bitmap = config.singleUploadBitmap
    binding.uploadImage.visibility = if (bitmap != null) View.VISIBLE else View.GONE
    binding.uploadEmpty.visibility = if (bitmap == null) View.VISIBLE else View.GONE
    if (bitmap != null) binding.uploadImage.setImageBitmap(bitmap)
}

private fun bindMultiSources(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    val show = config.maxSources > 0
    binding.multiSourcePanel.visibility = if (show) View.VISIBLE else View.GONE
    if (!show) return
    binding.sourceCount.text = "${config.multiSources.size} / ${config.maxSources}"
    binding.sourceHint.visibility = if (config.multiSources.isEmpty()) View.VISIBLE else View.GONE
    binding.sourceHint.gravity = if (config.multiSources.isEmpty()) android.view.Gravity.CENTER else android.view.Gravity.START
    binding.sourceRow.removeAllViews()
    binding.sourceRow.gravity = if (config.multiSources.isEmpty()) {
        android.view.Gravity.CENTER_HORIZONTAL
    } else {
        android.view.Gravity.NO_GRAVITY
    }
    config.multiSources.forEach { source ->
        val item = GenerationSourceItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.sourceRow, false)
        item.root.clipToOutline = true
        item.sourceImage.clipToOutline = true
        item.sourceImage.setImageBitmap(source.bitmap)
        item.removeButton.isEnabled = !config.isSourceBusy
        item.removeButton.setOnClickListener { if (!config.isSourceBusy) config.onRemoveSource?.invoke(source.id) }
        binding.sourceRow.addView(item.root, squareRowParams(binding.root, 112, 10))
    }
    if (config.multiSources.size < config.maxSources) {
        val context = binding.root.context
        val addSize = if (config.multiSources.isEmpty()) 250 else 112
        val add = FrameLayout(context).apply {
            clipToOutline = true
            setBackgroundResource(R.drawable.bg_generation_upload)
            setOnClickListener { if (!config.isSourceBusy) config.onAddSources?.invoke() }
            addView(LinearLayout(context).apply {
                gravity = android.view.Gravity.CENTER
                orientation = LinearLayout.VERTICAL
                addView(ImageView(context).apply {
                    setImageResource(R.drawable.ic_lumora_upload)
                    setColorFilter(0xFFD6FF2F.toInt())
                    setBackgroundResource(R.drawable.bg_common_icon_circle)
                    setPadding(dp(this, 14), dp(this, 14), dp(this, 14), dp(this, 14))
                }, LinearLayout.LayoutParams(dp(this, 54), dp(this, 54)))
                addView(TextView(context).apply {
                    text = context.getString(R.string.ui_upload_image)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = if (config.multiSources.isEmpty()) 16f else 12f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    setPadding(dp(this, 6), dp(this, 12), dp(this, 6), 0)
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        binding.sourceRow.addView(add, squareRowParams(binding.root, addSize, 10))
    }
}

private fun bindPrompt(
    binding: GenerationScreenBinding,
    config: NativeGenerationConfig,
    showPrompt: Boolean,
    onPromptChanged: (String) -> Unit,
    onImprovePrompt: () -> Unit,
    onShowPromptChanged: (Boolean) -> Unit,
) {
    binding.promptWrapper.visibility = if (config.showPromptSection) View.VISIBLE else View.GONE
    if (!config.showPromptSection) return
    binding.promptTitle.text = if (config.promptOptional && !showPrompt) {
        binding.root.context.getString(R.string.ui_prompt_optional)
    } else {
        binding.root.context.getString(R.string.ui_prompt)
    }
    binding.promptHeader.setOnClickListener { if (config.promptOptional) onShowPromptChanged(!showPrompt) }
    binding.promptCard.visibility = if (showPrompt) View.VISIBLE else View.GONE
    binding.improveButton.visibility = if (showPrompt) View.VISIBLE else View.GONE
    binding.promptInput.hint = config.promptHint
    val oldWatcher = binding.promptInput.getTag(R.id.promptInput) as? TextWatcher
    if (oldWatcher != null) binding.promptInput.removeTextChangedListener(oldWatcher)
    if (binding.promptInput.text.toString() != config.prompt) binding.promptInput.setText(config.prompt)
    binding.promptCount.text = "${config.prompt.length}/1000"
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val next = s?.toString().orEmpty().take(1000)
            if (next != config.prompt) onPromptChanged(next)
        }
        override fun afterTextChanged(s: Editable?) = Unit
    }
    binding.promptInput.addTextChangedListener(watcher)
    binding.promptInput.setTag(R.id.promptInput, watcher)
    binding.improveButton.alpha = if (config.prompt.isNotBlank() && !config.isImprovingPrompt) 1f else 0.45f
    binding.improveButton.text = if (config.isImprovingPrompt) binding.root.context.getString(R.string.loading) else binding.root.context.getString(R.string.ui_ai_prompt_enhancer)
    binding.improveButton.setOnClickListener { if (config.prompt.isNotBlank() && !config.isImprovingPrompt) onImprovePrompt() }
}

private fun bindLoading(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    binding.loadingPanel.visibility = if (config.isGenerating) View.VISIBLE else View.GONE
    if (!config.isGenerating) return
    val progress = config.generationProgress?.coerceIn(0f, 1f)
    binding.loadingTitle.text = config.generationStatusText ?: binding.root.context.getString(R.string.loading)
    binding.loadingSubtitle.text = progress?.let { "${(it * 100).toInt()}% completed" } ?: binding.root.context.getString(R.string.ui_queue)
    binding.loadingProgress.isIndeterminate = progress == null
    if (progress != null) binding.loadingProgress.progress = (progress * 100).toInt()
}

private fun bindResult(
    binding: GenerationScreenBinding,
    config: NativeGenerationConfig,
    scope: CoroutineScope,
    onEditResult: () -> Unit,
) {
    val paths = (config.generatedPaths.ifEmpty { config.generatedPath?.let(::listOf).orEmpty() }).distinct()
    binding.resultPanel.visibility = if (paths.isEmpty()) View.GONE else View.VISIBLE
    if (paths.isEmpty()) return
    val isVideo = isVideo(config.mediaType, config.generatedMimeType)
    binding.resultTitle.text = if (paths.size > 1) "${paths.size} Results Ready" else if (isVideo) "Video Ready" else "Image Ready"
    binding.resultRow.removeAllViews()
    paths.forEach { path ->
        val item = GenerationResultItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.resultRow, false)
        bindMediaThumb(item.resultImage, path, isVideo, if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy)
        item.playBadge.visibility = if (isVideo) View.VISIBLE else View.GONE
        item.root.setOnClickListener { showMediaViewer(binding.root, path, config.mediaType, config.generatedMimeType) }
        binding.resultRow.addView(item.root)
    }
    val selectedPath = paths.last()
    binding.editResultButton.setOnClickListener { onEditResult() }
    binding.downloadResultButton.setOnClickListener {
        scope.launch {
            val result = MediaGallerySaver.saveToGallery(binding.root.context, selectedPath, config.generatedMimeType, config.mediaType)
            Toast.makeText(binding.root.context, result.getOrElse { it.message ?: "Could not download media." }, Toast.LENGTH_SHORT).show()
        }
    }
    binding.shareResultButton.setOnClickListener {
        MediaShareUtils.shareMedia(binding.root.context, selectedPath, config.generatedMimeType)
    }
}

private fun bindError(binding: GenerationScreenBinding, error: String?, onDismissError: () -> Unit) {
    binding.errorText.visibility = if (error == null) View.GONE else View.VISIBLE
    binding.errorText.text = error.orEmpty()
    binding.errorText.setOnClickListener { onDismissError() }
}

private fun bindBottomBar(
    binding: GenerationScreenBinding,
    config: NativeGenerationConfig,
    selectorsOpen: Boolean,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
    onGenerate: () -> Unit,
    onSelectorsOpenChanged: (Boolean) -> Unit,
) {
    val hasStyles = config.styleItems.isNotEmpty()
    val hasSelectors = hasStyles || config.showRatio
    binding.summaryRow.visibility = if (hasSelectors) View.VISIBLE else View.GONE
    binding.selectorPanel.visibility = if (selectorsOpen && hasSelectors) View.VISIBLE else View.GONE
    val selectedStyle = config.styleItems.firstOrNull { it.selected } ?: config.styleItems.firstOrNull()
    binding.summaryRow.text = buildString {
        if (selectedStyle != null) append(binding.root.context.getString(selectedStyle.labelRes))
        if (config.showRatio) {
            if (isNotEmpty()) append("  |  ")
            append(config.selectedAspectRatio.label)
        }
        append(if (selectorsOpen) "  Close" else "  Options")
    }
    binding.summaryRow.setOnClickListener { onSelectorsOpenChanged(!selectorsOpen) }
    bindStyles(binding, config)
    bindRatios(binding, config, onAspectRatioChanged)
    binding.generateButton.isEnabled = config.generateEnabled && !config.isGenerating
    binding.generateButton.alpha = if (binding.generateButton.isEnabled) 1f else 0.45f
    binding.generateButton.text = if (config.isGenerating) {
        binding.root.context.getString(R.string.loading)
    } else {
        config.generateButtonText ?: binding.root.context.getString(R.string.ui_generate_now)
    }
    binding.generateButton.setOnClickListener { if (config.generateEnabled && !config.isGenerating) onGenerate() }
    binding.creditNote.text = binding.root.context.getString(R.string.ui_credits_consumed_note, config.creditCost)
}

private fun bindStyles(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    binding.styleTitle.visibility = if (config.styleItems.isEmpty()) View.GONE else View.VISIBLE
    binding.styleScroll.visibility = if (config.styleItems.isEmpty()) View.GONE else View.VISIBLE
    binding.styleRow.removeAllViews()
    config.styleItems.forEach { style ->
        val item = GenerationStyleItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.styleRow, false)
        item.root.setBackgroundResource(if (style.selected) R.drawable.bg_generation_selected else R.drawable.bg_generation_unselected)
        item.styleLabel.text = binding.root.context.getString(style.labelRes)
        item.styleLabel.setTextColor(if (style.selected) 0xFFD6FF2F.toInt() else 0xFFFFFFFF.toInt())
        item.checkBadge.visibility = if (style.selected) View.VISIBLE else View.GONE
        item.styleImage.setImageDrawable(assetDrawable(binding.root, style.assetFileName))
        item.root.setOnClickListener { style.onClick() }
        binding.styleRow.addView(item.root, rowParams(binding.root, 96, 10))
    }
}

private fun bindRatios(
    binding: GenerationScreenBinding,
    config: NativeGenerationConfig,
    onAspectRatioChanged: (GenerationAspectRatio) -> Unit,
) {
    binding.ratioTitle.visibility = if (config.showRatio) View.VISIBLE else View.GONE
    binding.ratioScroll.visibility = if (config.showRatio) View.VISIBLE else View.GONE
    binding.ratioRow.removeAllViews()
    if (!config.showRatio) return
    config.aspectRatioOptions.forEach { ratio ->
        val item = GenerationRatioItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.ratioRow, false)
        item.root.text = ratio.label
        item.root.setTextColor(if (ratio == config.selectedAspectRatio) 0xFFD6FF2F.toInt() else 0xFFFFFFFF.toInt())
        item.root.setBackgroundResource(if (ratio == config.selectedAspectRatio) R.drawable.bg_generation_selected else R.drawable.bg_generation_unselected)
        item.root.setOnClickListener { onAspectRatioChanged(ratio) }
        binding.ratioRow.addView(item.root, rowParams(binding.root, 74, 8))
    }
}

private fun showMediaViewer(anchor: View, path: String, mediaType: String, mimeType: String) {
    val context = anchor.context
    val viewer = ProfileMediaViewerBinding.inflate(LayoutInflater.from(context))
    val file = File(path)
    val video = isVideo(mediaType, mimeType)
    viewer.mediaFrame.layoutParams = viewer.mediaFrame.layoutParams.apply {
        height = (context.resources.displayMetrics.heightPixels * 0.62f).toInt().coerceAtMost(dp(anchor, 420))
    }
    viewer.mediaTitle.text = file.name.ifBlank { if (video) "Video" else "Image" }
    viewer.missingText.visibility = if (file.exists()) View.GONE else View.VISIBLE
    viewer.mediaImage.visibility = if (file.exists() && !video) View.VISIBLE else View.GONE
    viewer.mediaVideo.visibility = if (file.exists() && video) View.VISIBLE else View.GONE
    if (file.exists() && video) {
        viewer.mediaVideo.setVideoURI(Uri.fromFile(file))
        viewer.mediaVideo.setOnPreparedListener {
            it.isLooping = true
            viewer.mediaVideo.start()
        }
    } else if (file.exists()) {
        viewer.mediaImage.setImageURI(Uri.fromFile(file))
    }
    val dialog = AlertDialog.Builder(context)
        .setView(viewer.root)
        .setNegativeButton(R.string.ui_cancel, null)
        .show()
    dialog.setOnDismissListener { viewer.mediaVideo.stopPlayback() }
}

private fun bindMediaThumb(image: android.widget.ImageView, path: String, isVideo: Boolean, fallbackRes: Int) {
    val file = File(path)
    when {
        path.isBlank() || !file.exists() -> image.setImageResource(fallbackRes)
        isVideo -> videoFrame(file)?.let { image.setImageBitmap(it) } ?: image.setImageResource(fallbackRes)
        else -> image.setImageURI(Uri.fromFile(file))
    }
}

private fun rowParams(view: View, widthDp: Int, endMarginDp: Int): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(dp(view, widthDp), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        marginEnd = dp(view, endMarginDp)
    }

private fun squareRowParams(view: View, sizeDp: Int, endMarginDp: Int): LinearLayout.LayoutParams =
    LinearLayout.LayoutParams(dp(view, sizeDp), dp(view, sizeDp)).apply {
        marginEnd = dp(view, endMarginDp)
    }

private fun dp(view: View, value: Int): Int = (value * view.resources.displayMetrics.density).toInt()

private fun isVideo(mediaType: String, mimeType: String): Boolean =
    mediaType.equals("VIDEO", ignoreCase = true) || mimeType.startsWith("video/", ignoreCase = true)

private fun videoFrame(file: File): Bitmap? = runCatching {
    MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}.getOrNull()

private fun assetDrawable(view: View, fileName: String): Drawable? =
    runCatching {
        view.context.assets.open(fileName).use { Drawable.createFromStream(it, fileName) }
    }.getOrNull()
