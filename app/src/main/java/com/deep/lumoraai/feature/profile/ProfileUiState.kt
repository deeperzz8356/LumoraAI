package com.deep.lumoraai.feature.profile

import com.deep.lumoraai.data.model.HistoryModel

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val items: List<String>,
        val generations: List<HistoryModel> = emptyList(),
        val credits: Int = 0,
        val isGuest: Boolean = true,
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object Empty : ProfileUiState
}
