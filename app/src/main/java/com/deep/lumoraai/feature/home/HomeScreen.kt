package com.deep.lumoraai.feature.home

import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdFormat
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.AdsConfigStore
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.nativead.NativeAdManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.avatarRoute
import com.deep.lumoraai.core.navigation.bgStudioRoute
import com.deep.lumoraai.core.navigation.logoRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.OnboardingPreferences
import com.deep.lumoraai.databinding.ItemHomeActionBinding
import com.deep.lumoraai.databinding.ItemHomeRecentBinding
import com.deep.lumoraai.databinding.ItemHomeToolBinding
import com.deep.lumoraai.databinding.ScreenHomeBinding
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay
import java.io.File

private const val HOME_XML_NATIVE_TAG = "home_xml_native_loaded"
private const val HomeBackground = 0xFF081020.toInt()
private const val Lime = 0xFFD6FF2F.toInt()
private const val Purple = 0xFF9C63FF.toInt()
private const val Pink = 0xFFFF3D9D.toInt()
private const val Cyan = 0xFF20E6F2.toInt()
private const val Indigo = 0xFF7D86FF.toInt()

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    unreadCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ads = LocalAdsManager.current
    val adStore = LocalAdsConfigStore.current
    val activity = rememberCurrentActivity()
    val handler = remember { Handler(Looper.getMainLooper()) }
    var showProfileHint by remember { mutableStateOf(!OnboardingPreferences.isProfileHintSeen(context)) }

    LaunchedEffect(Unit) {
        ads?.preloadInterstitial(context, AdPlacement.INTER_ALL)
    }

    fun dismissProfileHint() {
        OnboardingPreferences.markProfileHintSeen(context)
        showProfileHint = false
    }

    LaunchedEffect(showProfileHint) {
        if (showProfileHint) {
            delay(2_000)
            dismissProfileHint()
        }
    }

    DisposableEffect(Unit) {
        onDispose { handler.removeCallbacksAndMessages(null) }
    }

    val featureSelect: (String) -> Unit = { route ->
        if (ads == null) {
            onNavigate(route)
        } else {
            ads.recordFeatureTrigger()
            ads.showInterstitial(activity, AdPlacement.INTER_ALL, requireTrigger = true) {
                onNavigate(route)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ComposeColor(HomeBackground),
        bottomBar = { BottomNavigationBar(emptyList(), Screen.Home.route, onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ComposeColor(HomeBackground))
                .padding(padding)
        ) {
            AndroidView(
                factory = { ScreenHomeBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindHome(
                        binding = ScreenHomeBinding.bind(root),
                        uiState = uiState,
                        unreadCount = unreadCount,
                        showProfileHint = showProfileHint,
                        onNavigate = onNavigate,
                        onFeatureSelect = featureSelect,
                        onNotificationClick = onNotificationClick,
                        onDismissProfileHint = { dismissProfileHint() },
                        bindNativeAd = {
                            if (ads != null && adStore != null) {
                                bindNativeAdSlot(root.context, it, ads.nativeManager, adStore)
                            } else {
                                it.visibility = View.GONE
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun bindHome(
    binding: ScreenHomeBinding,
    uiState: HomeUiState,
    unreadCount: Int,
    showProfileHint: Boolean,
    onNavigate: (String) -> Unit,
    onFeatureSelect: (String) -> Unit,
    onNotificationClick: (() -> Unit)?,
    onDismissProfileHint: () -> Unit,
    bindNativeAd: (ViewGroup) -> Unit,
) {
    binding.loading.visibility = if (uiState is HomeUiState.Loading) View.VISIBLE else View.GONE
    binding.messageState.visibility = View.GONE
    binding.contentScroll.visibility = View.GONE

    when (uiState) {
        is HomeUiState.Loading -> return
        is HomeUiState.Error -> showMessage(binding, uiState.message, "")
        is HomeUiState.Empty -> showMessage(
            binding,
            binding.root.context.getString(R.string.ui_no_content),
            "Nothing to see here."
        )
        is HomeUiState.Success -> bindSuccess(
            binding = binding,
            state = uiState,
            unreadCount = unreadCount,
            showProfileHint = showProfileHint,
            onNavigate = onNavigate,
            onFeatureSelect = onFeatureSelect,
            onNotificationClick = onNotificationClick,
            onDismissProfileHint = onDismissProfileHint,
            bindNativeAd = bindNativeAd,
        )
    }
}

private fun showMessage(binding: ScreenHomeBinding, title: String, body: String) {
    binding.messageState.visibility = View.VISIBLE
    binding.messageTitle.text = title
    binding.messageBody.text = body
    binding.messageBody.visibility = if (body.isBlank()) View.GONE else View.VISIBLE
}

private fun bindSuccess(
    binding: ScreenHomeBinding,
    state: HomeUiState.Success,
    unreadCount: Int,
    showProfileHint: Boolean,
    onNavigate: (String) -> Unit,
    onFeatureSelect: (String) -> Unit,
    onNotificationClick: (() -> Unit)?,
    onDismissProfileHint: () -> Unit,
    bindNativeAd: (ViewGroup) -> Unit,
) {
    val context = binding.root.context
    binding.contentScroll.visibility = View.VISIBLE
    binding.profileHintOverlay.visibility = if (showProfileHint) View.VISIBLE else View.GONE
    binding.profileHintOverlay.setOnClickListener { onDismissProfileHint() }
    binding.profileHintAvatarHit.setOnClickListener {
        onDismissProfileHint()
        onNavigate(Screen.Profile.route)
    }

    val rawName = state.userName.ifBlank { context.getString(R.string.ui_guest) }
    val displayName = if (rawName.length > 8) "${rawName.take(8)}..." else rawName
    binding.greeting.text = context.getString(R.string.ui_hi_name, displayName)
    binding.avatar.setOnClickListener { onNavigate(Screen.Profile.route) }
    binding.creditsChip.text = if (state.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "✦ Unlimited"
    } else {
        "✦ ${state.credits}"
    }
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.unreadDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    binding.notificationButton.setOnClickListener {
        onNotificationClick?.invoke() ?: onNavigate(Screen.Notifications.route)
    }
    binding.heroPreview.setOnClickListener { onNavigate(Screen.History.route) }

    bindCreateGrid(binding.createGrid, onFeatureSelect)
    bindRecent(binding, state.recentItems, onNavigate)
    bindNativeAd(binding.nativeAdSlot)
    bindToolsGrid(binding.toolsGrid, onFeatureSelect)
}

private fun bindCreateGrid(grid: GridLayout, onNavigate: (String) -> Unit) {
    if (grid.childCount == 4) return
    grid.removeAllViews()
    val context = grid.context
    listOf(
        HomeCardSpec(context.getString(R.string.ui_create_text_to_image), context.getString(R.string.ui_create_dream_it), "*", Lime, Screen.TextToImage.route),
        HomeCardSpec(context.getString(R.string.ui_create_img_to_img), context.getString(R.string.ui_create_refine_it), "#", Purple, Screen.ImageToImage.route),
        HomeCardSpec(context.getString(R.string.ui_create_img_to_video), context.getString(R.string.ui_create_animate_it), "◆", Pink, Screen.ImageToVideo.route),
        HomeCardSpec(context.getString(R.string.ui_create_text_to_video), context.getString(R.string.ui_create_direct_it), "▶", Cyan, Screen.TextToVideo.route),
    ).forEachIndexed { index, spec ->
        val card = ItemHomeActionBinding.inflate(LayoutInflater.from(context), grid, false)
        card.title.text = spec.title
        card.subtitle.text = spec.subtitle
        card.iconGlyph.text = spec.glyph
        card.iconGlyph.setTextColor(spec.accent)
        card.arrow.setTextColor(spec.accent)
        card.root.setOnClickListener { onNavigate(spec.route) }
        grid.addView(card.root, gridParams(index, 118, grid))
    }
}

private fun bindToolsGrid(grid: GridLayout, onNavigate: (String) -> Unit) {
    if (grid.childCount == 6) return
    grid.removeAllViews()
    val context = grid.context
    listOf(
        HomeCardSpec(context.getString(R.string.ui_tool_logo), "", "✎", Cyan, logoRoute()),
        HomeCardSpec(context.getString(R.string.ui_tool_ai_avatar), "", "☺", Purple, avatarRoute()),
        HomeCardSpec(context.getString(R.string.ui_tool_photo_enhancer), "", "≋", Purple, Screen.PhotoEnhance.route),
        HomeCardSpec(context.getString(R.string.ui_tool_remove_background), "", "◉", Indigo, bgStudioRoute("remove")),
        HomeCardSpec(context.getString(R.string.ui_tool_promo_videos), "", "▶", Pink, Screen.PromoVideo.route),
        HomeCardSpec(context.getString(R.string.ui_tool_compress), "", "⇲", Lime, Screen.Compress.route),
    ).forEachIndexed { index, spec ->
        val card = ItemHomeToolBinding.inflate(LayoutInflater.from(context), grid, false)
        card.title.text = spec.title
        card.iconGlyph.text = spec.glyph
        card.iconGlyph.setTextColor(spec.accent)
        card.iconGlyph.background = roundedFill(accentWithAlpha(spec.accent, 0x24), 12f, grid)
        card.root.setOnClickListener { onNavigate(spec.route) }
        grid.addView(card.root, gridParams(index, 140, grid))
    }
}

private fun bindRecent(
    binding: ScreenHomeBinding,
    items: List<HomeRecentItem>,
    onNavigate: (String) -> Unit,
) {
    binding.recentSection.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    binding.viewAllRecent.setOnClickListener { onNavigate(Screen.History.route) }
    binding.recentList.removeAllViews()
    items.forEachIndexed { index, item ->
        val row = ItemHomeRecentBinding.inflate(LayoutInflater.from(binding.root.context), binding.recentList, false)
        row.title.text = item.title
        row.time.text = item.timeLabel
        row.playBadge.visibility = if (item.mediaType.equals("VIDEO", ignoreCase = true)) View.VISIBLE else View.GONE
        bindRecentImage(row.mediaImage, item)
        row.root.setOnClickListener { onNavigate(Screen.History.route) }
        val marginEnd = if (index == items.lastIndex) 0 else dp(row.root, 10)
        binding.recentList.addView(row.root, ViewGroup.MarginLayoutParams(dp(row.root, 190), dp(row.root, 78)).apply {
            setMargins(0, 0, marginEnd, 0)
        })
    }
}

private fun bindRecentImage(image: ImageView, item: HomeRecentItem) {
    val file = item.mediaUrl?.takeIf { it.isNotBlank() }?.let(::File)
    when {
        file == null || !file.exists() -> image.setImageResource(item.fallbackImageRes)
        item.mediaType.equals("VIDEO", ignoreCase = true) -> {
            videoFrame(file)?.let { image.setImageBitmap(it) } ?: image.setImageResource(item.fallbackImageRes)
        }
        else -> image.setImageURI(Uri.fromFile(file))
    }
    image.contentDescription = item.title
}

private fun bindNativeAdSlot(
    context: android.content.Context,
    slot: ViewGroup,
    manager: NativeAdManager,
    store: AdsConfigStore,
) {
    val config = store.current
    if (!config.formatEnabled(AdFormat.NATIVE) || !config.isPlacementEnabled(AdPlacement.NATIVE_HOME)) {
        slot.visibility = View.GONE
        return
    }
    if (slot.tag == HOME_XML_NATIVE_TAG) return
    slot.tag = HOME_XML_NATIVE_TAG
    slot.visibility = View.GONE
    manager.load(
        context = context,
        placement = AdPlacement.NATIVE_HOME,
        cacheKey = "home_xml",
        onLoaded = { ad ->
            slot.removeAllViews()
            val adView = LayoutInflater.from(context).inflate(R.layout.ad_native_large, slot, false) as NativeAdView
            bindAssetViews(adView)
            populateNativeAd(adView, ad)
            slot.addView(adView)
            slot.visibility = View.VISIBLE
        },
        onFailed = {
            slot.removeAllViews()
            slot.visibility = View.GONE
        }
    )
}

private fun bindAssetViews(adView: NativeAdView) {
    adView.headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    adView.bodyView = adView.findViewById<TextView>(R.id.ad_body)
    adView.callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    adView.mediaView = adView.findViewById<MediaView?>(R.id.ad_media)
}

private fun populateNativeAd(adView: NativeAdView, ad: NativeAd) {
    (adView.headlineView as? TextView)?.text = ad.headline
    (adView.bodyView as? TextView)?.apply {
        text = ad.body
        visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (adView.callToActionView as? Button)?.apply {
        text = ad.callToAction ?: "Learn more"
        visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (adView.iconView as? ImageView)?.apply {
        val drawable = ad.icon?.drawable
        if (drawable != null) {
            setImageDrawable(drawable)
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }
    adView.mediaView?.let { mediaView ->
        mediaView.setImageScaleType(ImageView.ScaleType.FIT_CENTER)
        ad.mediaContent?.let { mediaView.mediaContent = it }
    }
    adView.setNativeAd(ad)
}

private fun gridParams(index: Int, heightDp: Int, view: View): GridLayout.LayoutParams =
    GridLayout.LayoutParams(
        GridLayout.spec(index / 2, 1),
        GridLayout.spec(index % 2, 1f)
    ).apply {
        width = 0
        height = dp(view, heightDp)
        setMargins(
            if (index % 2 == 0) 0 else dp(view, 6),
            if (index < 2) 0 else dp(view, 11),
            if (index % 2 == 0) dp(view, 6) else 0,
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

private fun videoFrame(file: File): Bitmap? = runCatching {
    MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}.getOrNull()

private data class HomeCardSpec(
    val title: String,
    val subtitle: String,
    val glyph: String,
    val accent: Int,
    val route: String,
)
