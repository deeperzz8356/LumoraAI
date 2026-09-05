package com.deep.lumoraai.feature.auth

sealed interface AuthUiState {
    data object Initial : AuthUiState
    data class EmailForm(val isSignUp: Boolean) : AuthUiState
    data object Loading : AuthUiState

    /**
     * Authentication succeeded.
     *
     * @param isNewAccount true when this success is the result of creating a
     *   brand-new account (email sign-up), so the UI can surface a confirmation.
     */
    data class Success(val isNewAccount: Boolean = false) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
