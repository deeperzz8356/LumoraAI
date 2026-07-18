package com.deep.lumoraai.feature.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val items: List<String>, val credits: Int = 0) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}