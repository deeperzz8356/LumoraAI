package com.deep.lumoraai.feature.createhub

sealed interface CreateHubUiState {
    data object Loading : CreateHubUiState
    data object Generating : CreateHubUiState
    data object TrialExpired : CreateHubUiState
    data class ImageGenerated(val filePath: String, val mimeType: String = "image/png") : CreateHubUiState
    data class VideoGenerated(val filePath: String, val mimeType: String = "video/mp4") : CreateHubUiState
    data class Success(val items: List<String>) : CreateHubUiState
    data class Error(val message: String) : CreateHubUiState
    data object Empty : CreateHubUiState
}
