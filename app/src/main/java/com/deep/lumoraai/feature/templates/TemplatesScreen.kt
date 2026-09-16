package com.deep.lumoraai.feature.templates

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.avatarRoute
import com.deep.lumoraai.core.navigation.imageToImageRoute
import com.deep.lumoraai.core.navigation.imageToVideoRoute
import com.deep.lumoraai.core.navigation.logoRoute
import com.deep.lumoraai.core.navigation.promoVideoRoute
import com.deep.lumoraai.core.navigation.templateSectionRoute
import com.deep.lumoraai.core.navigation.textToImageRoute
import com.deep.lumoraai.core.navigation.textToVideoRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.databinding.TemplateSectionScreenBinding
import com.deep.lumoraai.databinding.TemplatesScreenBinding
import com.deep.lumoraai.feature.templates.model.TemplateAction
import com.deep.lumoraai.feature.templates.model.TemplateCategory
import com.deep.lumoraai.feature.templates.model.TemplateListItem

private const val Background = 0xFF081020

@Composable
fun TemplatesScreen(
    uiState: TemplatesUiState,
    onRetry: () -> Unit = {},
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    unreadCount: Int = 0,
    selectedCategoryId: String = TemplateCategory.IMAGE.id,
    onCategorySelected: (String) -> Unit = {},
    scrollMemory: TemplateScrollMemory = TemplateScrollMemory(),
    modifier: Modifier = Modifier,
) {
    val nativeTemplateInterval = LocalAdsConfigStore.current?.current?.nativeTemplateInterval ?: 3
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComposeColor(Background),
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = Screen.Templates.route,
                onSelected = onNavigate,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor(Background))
                .padding(padding)
        ) {
            if (uiState is TemplatesUiState.Error) {
                androidx.compose.material3.TextButton(onClick = onRetry) { androidx.compose.material3.Text("Try again") }
            }
            AndroidView(
                factory = { TemplatesScreenBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindTemplates(
                        binding = TemplatesScreenBinding.bind(root),
                        uiState = uiState,
                        selectedCategoryId = selectedCategoryId,
                        unreadCount = unreadCount,
                        nativeTemplateInterval = nativeTemplateInterval,
                        scrollMemory = scrollMemory,
                        onCategorySelected = onCategorySelected,
                        onNavigate = onNavigate,
                        onViewAll = { categoryId, sectionId ->
                            onNavigate(templateSectionRoute(categoryId, sectionId))
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

private fun bindTemplates(
    binding: TemplatesScreenBinding,
    uiState: TemplatesUiState,
    selectedCategoryId: String,
    unreadCount: Int,
    nativeTemplateInterval: Int,
    scrollMemory: TemplateScrollMemory,
    onCategorySelected: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onViewAll: (String, String) -> Unit,
) {
    binding.loading.visibility = View.GONE
    binding.messageState.visibility = View.GONE
    binding.content.visibility = View.GONE

    when (uiState) {
        TemplatesUiState.Loading -> showLoading(binding)
        TemplatesUiState.Empty -> showMessage(binding, binding.root.context.getString(R.string.ui_template_empty))
        is TemplatesUiState.Error -> showMessage(binding, uiState.message)
        is TemplatesUiState.Success -> {
            val category = uiState.category(selectedCategoryId)
                ?: uiState.categories.firstOrNull()
            if (category == null) {
                showMessage(binding, binding.root.context.getString(R.string.ui_template_empty))
                return
            }
            if (category.sections.isEmpty()) {
                showMessage(binding, binding.root.context.getString(R.string.ui_template_empty))
                return
            }
            showContent(binding)
            bindHeader(binding, uiState.credits, unreadCount, onNavigate)

            val tabs = mapOf(
                TemplateCategory.IMAGE.id to binding.imagesTab,
                TemplateCategory.VIDEO.id to binding.videoTab,
                TemplateCategory.PROMO_VIDEO.id to binding.promoVideoTab,
                TemplateCategory.AVATAR.id to binding.avatarTab,
            )
            tabs.forEach { (categoryId, tab) ->
                val selected = categoryId == category.id
                tab.isSelected = selected
                tab.setBackgroundResource(
                    if (selected) R.drawable.bg_template_tab_selected else R.drawable.bg_template_tab
                )
                tab.setTextColor(
                    binding.root.context.getColor(
                        if (selected) R.color.lumora_lime else R.color.lumora_text_muted
                    )
                )
                ViewCompat.setStateDescription(tab, if (selected) "Selected" else "Not selected")
                tab.setOnClickListener {
                    saveMainScroll(binding.sectionsList, scrollMemory)
                    onCategorySelected(categoryId)
                }
            }

            val manager = (binding.sectionsList.layoutManager as? LinearLayoutManager)
                ?: LinearLayoutManager(binding.root.context).also {
                    binding.sectionsList.layoutManager = it
                }
            val currentAdapter = binding.sectionsList.adapter as? TemplateSectionsAdapter
            if (currentAdapter?.categoryId != category.id || currentAdapter.sections != category.sections) {
                if (currentAdapter != null) {
                    scrollMemory.saveCategoryPosition(
                        currentAdapter.categoryId,
                        manager.findFirstVisibleItemPosition().coerceAtLeast(0)
                    )
                }
                binding.sectionsList.adapter = TemplateSectionsAdapter(
                    categoryId = category.id,
                    sections = category.sections,
                    adRowInterval = nativeTemplateInterval,
                    scrollMemory = scrollMemory,
                    onTemplateClick = { navigateTemplate(it, onNavigate) },
                    onViewAll = { onViewAll(category.id, it.id) },
                )
                manager.scrollToPositionWithOffset(scrollMemory.categoryPosition(category.id), 0)
            }
            binding.sectionsList.clearOnScrollListeners()
            binding.sectionsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    scrollMemory.saveCategoryPosition(
                        category.id,
                        manager.findFirstVisibleItemPosition().coerceAtLeast(0)
                    )
                }
            })
        }
    }
}

private fun bindHeader(
    binding: TemplatesScreenBinding,
    credits: Int,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
) {
    val context = binding.root.context
    binding.creditsChip.text = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        credits.toString()
    }
    binding.creditsChip.contentDescription = context.getString(R.string.ui_credits_count_format, credits)
    binding.creditsChip.compoundDrawableTintList = ColorStateList.valueOf(context.getColor(R.color.lumora_lime))
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.unreadDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
}

private fun saveMainScroll(list: RecyclerView, memory: TemplateScrollMemory) {
    val adapter = list.adapter as? TemplateSectionsAdapter ?: return
    val manager = list.layoutManager as? LinearLayoutManager ?: return
    memory.saveCategoryPosition(
        adapter.categoryId,
        manager.findFirstVisibleItemPosition().coerceAtLeast(0)
    )
}

private fun showMessage(binding: TemplatesScreenBinding, message: String) {
    binding.messageState.text = message
    binding.messageState.visibility = View.VISIBLE
}

private fun showLoading(binding: TemplatesScreenBinding) {
    binding.content.animate().cancel()
    // Preserve loaded sections and their View All buttons while refreshing media.
    if (binding.sectionsList.adapter == null) {
        binding.content.alpha = 0f
        binding.content.visibility = View.GONE
    }
    binding.messageState.visibility = View.GONE
    binding.loading.alpha = 1f
    binding.loading.visibility = if (binding.sectionsList.adapter == null) View.VISIBLE else View.GONE
}

private fun showContent(binding: TemplatesScreenBinding) {
    binding.loading.visibility = View.GONE
    binding.messageState.visibility = View.GONE
    if (binding.content.visibility != View.VISIBLE) {
        binding.content.alpha = 0f
        binding.content.visibility = View.VISIBLE
        binding.content.animate().alpha(1f).setDuration(180L).start()
    } else {
        binding.content.alpha = 1f
    }
}

@Composable
fun TemplateSectionScreen(
    uiState: TemplatesUiState,
    categoryId: String,
    sectionId: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val nativeTemplateInterval = LocalAdsConfigStore.current?.current?.nativeTemplateInterval ?: 3
    val spanCount = when {
        widthDp >= 840 -> 4
        widthDp >= 600 -> 3
        else -> 2
    }
    AndroidView(
        factory = { TemplateSectionScreenBinding.inflate(LayoutInflater.from(it)).root },
        update = { root ->
            bindTemplateSection(
                binding = TemplateSectionScreenBinding.bind(root),
                uiState = uiState,
                categoryId = categoryId,
                sectionId = sectionId,
                onRetry = onRetry,
                spanCount = spanCount,
                nativeTemplateInterval = nativeTemplateInterval,
                onBack = onBack,
                onNavigate = onNavigate,
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(ComposeColor(Background))
            .statusBarsPadding()
            .navigationBarsPadding()
    )
}

private fun bindTemplateSection(
    binding: TemplateSectionScreenBinding,
    uiState: TemplatesUiState,
    categoryId: String,
    sectionId: String,
    onRetry: () -> Unit,
    spanCount: Int,
    nativeTemplateInterval: Int,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    binding.backButton.setOnClickListener { onBack() }
    if (uiState is TemplatesUiState.Loading) {
        // Keep the opened View All grid and its scroll position during refresh.
        binding.retryButton.visibility = View.GONE
        binding.messageState.visibility = View.GONE
        binding.loading.visibility = if (binding.grid.tag == null) View.VISIBLE else View.GONE
        if (binding.grid.tag == null) {
            binding.grid.visibility = View.GONE
            binding.title.text = binding.root.context.getString(R.string.ui_templates)
        }
        return
    }
    val section = (uiState as? TemplatesUiState.Success)?.section(categoryId, sectionId)
    binding.retryButton.visibility = if (uiState is TemplatesUiState.Error) View.VISIBLE else View.GONE
    binding.retryButton.setOnClickListener { onRetry() }
    if (section == null || section.templates.isEmpty()) {
        binding.loading.visibility = View.GONE
        binding.title.text = binding.root.context.getString(R.string.ui_templates)
        binding.grid.visibility = View.GONE
        binding.grid.tag = null
        binding.messageState.text = if (uiState is TemplatesUiState.Error) uiState.message else binding.root.context.getString(R.string.ui_template_section_empty)
        binding.messageState.visibility = if (uiState == TemplatesUiState.Loading) View.GONE else View.VISIBLE
        return
    }
    binding.title.text = section.title
    binding.messageState.visibility = View.GONE
    val contentKey = listOf(categoryId, sectionId, section.templates, spanCount, nativeTemplateInterval)
    if (binding.grid.tag == contentKey) {
        binding.loading.visibility = if (binding.grid.alpha == 0f) View.VISIBLE else View.GONE
        return
    }
    binding.grid.tag = contentKey
    binding.grid.visibility = View.VISIBLE
    binding.grid.alpha = 0f
    binding.loading.visibility = View.VISIBLE
    val currentManager = binding.grid.layoutManager as? GridLayoutManager
    if (currentManager?.spanCount != spanCount) {
        binding.grid.layoutManager = GridLayoutManager(binding.root.context, spanCount)
        while (binding.grid.itemDecorationCount > 0) {
            binding.grid.removeItemDecorationAt(0)
        }
        binding.grid.addItemDecoration(
            GridSpacingDecoration(
                spanCount = spanCount,
                spacing = (12 * binding.root.resources.displayMetrics.density).toInt()
            )
        )
    }
    val adapter = TemplateCardAdapter(
        templates = section.templates,
        horizontal = false,
        spanCount = spanCount,
        adRowInterval = nativeTemplateInterval,
        onTemplateClick = { navigateTemplate(it, onNavigate) },
    )
    binding.grid.adapter = adapter
    binding.grid.doOnPreDraw {
        if (binding.grid.tag == contentKey) {
            binding.loading.visibility = View.GONE
            binding.grid.alpha = 1f
        }
    }
    // Ad rows must span all columns — configure this after setting the adapter
    // so the layout manager can query getItemViewType() on demand.
    (binding.grid.layoutManager as? GridLayoutManager)?.spanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == TemplateCardAdapter.VIEW_TYPE_NATIVE_AD) {
                    spanCount
                } else {
                    1
                }
            }
        }
}

private fun navigateTemplate(item: TemplateListItem, onNavigate: (String) -> Unit) {
    val route = when (item.action) {
        TemplateAction.TEXT_TO_IMAGE -> textToImageRoute(item.prompt)
        TemplateAction.TEXT_TO_VIDEO -> textToVideoRoute(item.prompt)
        TemplateAction.IMAGE_TO_IMAGE -> imageToImageRoute(item.prompt)
        TemplateAction.IMAGE_TO_VIDEO -> imageToVideoRoute(item.prompt)
        TemplateAction.PROMO_VIDEO -> promoVideoRoute(item.prompt)
        TemplateAction.LOGO_CREATION -> logoRoute(item.prompt)
        TemplateAction.CREATE_AVATAR -> avatarRoute(item.prompt)
    }
    onNavigate(route)
}
