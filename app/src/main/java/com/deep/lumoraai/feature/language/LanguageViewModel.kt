package com.deep.lumoraai.feature.language

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deep.lumoraai.feature.language.model.LanguageModel

class LanguageViewModel : ViewModel() {
    var uiState: LanguageUiState by mutableStateOf(LanguageUiState.Loading)
        private set

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        val languages = listOf(
            LanguageModel("en", "English", "🇬🇧"),
            LanguageModel("hi", "Hindi", "🇮🇳"),
            LanguageModel("es", "Spanish", "🇪🇸"),
            LanguageModel("nl", "Dutch", "🇳🇱"),
            LanguageModel("it", "Italian", "🇮🇹"),
            LanguageModel("pt", "Portuguese", "🇵🇹"),
            LanguageModel("tr", "Turkish", "🇹🇷"),
            LanguageModel("th", "Thai", "🇹🇭"),
            LanguageModel("vi", "Vietnamese", "🇻🇳"),
            LanguageModel("ar", "Arabic", "🇸🇦"),
            LanguageModel("ko", "Korean", "🇰🇷"),
            LanguageModel("ja", "Japanese", "🇯🇵"),
            LanguageModel("zh", "Chinese (Simplified)", "🇨🇳"),
            LanguageModel("zht", "Chinese (Traditional)", "🇹🇼")
        )
        uiState = LanguageUiState.Success(
            languages = languages,
            selectedLanguageCode = "en",
            searchQuery = ""
        )
    }

    fun selectLanguage(code: String) {
        val currentState = uiState
        if (currentState is LanguageUiState.Success) {
            uiState = currentState.copy(selectedLanguageCode = code)
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = uiState
        if (currentState is LanguageUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }
}
