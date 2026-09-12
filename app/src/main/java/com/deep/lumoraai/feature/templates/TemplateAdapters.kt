package com.deep.lumoraai.feature.templates

import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.deep.lumoraai.R
import com.deep.lumoraai.databinding.TemplateCardBinding
import com.deep.lumoraai.databinding.TemplateSectionRowBinding
import com.deep.lumoraai.feature.templates.model.TemplateListItem
import com.deep.lumoraai.feature.templates.model.TemplateSection

internal class TemplateSectionsAdapter(
    val categoryId: String,
    private val sections: List<TemplateSection>,
    private val scrollMemory: TemplateScrollMemory,
    private val onTemplateClick: (TemplateListItem) -> Unit,
    private val onViewAll: (TemplateSection) -> Unit,
) : RecyclerView.Adapter<TemplateSectionsAdapter.SectionViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = sections[position].id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder =
        SectionViewHolder(
            TemplateSectionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun onViewRecycled(holder: SectionViewHolder) {
        holder.savePosition()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = sections.size

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
                binding.cards.addItemDecoration(
                    HorizontalSpacingDecoration(dp(binding.root, 12))
                )
            }
            binding.cards.adapter = TemplateCardAdapter(
                templates = section.templates,
                horizontal = true,
                onTemplateClick = onTemplateClick
            )
            manager.scrollToPositionWithOffset(
                scrollMemory.sectionPosition(categoryId, section.id),
                0
            )
            binding.cards.clearOnScrollListeners()
            binding.cards.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
}

internal class TemplateCardAdapter(
    private val templates: List<TemplateListItem>,
    private val horizontal: Boolean,
    private val onTemplateClick: (TemplateListItem) -> Unit,
) : RecyclerView.Adapter<TemplateCardAdapter.TemplateViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = templates[position].id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = TemplateCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size

    inner class TemplateViewHolder(
        private val binding: TemplateCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TemplateListItem) {
            binding.root.contentDescription = item.title
            binding.root.setOnClickListener { onTemplateClick(item) }
            binding.preview.setImageDrawable(null)
            val preview = item.previewAssetFileName
            if (preview.isNullOrBlank()) {
                binding.preview.visibility = View.INVISIBLE
                return
            }
            binding.preview.visibility = View.VISIBLE
            binding.preview.load("file:///android_asset/templates/${Uri.encode(preview)}") {
                crossfade(true)
                placeholder(null)
                error(null)
                listener(
                    onError = { _, _ -> binding.preview.visibility = View.INVISIBLE },
                    onSuccess = { _, _ -> binding.preview.visibility = View.VISIBLE }
                )
            }
        }
    }
}

internal class HorizontalSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) > 0) outRect.left = spacing
    }
}

internal class GridSpacingDecoration(
    private val spanCount: Int,
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view).coerceAtLeast(0)
        val column = position % spanCount
        outRect.left = spacing * column / spanCount
        outRect.right = spacing * (spanCount - 1 - column) / spanCount
        if (position >= spanCount) outRect.top = spacing
    }
}

private fun dp(view: View, value: Int): Int =
    (value * view.resources.displayMetrics.density).toInt()
