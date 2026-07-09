package com.deep.lumoraai.feature.queue

sealed interface QueueUiState {
    data object Loading : QueueUiState
    data class Success(val items: List<String>) : QueueUiState
    data class Error(val message: String) : QueueUiState
    data object Empty : QueueUiState
}