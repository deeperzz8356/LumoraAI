package com.deep.lumoraai.feature.profile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val items: List<String>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data object Empty : ProfileUiState
}