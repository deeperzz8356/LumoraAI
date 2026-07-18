package com.deep.lumoraai.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set
        
    private val generationRepository = com.deep.lumoraai.data.repository.GenerationRepository()

    init { load() }

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "Creator"
        uiState = HomeUiState.Success(listOf("Welcome, $name!"))
        
        if (user != null) {
            viewModelScope.launch {
                val creditsResult = generationRepository.getCredits()
                if (creditsResult.isSuccess) {
                    val currentSuccess = uiState as? HomeUiState.Success
                    if (currentSuccess != null) {
                        uiState = currentSuccess.copy(credits = creditsResult.getOrDefault(0))
                    }
                }
            }
        }
    }
}