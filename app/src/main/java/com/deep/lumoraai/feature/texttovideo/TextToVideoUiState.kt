package com.deep.lumoraai.feature.texttovideo

sealed interface TextToVideoUiState {
    data object Loading : TextToVideoUiState
    data class Success(val items: List<String>) : TextToVideoUiState
    data class Error(val message: String) : TextToVideoUiState
    data object Empty : TextToVideoUiState
}