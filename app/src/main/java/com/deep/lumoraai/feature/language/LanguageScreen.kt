package com.deep.lumoraai.feature.language

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.databinding.LanguageItemBinding
import com.deep.lumoraai.databinding.LanguageScreenBinding
import com.deep.lumoraai.core.view.applySystemBarPadding

private const val DONE_UNLOCK_DELAY_MS = 3_000L

@Composable
fun LanguageScreen(
    uiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val handler = remember { Handler(Looper.getMainLooper()) }
    DisposableEffect(Unit) { onDispose { handler.removeCallbacksAndMessages(null) } }
    Column(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context -> LanguageScreenBinding.inflate(LayoutInflater.from(context)).root.apply { applySystemBarPadding() } },
            update = { root ->
                val binding = LanguageScreenBinding.bind(root)
                binding.doneButton.setOnClickListener { onDone() }
                when (uiState) {
                    LanguageUiState.Loading -> { binding.loading.visibility = View.VISIBLE; binding.languageScroll.visibility = View.GONE }
                    is LanguageUiState.Success -> bindLanguages(binding, uiState, handler, onLanguageSelected)
                }
            }, modifier = Modifier.fillMaxWidth().weight(1f)
        )
        PlacementNativeAd(placement = AdPlacement.NATIVE_LANGUAGE)
    }
}

private fun bindLanguages(binding: LanguageScreenBinding, state: LanguageUiState.Success, handler: Handler, onSelected: (String) -> Unit) {
    binding.loading.visibility = View.GONE
    binding.languageScroll.visibility = View.VISIBLE
    binding.doneButton.isEnabled = false
    binding.languageList.removeAllViews()
    state.languages.forEach { language ->
        val row = LanguageItemBinding.inflate(LayoutInflater.from(binding.root.context), binding.languageList, false)
        val selected = language.code == state.selectedLanguageCode
        row.root.isSelected = selected
        row.radioInner.visibility = if (selected) View.VISIBLE else View.GONE
        row.flag.text = language.flagEmoji
        row.name.text = language.name
        row.root.contentDescription = "${language.name}${if (selected) ", selected" else ""}"
        row.root.setOnClickListener {
            handler.removeCallbacksAndMessages(binding.doneButton)
            binding.doneButton.isEnabled = false
            onSelected(language.code)
            handler.postAtTime({ binding.doneButton.isEnabled = true }, binding.doneButton, SystemClock.uptimeMillis() + DONE_UNLOCK_DELAY_MS)
        }
        binding.languageList.addView(row.root)
    }
}
