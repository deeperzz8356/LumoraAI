package com.deep.lumoraai.feature.aitools

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.avatarRoute
import com.deep.lumoraai.core.navigation.bgStudioRoute
import com.deep.lumoraai.core.navigation.logoRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.databinding.AiToolsItemToolBinding
import com.deep.lumoraai.databinding.AiToolsScreenBinding

private const val Lime = 0xFFD6FF2F.toInt()
private const val Purple = 0xFF9C63FF.toInt()
private const val Pink = 0xFFFF3D9D.toInt()
private const val Cyan = 0xFF20E6F2.toInt()
private const val BlueAccent = 0xFF7D86FF.toInt()

@Composable
fun AIToolsScreen(
    credits: Int,
    onNavigate: (String) -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF081020),
        bottomBar = { BottomNavigationBar(emptyList(), "aitools", onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081020))
                .padding(padding)
        ) {
            AndroidView(
                factory = { AiToolsScreenBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindAiTools(
                        binding = AiToolsScreenBinding.bind(root),
                        credits = credits,
                        unreadCount = unreadCount,
                        onNavigate = onNavigate,
                    )
                },
                modifier = Modifier.weight(1f)
            )
            PlacementNativeAd(
                placement = AdPlacement.NATIVE_TOOLS,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun bindAiTools(
    binding: AiToolsScreenBinding,
    credits: Int,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
) {
    binding.creditsChip.text = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        credits.toString()
    }
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.unreadDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
    bindToolsGrid(binding.toolsGrid, onNavigate)
}

private fun bindToolsGrid(grid: GridLayout, onNavigate: (String) -> Unit) {
    if (grid.childCount == 6) return
    grid.removeAllViews()
    val context = grid.context
    listOf(
        ToolSpec(context.getString(R.string.ui_tool_logo), context.getString(R.string.ui_generate_a_logo), R.drawable.ic_lumora_logo_tool, Cyan, logoRoute()),
        ToolSpec(context.getString(R.string.ui_tool_ai_avatar), context.getString(R.string.ui_create_your_avatar), R.drawable.ic_lumora_avatar, Purple, avatarRoute()),
        ToolSpec(context.getString(R.string.ui_photo_enhancer), context.getString(R.string.ui_improve_quality), R.drawable.ic_lumora_enhance, Purple, Screen.PhotoEnhance.route),
        ToolSpec(context.getString(R.string.ui_remove_background), context.getString(R.string.ui_cut_subject), R.drawable.ic_lumora_cutout, BlueAccent, bgStudioRoute("remove")),
        ToolSpec(context.getString(R.string.ui_promo_videos), context.getString(R.string.ui_ad_ready_clips), R.drawable.ic_lumora_video, Pink, Screen.PromoVideo.route),
        ToolSpec(context.getString(R.string.ui_compress), context.getString(R.string.ui_smaller_files_without_the_mess), R.drawable.ic_lumora_compress, Lime, Screen.Compress.route),
    ).forEachIndexed { index, spec ->
        val item = AiToolsItemToolBinding.inflate(LayoutInflater.from(context), grid, false)
        item.title.text = spec.title
        item.subtitle.text = spec.subtitle
        item.iconGlyph.setImageResource(spec.iconRes)
        item.iconGlyph.setColorFilter(spec.accent)
        item.iconGlyph.background = roundedFill(accentWithAlpha(spec.accent, 0x24), 10f, grid)
        item.root.setOnClickListener { onNavigate(spec.route) }
        grid.addView(item.root, gridParams(index, grid))
    }
}

private fun gridParams(index: Int, view: View): GridLayout.LayoutParams =
    GridLayout.LayoutParams(
        GridLayout.spec(index / 2, 1),
        GridLayout.spec(index % 2, 1f)
    ).apply {
        width = 0
        height = dp(view, 116)
        setMargins(
            if (index % 2 == 0) 0 else dp(view, 5),
            if (index < 2) 0 else dp(view, 10),
            if (index % 2 == 0) dp(view, 5) else 0,
            0
        )
    }

private fun roundedFill(color: Int, radiusDp: Float, view: View): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radiusDp * view.resources.displayMetrics.density
    }

private fun accentWithAlpha(color: Int, alpha: Int): Int =
    (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

private fun dp(view: View, value: Int): Int = (value * view.resources.displayMetrics.density).toInt()

private data class ToolSpec(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val accent: Int,
    val route: String,
)
