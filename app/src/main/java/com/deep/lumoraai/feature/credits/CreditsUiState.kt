package com.deep.lumoraai.feature.credits

sealed interface CreditsUiState {
    data object Loading : CreditsUiState
    data class Success(val credits: Int) : CreditsUiState
    data class Error(val message: String) : CreditsUiState
}