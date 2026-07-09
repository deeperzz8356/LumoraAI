package com.deep.lumoraai.feature.history

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val items: List<String>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data object Empty : HistoryUiState
}