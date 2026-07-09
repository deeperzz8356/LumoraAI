package com.deep.lumoraai.feature.result

sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(val items: List<String>) : ResultUiState
    data class Error(val message: String) : ResultUiState
    data object Empty : ResultUiState
}