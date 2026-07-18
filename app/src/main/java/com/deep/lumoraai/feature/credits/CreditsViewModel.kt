package com.deep.lumoraai.feature.credits

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.GenerationRepository
import kotlinx.coroutines.launch

class CreditsViewModel : ViewModel() {
    private val generationRepository = GenerationRepository()
    
    var uiState: CreditsUiState by mutableStateOf(CreditsUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = CreditsUiState.Loading
        viewModelScope.launch {
            val result = generationRepository.getCredits()
            if (result.isSuccess) {
                uiState = CreditsUiState.Success(result.getOrDefault(0))
            } else {
                uiState = CreditsUiState.Error("Failed to load credits")
            }
        }
    }
    
    fun buyCredits(amount: Int) {
        val currentState = uiState
        if (currentState is CreditsUiState.Success) {
            uiState = CreditsUiState.Loading
            viewModelScope.launch {
                val result = generationRepository.addCredits(amount)
                if (result.isSuccess) {
                    uiState = CreditsUiState.Success(currentState.credits + amount)
                } else {
                    uiState = CreditsUiState.Error("Failed to add credits")
                }
            }
        }
    }
}