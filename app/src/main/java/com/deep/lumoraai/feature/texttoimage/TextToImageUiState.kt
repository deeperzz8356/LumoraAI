package com.deep.lumoraai.feature.texttoimage

sealed interface TextToImageUiState {
    data object Loading : TextToImageUiState
    data class Success(val items: List<String>) : TextToImageUiState
    data class Error(val message: String) : TextToImageUiState
    data object Empty : TextToImageUiState
}