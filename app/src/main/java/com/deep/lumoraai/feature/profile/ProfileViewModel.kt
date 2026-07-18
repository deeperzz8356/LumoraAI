package com.deep.lumoraai.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    private val generationRepository = com.deep.lumoraai.data.repository.GenerationRepository()

    init { load() }

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        val items = if (user != null) {
            val name = user.displayName ?: user.email?.substringBefore("@") ?: "Guest User"
            val email = user.email ?: "Anonymous Access"
            val plan = if (user.isAnonymous) "Free Tier (Guest)" else "Premium Account"
            listOf(name, email, plan)
        } else {
            listOf("Not Logged In", "Please register or sign in.")
        }
        
        uiState = ProfileUiState.Success(items, emptyList())
        
        if (user != null) {
            viewModelScope.launch {
                val historyResult = generationRepository.getHistory()
                val creditsResult = generationRepository.getCredits()
                
                val currentSuccess = uiState as? ProfileUiState.Success
                if (currentSuccess != null) {
                    var newState = currentSuccess
                    if (historyResult.isSuccess) {
                        newState = newState.copy(generations = historyResult.getOrDefault(emptyList()))
                    }
                    if (creditsResult.isSuccess) {
                        newState = newState.copy(credits = creditsResult.getOrDefault(0))
                    }
                    uiState = newState
                }
            }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}