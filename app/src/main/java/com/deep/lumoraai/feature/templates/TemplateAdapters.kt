package com.deep.lumoraai.feature.templates

import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.databinding.TemplateCardBinding
import com.deep.lumoraai.databinding.TemplateSectionRowBinding
import com.deep.lumoraai.feature.templates.model.TemplateListItem
import com.deep.lumoraai.feature.templates.model.TemplateSection

// ---------------------------------------------------------------------------
// Sections list adapter — one row per section, each containing a horizontal
// carousel, with full-width native ad rows inserted between section rows.
// ---------------------------------------------------------------------------

internal class TemplateSectionsAdapter(
    val categoryId: String,
    val sections: List<TemplateSection>,
    private val adRowInterval: Int,
    private val scrollMemory: TemplateScrollMemory,
    private val onTemplateClick: (TemplateListItem) -> Unit,
    private val onViewAll: (TemplateSection) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed interface ListItem {
        data class Section(val section: TemplateSection) : ListItem
        data class NativeAd(val slotIndex: Int) : ListItem
    }

    private val items: List<ListItem> = buildList {
        var completedRows = 0
        var adSlotIndex = 0
        sections.forEachIndexed { index, section ->
            add(ListItem.Section(section))
            completedRows++
            if (completedRows % adRowInterval.coerceAtLeast(1) == 0) {
                add(ListItem.NativeAd(adSlotIndex++))
            }
        }
    }

    init { setHasStableIds(true) }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.Section -> VIEW_TYPE_SECTION
        is ListItem.NativeAd -> VIEW_TYPE_NATIVE_AD
    }

    override fun getItemId(position: Int): Long = when (val item = items[position]) {
        is ListItem.Section -> item.section.id.hashCode().toLong()
        is ListItem.NativeAd -> -(item.slotIndex.toLong() + 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_SECTION -> SectionViewHolder(
                TemplateSectionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_NATIVE_AD -> {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                NativeAdViewHolder(composeView)
            }
            else -> error("Unknown viewType $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> holder.bind((items[position] as ListItem.Section).section)
            is NativeAdViewHolder -> {
                val slot = (items[position] as ListItem.NativeAd).slotIndex
                holder.bind("template_main_ad_$slot")
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is SectionViewHolder) holder.savePosition()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = items.size

    private companion object {
        const val VIEW_TYPE_SECTION = 0
        const val VIEW_TYPE_NATIVE_AD = 1
    }

    inner class SectionViewHolder(
        private val binding: TemplateSectionRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var boundSection: TemplateSection? = null

        fun bind(section: TemplateSection) {
            boundSection = section
            binding.title.text = section.title
            binding.viewAll.contentDescription = binding.root.context.getString(
                R.string.ui_template_view_all_section,
                section.title
            )
            binding.viewAll.setOnClickListener { onViewAll(section) }

            val manager = (binding.cards.layoutManager as? LinearLayoutManager)
                ?: LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false).also {
                    binding.cards.layoutManager = it
                }
            if (binding.cards.itemDecorationCount == 0) {
                binding.cards.addItemDecoration(HorizontalSpacingDecoration(dp(binding.root, 12)))
            }
            // Horizontal carousel cards stay template-only; the parent list owns
            // the full-width native template ad rows after every three sections.
            binding.cards.adapter = TemplateCardAdapter(
                templates = section.templates,
                horizontal = true,
                onTemplateClick = onTemplateClick,
                spanCount = 0,
            )
            manager.scrollToPositionWithOffset(
                scrollMemory.sectionPosition(categoryId, section.id), 0
            )
            binding.cards.clearOnScrollListeners()
            binding.cards.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    scrollMemory.saveSectionPosition(
                        categoryId,
                        section.id,
                        manager.findFirstVisibleItemPosition().coerceAtLeast(0)
                    )
                }
            })
        }

        fun savePosition() {
            val section = boundSection ?: return
            val manager = binding.cards.layoutManager as? LinearLayoutManager ?: return
            scrollMemory.saveSectionPosition(
                categoryId,
                section.id,
                manager.findFirstVisibleItemPosition().coerceAtLeast(0)
            )
        }
    }

    inner class NativeAdViewHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {
        fun bind(slotKey: String) {
            composeView.setContent {
                PlacementNativeAd(
                    placement = AdPlacement.NATIVE_TEMPLATE,
                    modifier = Modifier.padding(vertical = 8.dp),
                    slotKey = slotKey,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Template card adapter — used for both horizontal carousels and the vertical
// grid on the section-detail screen.
//
// [spanCount] == 0  →  horizontal carousel mode: no ad injection.
// [spanCount] > 0   →  vertical grid mode: injects a full-width native ad row
//                       after every [adRowInterval] completed template rows.
//
// The caller must configure GridLayoutManager.SpanSizeLookup to give ad items
// maxLineSpan width.  See TemplatesScreen.bindTemplateSection().
// ---------------------------------------------------------------------------

internal class TemplateCardAdapter(
    private val templates: List<TemplateListItem>,
    private val horizontal: Boolean,
    private val onTemplateClick: (TemplateListItem) -> Unit,
    /** Grid column count.  0 = carousel mode (no ads). */
    val spanCount: Int = 0,
    /** Insert a native ad after every this-many template rows. */
    private val adRowInterval: Int = 3,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed interface ListItem {
        data class Template(val item: TemplateListItem) : ListItem
        data class NativeAd(val slotIndex: Int) : ListItem
    }

    private val items: List<ListItem> = buildMixedList()

    private fun buildMixedList(): List<ListItem> = buildList {
        if (horizontal || spanCount == 0) {
            // Carousel — templates only, no ads.
            templates.forEach { add(ListItem.Template(it)) }
            return@buildList
        }
        // Grid — insert a full-width ad row after every [adRowInterval] template rows.
        var completedRows = 0
        var adSlotIndex = 0
        templates.forEachIndexed { i, t ->
            add(ListItem.Template(t))
            val itemsAdded = i + 1
            // A new row completes every [spanCount] templates.
            if (itemsAdded % spanCount == 0) {
                completedRows++
                if (completedRows % adRowInterval.coerceAtLeast(1) == 0) {
                    add(ListItem.NativeAd(adSlotIndex++))
                }
            }
        }
    }

    companion object {
        const val VIEW_TYPE_TEMPLATE = 0
        const val VIEW_TYPE_NATIVE_AD = 1
    }

    init { setHasStableIds(true) }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.Template -> VIEW_TYPE_TEMPLATE
        is ListItem.NativeAd -> VIEW_TYPE_NATIVE_AD
    }

    override fun getItemId(position: Int): Long = when (val it = items[position]) {
        is ListItem.Template -> it.item.id.hashCode().toLong()
        // Negative to avoid collision with template hash codes.
        is ListItem.NativeAd -> -(it.slotIndex.toLong() + 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_TEMPLATE -> {
                val binding = TemplateCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                if (horizontal) {
                    binding.root.layoutParams = RecyclerView.LayoutParams(
                        parent.resources.getDimensionPixelSize(R.dimen.template_card_width),
                        parent.resources.getDimensionPixelSize(R.dimen.template_card_height)
                    )
                } else {
                    binding.root.layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        parent.resources.getDimensionPixelSize(R.dimen.template_grid_card_height)
                    )
                }
                binding.root.clipToOutline = true
                binding.preview.clipToOutline = true
                TemplateViewHolder(binding)
            }
            VIEW_TYPE_NATIVE_AD -> {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                NativeAdViewHolder(composeView)
            }
            else -> error("Unknown viewType $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TemplateViewHolder -> holder.bind((items[position] as ListItem.Template).item)
            is NativeAdViewHolder -> {
                val slot = (items[position] as ListItem.NativeAd).slotIndex
                holder.bind("template_section_ad_$slot")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ---- ViewHolders ----

    inner class TemplateViewHolder(
        private val binding: TemplateCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TemplateListItem) {
            binding.root.contentDescription = "${item.title}. ${item.subtitle}. Tap to create."
            binding.root.setOnClickListener { onTemplateClick(item) }
            binding.ideaTitle.text = item.title
            binding.ideaDescription.text = "${item.subtitle}\n\nTap to create"
            binding.ideaPlaceholder.visibility = View.VISIBLE
            binding.videoPreview.bind((item.mediaUrl ?: item.assetFileName).takeIf { item.assetFileName.endsWith(".mp4", true) })
            binding.preview.setImageDrawable(null)
            val preview = item.previewUrl ?: item.previewAssetFileName
            if (preview.isNullOrBlank()) {
                binding.preview.visibility = View.INVISIBLE
                return
            }
            binding.preview.visibility = View.VISIBLE
            binding.preview.load(if (preview.startsWith("https://")) preview else "file:///android_asset/templates/${Uri.encode(preview)}") {
                crossfade(true)
                placeholder(null)
                error(null)
                listener(
                    onError = { _, _ ->
                        binding.preview.visibility = View.INVISIBLE
                        binding.ideaPlaceholder.visibility = View.VISIBLE
                    },
                    onSuccess = { _, _ ->
                        binding.preview.visibility = View.VISIBLE
                        binding.ideaPlaceholder.visibility = View.GONE
                    }
                )
            }
        }
    }

    inner class NativeAdViewHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {
        fun bind(slotKey: String) {
            composeView.setContent {
                PlacementNativeAd(
                    placement = AdPlacement.NATIVE_TEMPLATE,
                    modifier = Modifier.padding(vertical = 8.dp),
                    slotKey = slotKey,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Item decorations
// ---------------------------------------------------------------------------

internal class HorizontalSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) > 0) outRect.left = spacing
    }
}

internal class GridSpacingDecoration(
    private val spanCount: Int,
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view).coerceAtLeast(0)
        val column = position % spanCount
        outRect.left = spacing * column / spanCount
        outRect.right = spacing * (spanCount - 1 - column) / spanCount
        if (position >= spanCount) outRect.top = spacing
    }
}

private fun dp(view: View, value: Int): Int =
    (value * view.resources.displayMetrics.density).toInt()
