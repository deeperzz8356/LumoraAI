package com.deep.lumoraai.feature.queue

import com.deep.lumoraai.data.model.ActiveJobInfo

sealed interface QueueUiState {
    data object Loading : QueueUiState
    data class Success(val items: List<ActiveJobInfo>) : QueueUiState
    data class Error(val message: String) : QueueUiState
    data object Empty : QueueUiState
}