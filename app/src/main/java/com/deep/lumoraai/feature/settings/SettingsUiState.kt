package com.deep.lumoraai.feature.settings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val items: List<String>) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
    data object Empty : SettingsUiState
}