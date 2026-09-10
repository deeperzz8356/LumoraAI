package com.deep.lumoraai.feature.onboarding

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.databinding.OnboardingScreenBinding
import com.deep.lumoraai.core.view.applySystemBarPadding

private data class OnboardingPage(
    val titleLines: List<List<TitlePart>>,
    @StringRes val description: Int,
    @DrawableRes val image: Int
)

private data class TitlePart(@StringRes val text: Int, @ColorRes val color: Int = R.color.lumora_text_primary)

private val pages = listOf(
    OnboardingPage(listOf(listOf(TitlePart(R.string.ui_next_gen)), listOf(TitlePart(R.string.ui_ai_text_to_image)), listOf(TitlePart(R.string.ui_generation_tool, R.color.lumora_indicator))), R.string.ui_onboarding_step_1_desc, R.drawable.onboarding_1_ill),
    OnboardingPage(listOf(listOf(TitlePart(R.string.ui_cinematic), TitlePart(R.string.ui_video, R.color.lumora_pink)), listOf(TitlePart(R.string.ui_onboarding_generation), TitlePart(R.string.ui_from_images))), R.string.ui_onboarding_step_2_desc, R.drawable.onboarding_2_ill),
    OnboardingPage(listOf(listOf(TitlePart(R.string.ui_generate, R.color.lumora_pink), TitlePart(R.string.ui_videos)), listOf(TitlePart(R.string.ui_onboarding_from_text), TitlePart(R.string.ui_prompts))), R.string.ui_onboarding_step_3_desc, R.drawable.onboarding_3_ill),
    OnboardingPage(listOf(listOf(TitlePart(R.string.ui_ai), TitlePart(R.string.ui_powered, R.color.lumora_pink)), listOf(TitlePart(R.string.ui_onboarding_face_swap_technology))), R.string.ui_onboarding_step_4_desc, R.drawable.onboarding_4_ill),
    OnboardingPage(listOf(listOf(TitlePart(R.string.ui_welcome_to)), listOf(TitlePart(R.string.ui_ai_2), TitlePart(R.string.ui_background, R.color.lumora_pink), TitlePart(R.string.ui_eraser))), R.string.ui_onboarding_step_5_desc, R.drawable.onboarding_5_ill)
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
    Column(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context -> OnboardingScreenBinding.inflate(LayoutInflater.from(context)).apply { content.applySystemBarPadding() }.root },
            update = { root ->
                val binding = OnboardingScreenBinding.bind(root)
                val page = pages[pageIndex]
                binding.title.text = styledTitle(root.context, page)
                binding.description.setText(page.description)
                binding.illustration.setImageResource(page.image)
                binding.illustration.contentDescription = root.context.getString(R.string.ui_onboarding_step_illustration, pageIndex + 1)
                binding.pageIndicator.contentDescription = "${pageIndex + 1} / ${pages.size}"
                listOf(binding.indicator1, binding.indicator2, binding.indicator3, binding.indicator4, binding.indicator5)
                    .forEachIndexed { index, indicator ->
                        indicator.layoutParams = (indicator.layoutParams as LinearLayout.LayoutParams).apply {
                            width = ((if (index == pageIndex) 24 else 6) * root.resources.displayMetrics.density).toInt()
                        }
                        indicator.setBackgroundResource(if (index == pageIndex) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive)
                    }
                binding.nextButton.setText(if (pageIndex == pages.lastIndex) R.string.ui_let_s_start else R.string.ui_next)
                binding.nextButton.layoutParams = binding.nextButton.layoutParams.apply {
                    width = ((if (pageIndex == pages.lastIndex) 140 else 82) * root.resources.displayMetrics.density).toInt()
                    height = ((if (pageIndex == pages.lastIndex) 52 else 48) * root.resources.displayMetrics.density).toInt()
                }
                binding.skipButton.setOnClickListener { complete() }
                binding.nextButton.setOnClickListener {
                    if (pageIndex == pages.lastIndex) complete() else pageIndex++
                }
            }, modifier = Modifier.fillMaxSize().weight(1f)
        )
        val placement = when (pageIndex) {
            0 -> AdPlacement.OB_NATIVE_1
            1 -> AdPlacement.OB_NATIVE_2
            2 -> AdPlacement.OB_NATIVE_3
            3 -> AdPlacement.OB_NATIVE_4
            else -> null
        }
        if (placement != null) PlacementNativeAd(placement = placement)
    }
}

private fun styledTitle(context: android.content.Context, page: OnboardingPage): CharSequence {
    val result = SpannableStringBuilder()
    page.titleLines.forEachIndexed { lineIndex, line ->
        line.forEachIndexed { partIndex, part ->
            if (partIndex > 0) result.append(' ')
            val start = result.length
            result.append(context.getString(part.text).trim())
            result.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, part.color)), start, result.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (lineIndex < page.titleLines.lastIndex) result.append('\n')
    }
    return result
}
