package com.deep.lumoraai.feature.createhub

sealed interface CreateHubUiState {
    data object Loading : CreateHubUiState
    data object Generating : CreateHubUiState
    data class ImageGenerated(val imageUrl: String) : CreateHubUiState
    data class VideoGenerated(val videoUrl: String) : CreateHubUiState
    data class Success(val items: List<String>) : CreateHubUiState
    data class Error(val message: String) : CreateHubUiState
    data object Empty : CreateHubUiState
}