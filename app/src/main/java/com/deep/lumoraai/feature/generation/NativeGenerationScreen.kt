package com.deep.lumoraai.feature.generation

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
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
import compose.icons.TablerIcons
import compose.icons.tablericons.AspectRatio
import compose.icons.tablericons.Bell
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Palette
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.SquarePlus
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
    LaunchedEffect(Unit) {
        CreditBalanceStore.refresh()
    }

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
                        credits = credits,
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
    credits: Int?,
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
    binding.creditsChip.text = when {
        credits == null -> ""
        credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY -> "Unlimited"
        else -> credits.toString()
    }
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.creditsChip.compoundDrawableTintList = ColorStateList.valueOf(binding.root.context.getColor(R.color.lumora_lime))
    bindTablerIcon(binding.notificationIconHost, TablerIcons.Bell, Color.White)

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
    binding.sourceHint.gravity = android.view.Gravity.START
    binding.sourceRow.removeAllViews()
    binding.sourceRow.gravity = android.view.Gravity.NO_GRAVITY
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
        val addSize = if (config.multiSources.isEmpty()) 106 else 112
        val addSourceClick = View.OnClickListener {
            if (!config.isSourceBusy) config.onAddSources?.invoke()
        }
        val add = FrameLayout(context).apply {
            clipToOutline = true
            setBackgroundResource(R.drawable.bg_generation_upload)
            isClickable = true
            isFocusable = true
            setOnClickListener(addSourceClick)
            addView(LinearLayout(context).apply {
                gravity = android.view.Gravity.CENTER
                orientation = LinearLayout.VERTICAL
                isClickable = true
                setOnClickListener(addSourceClick)
                addView(ComposeView(context).apply {
                    bindTablerIcon(this, TablerIcons.SquarePlus, Color(0xFFD6FF2F))
                    isClickable = false
                }, LinearLayout.LayoutParams(dp(this, 32), dp(this, 32)))
                addView(TextView(context).apply {
                    text = context.getString(R.string.ui_upload_image)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = if (config.multiSources.isEmpty()) 16f else 12f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    setPadding(dp(this, 4), dp(this, 14), dp(this, 4), 0)
                    isClickable = false
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
    val headerParams = binding.promptHeader.layoutParams
    if (config.promptOptional) {
        binding.promptHeader.visibility = View.VISIBLE
        headerParams.height = dp(binding.root, 50)
        binding.promptHeader.layoutParams = headerParams
        binding.promptHeader.setBackgroundResource(R.drawable.bg_generation_panel)
        binding.promptHeader.setPadding(dp(binding.root, 18), 0, dp(binding.root, 18), 0)
        binding.promptToggleIconHost.visibility = View.VISIBLE
        binding.promptChevronIconHost.visibility = View.VISIBLE
        binding.improveIconHost.visibility = View.GONE
        bindTablerIcon(binding.promptToggleIconHost, TablerIcons.Pencil, Color(0xFFD6FF2F))
        bindTablerIcon(binding.promptChevronIconHost, if (showPrompt) TablerIcons.ChevronUp else TablerIcons.ChevronDown, Color.White.copy(alpha = 0.75f))
        binding.promptTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        binding.promptTitle.text = binding.root.context.getString(R.string.ui_prompt_optional)
    } else {
        binding.promptHeader.visibility = View.GONE
        headerParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.promptHeader.layoutParams = headerParams
        binding.promptHeader.background = null
        binding.promptHeader.setPadding(0, 0, 0, 0)
        binding.promptToggleIconHost.visibility = View.GONE
        binding.promptChevronIconHost.visibility = View.GONE
        binding.improveIconHost.visibility = View.GONE
        binding.promptTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        binding.promptTitle.text = binding.root.context.getString(R.string.ui_prompt)
    }
    val togglePrompt = {
        if (config.promptOptional) onShowPromptChanged(!showPrompt)
    }
    val togglePromptClick = View.OnClickListener { togglePrompt() }
    binding.promptHeader.isClickable = config.promptOptional
    binding.promptHeader.isFocusable = config.promptOptional
    binding.promptHeader.setOnClickListener(togglePromptClick)
    binding.promptToggleIconHost.isClickable = true
    binding.promptToggleIconHost.setOnClickListener(togglePromptClick)
    binding.promptTitle.isClickable = config.promptOptional
    binding.promptTitle.setOnClickListener(togglePromptClick)
    binding.promptChevronIconHost.isClickable = true
    binding.promptChevronIconHost.setOnClickListener(togglePromptClick)
    binding.promptSubHeader.visibility = if (showPrompt) View.VISIBLE else View.GONE
    binding.promptCard.visibility = if (showPrompt) View.VISIBLE else View.GONE
    binding.improveButton.visibility = if (showPrompt) View.VISIBLE else View.GONE
    binding.improveIconHost.visibility = View.GONE
    binding.promptInput.minHeight = if (config.promptOptional) dp(binding.root, 96) else dp(binding.root, 190)
    binding.promptInput.hint = config.promptHint
    binding.promptInput.setTag(R.id.generation_prompt_change_callback, onPromptChanged)
    val currentPromptText = binding.promptInput.text.toString()
    val lastModelText = binding.promptInput.getTag(R.id.generation_prompt_model_text) as? String
    val shouldApplyModelText = currentPromptText != config.prompt &&
        (!binding.promptInput.hasFocus() || lastModelText != config.prompt)
    if (shouldApplyModelText) {
        binding.promptInput.setText(config.prompt)
        binding.promptInput.setSelection(binding.promptInput.text?.length ?: 0)
    }
    binding.promptInput.setTag(R.id.generation_prompt_model_text, config.prompt)
    binding.promptCount.text = "${config.prompt.length}/1000"
    val existingWatcher = binding.promptInput.getTag(R.id.generation_prompt_text_watcher) as? TextWatcher
    if (existingWatcher == null) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val next = s?.toString().orEmpty().take(1000)
                val modelText = binding.promptInput.getTag(R.id.generation_prompt_model_text) as? String
                if (next != modelText) {
                    @Suppress("UNCHECKED_CAST")
                    val callback = binding.promptInput.getTag(R.id.generation_prompt_change_callback) as? ((String) -> Unit)
                    callback?.invoke(next)
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 1000) {
                    s?.delete(1000, s.length)
                }
            }
        }
        binding.promptInput.addTextChangedListener(watcher)
        binding.promptInput.setTag(R.id.generation_prompt_text_watcher, watcher)
    }
    binding.improveButton.alpha = if (config.prompt.isNotBlank() && !config.isImprovingPrompt) 1f else 0.45f
    binding.improveButton.text = if (config.isImprovingPrompt) binding.root.context.getString(R.string.loading) else binding.root.context.getString(R.string.ui_ai_prompt_enhancer)
    val improveEnabled = config.prompt.isNotBlank() && !config.isImprovingPrompt
    binding.improveButton.setOnClickListener { if (improveEnabled) onImprovePrompt() }
    binding.improveIconHost.setOnClickListener(null)
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
    val hasSlider = config.hasVisibleSlider()
    val hasDuration = config.duration != null && config.onDurationChanged != null
    val hasSelectors = hasStyles || config.showRatio || hasSlider || hasDuration
    binding.summaryRow.visibility = if (hasSelectors) View.VISIBLE else View.GONE
    binding.selectorPanel.visibility = if (selectorsOpen && hasSelectors) View.VISIBLE else View.GONE
    val selectedStyle = config.styleItems.firstOrNull { it.selected } ?: config.styleItems.firstOrNull()
    val currentSliderValue = config.sliderValue
    val sliderSummary = if (selectedStyle == null && !config.showRatio && hasSlider && currentSliderValue != null) {
        "${config.sliderLabel} ${(currentSliderValue.coerceIn(0f, 1f) * 100).toInt()}%"
    } else {
        selectedStyle?.let { binding.root.context.getString(it.labelRes) }.orEmpty()
    }
    binding.summaryStyleText.text = sliderSummary
    binding.summaryRatioText.text = if (config.showRatio) config.selectedAspectRatio.label else ""
    binding.summaryStyleText.visibility = if (sliderSummary.isNotBlank()) View.VISIBLE else View.GONE
    binding.summaryStyleIconHost.visibility = if (sliderSummary.isNotBlank()) View.VISIBLE else View.GONE
    binding.summaryRatioText.visibility = if (config.showRatio) View.VISIBLE else View.GONE
    binding.summaryRatioIconHost.visibility = if (config.showRatio) View.VISIBLE else View.GONE
    binding.summaryRatioDivider.visibility = if (sliderSummary.isNotBlank() && config.showRatio) View.VISIBLE else View.GONE
    bindTablerIcon(binding.summaryStyleIconHost, TablerIcons.Palette, Color.White)
    bindTablerIcon(binding.summaryRatioIconHost, TablerIcons.AspectRatio, Color.White)
    bindTablerIcon(binding.summaryChevronIconHost, if (selectorsOpen) TablerIcons.ChevronUp else TablerIcons.ChevronDown, Color.White)
    val toggleSelectorsClick = View.OnClickListener { onSelectorsOpenChanged(!selectorsOpen) }
    binding.summaryRow.isClickable = hasSelectors
    binding.summaryRow.isFocusable = hasSelectors
    binding.summaryRow.setOnClickListener(toggleSelectorsClick)
    binding.summaryStyleIconHost.isClickable = false
    binding.summaryStyleText.isClickable = false
    binding.summaryRatioIconHost.isClickable = false
    binding.summaryRatioText.isClickable = false
    binding.summaryChevronIconHost.isClickable = false
    bindStyles(binding, config)
    bindRatios(binding, config, onAspectRatioChanged)
    bindSlider(binding, config)
    bindDuration(binding, config)
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

private fun bindSlider(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    val label = config.sliderLabel
    val value = config.sliderValue
    val onChanged = config.onSliderChanged
    val show = config.hasVisibleSlider() && label != null && value != null && onChanged != null
    binding.sliderSection.visibility = if (show) View.VISIBLE else View.GONE
    if (!show) return
    val currentValue = value.coerceIn(0f, 1f)
    val changeListener = onChanged
    val progress = (currentValue * 100).toInt()
    binding.sliderTitle.text = label
    binding.sliderValue.text = "$progress%"
    binding.valueSlider.setOnSeekBarChangeListener(null)
    if (binding.valueSlider.progress != progress) binding.valueSlider.progress = progress
    binding.valueSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!fromUser) return
            binding.sliderValue.text = "$progress%"
            changeListener(progress / 100f)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    })
}

private fun bindDuration(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    val duration = config.duration
    val onChanged = config.onDurationChanged
    val show = duration != null && onChanged != null
    binding.durationSection.visibility = if (show) View.VISIBLE else View.GONE
    if (!show) return
    val currentDuration = duration.coerceIn(5, 15)
    val changeListener = onChanged
    val seconds = currentDuration
    val progress = seconds - 5
    binding.durationValue.text = binding.root.context.getString(R.string.ui_seconds_format, seconds)
    binding.durationSlider.setOnSeekBarChangeListener(null)
    if (binding.durationSlider.progress != progress) binding.durationSlider.progress = progress
    binding.durationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!fromUser) return
            val next = (5 + progress).coerceIn(5, 15)
            binding.durationValue.text = binding.root.context.getString(R.string.ui_seconds_format, next)
            changeListener(next)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    })
}

private fun bindStyles(binding: GenerationScreenBinding, config: NativeGenerationConfig) {
    binding.styleTitle.visibility = if (config.styleItems.isEmpty()) View.GONE else View.VISIBLE
    binding.styleScroll.visibility = if (config.styleItems.isEmpty()) View.GONE else View.VISIBLE
    val signature = config.styleItems.joinToString("|") { "${it.labelRes}:${it.assetFileName}:${it.selected}" }
    if (binding.styleRow.getTag(R.id.generation_style_signature) == signature) return
    binding.styleRow.setTag(R.id.generation_style_signature, signature)
    binding.styleRow.removeAllViews()
    config.styleItems.forEach { style ->
        val item = GenerationStyleItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.styleRow, false)
        item.root.setBackgroundResource(if (style.selected) R.drawable.bg_generation_selected else R.drawable.bg_generation_unselected)
        item.styleLabel.text = binding.root.context.getString(style.labelRes)
        item.styleLabel.setTextColor(if (style.selected) 0xFFD6FF2F.toInt() else 0xFFFFFFFF.toInt())
        item.checkBadge.visibility = if (style.selected) View.VISIBLE else View.GONE
        item.styleImage.setImageDrawable(assetDrawable(binding.root, style.assetFileName))
        item.root.isClickable = true
        item.root.setOnClickListener { style.onClick() }
        item.styleImage.isClickable = false
        item.styleLabel.isClickable = false
        item.checkBadge.isClickable = false
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
    val signature = if (config.showRatio) {
        config.aspectRatioOptions.joinToString("|") { "${it.name}:${it == config.selectedAspectRatio}" }
    } else {
        "hidden"
    }
    if (binding.ratioRow.getTag(R.id.generation_ratio_signature) == signature) return
    binding.ratioRow.setTag(R.id.generation_ratio_signature, signature)
    binding.ratioRow.removeAllViews()
    if (!config.showRatio) return
    config.aspectRatioOptions.forEach { ratio ->
        val item = GenerationRatioItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.ratioRow, false)
        val selected = ratio == config.selectedAspectRatio
        item.ratioLabel.text = ratio.label
        item.ratioLabel.setTextColor(if (selected) 0xFFD6FF2F.toInt() else 0xFFFFFFFF.toInt())
        item.root.setBackgroundResource(if (ratio == config.selectedAspectRatio) R.drawable.bg_generation_selected else R.drawable.bg_generation_unselected)
        bindTablerIcon(item.ratioIconHost, TablerIcons.AspectRatio, if (selected) Color(0xFFD6FF2F) else Color.White.copy(alpha = 0.75f))
        item.root.isClickable = true
        item.root.setOnClickListener { onAspectRatioChanged(ratio) }
        item.ratioIconHost.isClickable = false
        item.ratioLabel.isClickable = false
        binding.ratioRow.addView(item.root, rowParams(binding.root, 84, 8))
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

private fun bindTablerIcon(
    host: ComposeView,
    imageVector: ImageVector,
    tint: Color,
) {
    val signature = "${imageVector.name}:${tint.value}"
    if (host.getTag(R.id.generation_icon_signature) == signature) return
    host.setTag(R.id.generation_icon_signature, signature)
    host.setContent {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun NativeGenerationConfig.hasVisibleSlider(): Boolean =
    sliderLabel != null &&
        sliderValue != null &&
        onSliderChanged != null &&
        !sliderLabel.equals("Creativity", ignoreCase = true)
