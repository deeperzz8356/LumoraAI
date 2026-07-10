package com.deep.lumoraai.feature.language

import com.deep.lumoraai.feature.language.model.LanguageModel

sealed interface LanguageUiState {
    data object Loading : LanguageUiState
    
    data class Success(
        val languages: List<LanguageModel>,
        val selectedLanguageCode: String,
        val searchQuery: String
    ) : LanguageUiState
}
