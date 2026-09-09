package com.deep.lumoraai.feature.onboarding

import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.databinding.ScreenOnboardingBinding

private data class OnboardingPage(
    val titleParts: List<Int>,
    @StringRes val description: Int,
    @DrawableRes val image: Int
)

private val pages = listOf(
    OnboardingPage(listOf(R.string.ui_next_gen, R.string.ui_ai_text_to_image, R.string.ui_generation_tool), R.string.ui_onboarding_step_1_desc, R.drawable.onboarding_1_ill),
    OnboardingPage(listOf(R.string.ui_cinematic, R.string.ui_video, R.string.ui_onboarding_generation, R.string.ui_from_images), R.string.ui_onboarding_step_2_desc, R.drawable.onboarding_2_ill),
    OnboardingPage(listOf(R.string.ui_generate, R.string.ui_videos, R.string.ui_onboarding_from_text, R.string.ui_prompts), R.string.ui_onboarding_step_3_desc, R.drawable.onboarding_3_ill),
    OnboardingPage(listOf(R.string.ui_ai, R.string.ui_powered, R.string.ui_onboarding_face_swap_technology), R.string.ui_onboarding_step_4_desc, R.drawable.onboarding_4_ill),
    OnboardingPage(listOf(R.string.ui_welcome_to, R.string.ui_ai_2, R.string.ui_background, R.string.ui_eraser), R.string.ui_onboarding_step_5_desc, R.drawable.onboarding_5_ill)
)

@Composable
fun OnboardingScreen(uiState: OnboardingUiState, onNext: () -> Unit, modifier: Modifier = Modifier) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var completing by remember { mutableStateOf(false) }
    val ads = LocalAdsManager.current
    val activity = rememberCurrentActivity()
    val complete = {
        if (!completing) {
            completing = true
            if (ads == null) onNext() else ads.showInterstitial(activity, AdPlacement.OB_INTER, onContinue = onNext)
        }
    }
    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context -> ScreenOnboardingBinding.inflate(LayoutInflater.from(context)).root },
            update = { root ->
                val binding = ScreenOnboardingBinding.bind(root)
                val page = pages[pageIndex]
                binding.title.text = page.titleParts.joinToString("\n") { root.context.getString(it).trim() }
                binding.description.setText(page.description)
                binding.illustration.setImageResource(page.image)
                binding.illustration.contentDescription = root.context.getString(R.string.ui_onboarding_step_illustration, pageIndex + 1)
                binding.pageIndicator.text = "${pageIndex + 1} / ${pages.size}"
                binding.nextButton.setText(if (pageIndex == pages.lastIndex) R.string.ui_let_s_start else R.string.ui_next)
                binding.adSpace.layoutParams = binding.adSpace.layoutParams.apply {
                    height = if (pageIndex < pages.lastIndex) (300 * root.resources.displayMetrics.density).toInt() else 0
                }
                binding.skipButton.setOnClickListener { complete() }
                binding.nextButton.setOnClickListener {
                    if (pageIndex == pages.lastIndex) complete() else pageIndex++
                }
            }, modifier = Modifier.fillMaxSize()
        )
        val placement = when (pageIndex) {
            0 -> AdPlacement.OB_NATIVE_1
            1 -> AdPlacement.OB_NATIVE_2
            2 -> AdPlacement.OB_NATIVE_3
            3 -> AdPlacement.OB_NATIVE_4
            else -> null
        }
        if (placement != null) PlacementNativeAd(placement = placement, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
