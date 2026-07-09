package com.deep.lumoraai.feature.onboarding

sealed interface OnboardingUiState {
    data object Loading : OnboardingUiState
    data class Success(val items: List<String>) : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState
    data object Empty : OnboardingUiState
}