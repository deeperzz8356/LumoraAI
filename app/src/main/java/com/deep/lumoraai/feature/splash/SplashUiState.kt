package com.deep.lumoraai.feature.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Success(val items: List<String>) : SplashUiState
    data class Error(val message: String) : SplashUiState
    data object Empty : SplashUiState
}