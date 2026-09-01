package com.deep.lumoraai.feature.history

import com.deep.lumoraai.data.model.HistoryModel

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val items: List<HistoryModel>, val credits: Int = 0) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Empty(val credits: Int = 0) : HistoryUiState
}
