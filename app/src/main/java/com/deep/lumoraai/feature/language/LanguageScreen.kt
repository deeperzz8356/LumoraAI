package com.deep.lumoraai.feature.language

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.databinding.ItemLanguageBinding
import com.deep.lumoraai.databinding.ScreenLanguageBinding

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
    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context -> ScreenLanguageBinding.inflate(LayoutInflater.from(context)).root },
            update = { root ->
                val binding = ScreenLanguageBinding.bind(root)
                binding.doneButton.setOnClickListener { onDone() }
                when (uiState) {
                    LanguageUiState.Loading -> { binding.loading.visibility = View.VISIBLE; binding.languageScroll.visibility = View.GONE }
                    is LanguageUiState.Success -> bindLanguages(binding, uiState, handler, onLanguageSelected)
                }
            }, modifier = Modifier.fillMaxSize()
        )
        PlacementNativeAd(placement = AdPlacement.NATIVE_LANGUAGE, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun bindLanguages(binding: ScreenLanguageBinding, state: LanguageUiState.Success, handler: Handler, onSelected: (String) -> Unit) {
    binding.loading.visibility = View.GONE
    binding.languageScroll.visibility = View.VISIBLE
    binding.doneButton.isEnabled = false
    binding.languageList.removeAllViews()
    state.languages.forEach { language ->
        val row = ItemLanguageBinding.inflate(LayoutInflater.from(binding.root.context), binding.languageList, false)
        val selected = language.code == state.selectedLanguageCode
        row.root.isSelected = selected
        row.radio.isChecked = selected
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
