package com.deep.lumoraai.feature.credits

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import kotlinx.coroutines.launch

class CreditsViewModel(application: Application) : AndroidViewModel(application) {
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)

    var uiState: CreditsUiState by mutableStateOf(CreditsUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CreditsUiState.Loading
        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (isDev) {
                uiState = CreditsUiState.Success(
                    credits = GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY,
                    isDeveloperMode = true
                )
                return@launch
            }
            val result = generationRepository.getCredits()
            if (result.isSuccess) {
                uiState = CreditsUiState.Success(
                    credits = result.getOrDefault(0),
                    isDeveloperMode = false
                )
            } else {
                uiState = CreditsUiState.Error("Failed to load credits")
            }
        }
    }

    fun buyCredits(amount: Int) {
        val currentState = uiState
        if (currentState is CreditsUiState.Success) {
            if (currentState.isDeveloperMode) {
                uiState = currentState.copy(credits = GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY)
                return
            }
            uiState = CreditsUiState.Loading
            viewModelScope.launch {
                val result = generationRepository.addCredits(amount)
                if (result.isSuccess) {
                    uiState = CreditsUiState.Success(
                        credits = currentState.credits + amount,
                        isDeveloperMode = false
                    )
                } else {
                    uiState = CreditsUiState.Error("Failed to add credits")
                }
            }
        }
    }
}
