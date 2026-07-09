package com.deep.lumoraai.feature.credits

sealed interface CreditsUiState {
    data object Loading : CreditsUiState
    data class Success(val items: List<String>) : CreditsUiState
    data class Error(val message: String) : CreditsUiState
    data object Empty : CreditsUiState
}