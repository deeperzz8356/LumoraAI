package com.deep.lumoraai.feature.auth

sealed interface AuthUiState {
    data object Initial : AuthUiState
    data class EmailForm(val isSignUp: Boolean) : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}
