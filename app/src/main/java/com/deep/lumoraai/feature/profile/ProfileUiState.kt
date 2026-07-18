package com.deep.lumoraai.feature.profile

import com.deep.lumoraai.data.repository.GenerationHistoryItem

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val items: List<String>, val generations: List<GenerationHistoryItem> = emptyList(), val credits: Int = 0) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object Empty : ProfileUiState
}