package com.deep.lumoraai.feature.imagetovideo

sealed interface ImageToVideoUiState {
    data object Loading : ImageToVideoUiState
    data class Success(val items: List<String>) : ImageToVideoUiState
    data class Error(val message: String) : ImageToVideoUiState
    data object Empty : ImageToVideoUiState
}