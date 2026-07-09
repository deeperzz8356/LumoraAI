package com.deep.lumoraai.feature.createhub

sealed interface CreateHubUiState {
    data object Loading : CreateHubUiState
    data class Success(val items: List<String>) : CreateHubUiState
    data class Error(val message: String) : CreateHubUiState
    data object Empty : CreateHubUiState
}