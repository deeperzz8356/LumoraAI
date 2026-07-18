package com.deep.lumoraai.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deep.lumoraai.data.repository.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    
    var uiState: SettingsUiState by mutableStateOf(SettingsUiState())
        private set

    init {
        load()
    }

    private fun load() {
        uiState = SettingsUiState(
            isDarkMode = repository.isDarkMode,
            notificationsEnabled = repository.notificationsEnabled,
            highQualityMode = repository.highQualityMode,
            selectedLanguage = repository.language
        )
    }

    fun toggleDarkMode(enabled: Boolean) {
        repository.isDarkMode = enabled
        uiState = uiState.copy(isDarkMode = enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        repository.notificationsEnabled = enabled
        uiState = uiState.copy(notificationsEnabled = enabled)
    }

    fun toggleHighQualityMode(enabled: Boolean) {
        repository.highQualityMode = enabled
        uiState = uiState.copy(highQualityMode = enabled)
    }

    fun setLanguage(language: String) {
        repository.language = language
        uiState = uiState.copy(selectedLanguage = language)
    }
}