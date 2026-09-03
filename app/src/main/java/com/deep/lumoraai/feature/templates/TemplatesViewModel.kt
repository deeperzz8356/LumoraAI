package com.deep.lumoraai.feature.templates

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TemplatesViewModel(application: Application) : AndroidViewModel(application) {
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)

    var uiState: TemplatesUiState by mutableStateOf(
        TemplatesUiState.Success(
            imageTemplates = realImageTemplates,
            videoTemplates = realVideoTemplates,
        )
    )
        private set

    init {
        loadCredits()
    }

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                LocalCreditBalance.maxWith(getApplication(), generationRepository.getCredits().getOrNull())
            }
            val current = uiState
            if (current is TemplatesUiState.Success) {
                uiState = current.copy(credits = credits)
            }
        }
    }
}
