package com.deep.lumoraai.feature.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.deep.lumoraai.data.repository.SettingsRepository
import com.deep.lumoraai.feature.language.model.LanguageModel

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    var uiState: LanguageUiState by mutableStateOf(LanguageUiState.Loading)
        private set

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        val languages = listOf(
            LanguageModel("en", "English", "🇺🇸"),
            LanguageModel("vi", "Tiếng Việt", "🇻🇳"),
            LanguageModel("es", "Español", "🇪🇸"),
            LanguageModel("fr", "Français", "🇫🇷"),
            LanguageModel("de", "Deutsch", "🇩🇪"),
            LanguageModel("it", "Italiano", "🇮🇹"),
            LanguageModel("pt", "Português", "🇧🇷"),
            LanguageModel("tr", "Türkçe", "🇹🇷"),
            LanguageModel("ar", "العربية", "🇸🇦"),
            LanguageModel("hi", "हिन्दी", "🇮🇳"),
            LanguageModel("ko", "한국어", "🇰🇷"),
            LanguageModel("zh", "中文", "🇨🇳")
        )
        uiState = LanguageUiState.Success(
            languages = languages,
            selectedLanguageCode = settingsRepository.localeCode,
            searchQuery = ""
        )
    }

    fun selectLanguage(code: String) {
        val currentState = uiState
        if (currentState is LanguageUiState.Success) {
            uiState = currentState.copy(selectedLanguageCode = code)
        }

    }

    fun persistSelection() {
        val state = uiState as? LanguageUiState.Success ?: return
        settingsRepository.localeCode = state.selectedLanguageCode
    }

    fun updateSearchQuery(query: String) {
        val currentState = uiState
        if (currentState is LanguageUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }
}
