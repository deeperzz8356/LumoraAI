package com.deep.lumoraai.feature.aitools

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AIToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val generationRepository = GenerationRepository()

    var credits: Int by mutableStateOf(0)
        private set

    init {
        loadCredits()
    }

    private fun loadCredits() {
        viewModelScope.launch {
            credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else if (FirebaseAuth.getInstance().currentUser != null) {
                generationRepository.getCredits().getOrDefault(0)
            } else {
                0
            }
        }
    }
}
