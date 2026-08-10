package com.deep.lumoraai.feature.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    private val generationRepository = GenerationRepository()
    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )

    init {
        load()
    }

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

        viewModelScope.launch {
            historyRepository.getHistory()
                .catch { /* keep empty generations */ }
                .collect { history ->
                    val current = uiState as? ProfileUiState.Success ?: return@collect
                    uiState = current.copy(generations = history)
                }
        }

        if (user != null) {
            viewModelScope.launch {
                val creditsResult = generationRepository.getCredits()
                val currentSuccess = uiState as? ProfileUiState.Success
                if (currentSuccess != null && creditsResult.isSuccess) {
                    uiState = currentSuccess.copy(credits = creditsResult.getOrDefault(0))
                }
            }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
