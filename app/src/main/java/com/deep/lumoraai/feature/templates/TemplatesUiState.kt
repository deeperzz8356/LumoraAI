package com.deep.lumoraai.feature.templates

sealed interface TemplatesUiState {
    data object Loading : TemplatesUiState
    data class Success(val items: List<String>) : TemplatesUiState
    data class Error(val message: String) : TemplatesUiState
    data object Empty : TemplatesUiState
}