package com.deep.lumoraai.feature.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.SettingsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val appPreferences = AppPreferencesRepository.getInstance(application)

    var uiState: SettingsUiState by mutableStateOf(SettingsUiState())
        private set

    init {
        load()
        viewModelScope.launch {
            combine(
                appPreferences.isDeveloperMode,
                appPreferences.isDevModeUnlocked
            ) { isDev, unlocked -> isDev to unlocked }
                .collect { (isDev, unlocked) ->
                    uiState = uiState.copy(
                        isDeveloperMode = isDev,
                        isDevModeUnlocked = unlocked
                    )
                }
        }
    }

    private fun load() {
        uiState = uiState.copy(
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

    fun onVersionTapped() {
        if (!BuildConfig.DEBUG) return
        val current = uiState
        val newCount = current.versionTapCount + 1
        uiState = current.copy(versionTapCount = newCount)
        if (newCount >= 7 && !current.isDevModeUnlocked) {
            viewModelScope.launch {
                appPreferences.unlockDevMode()
            }
        }
    }

    fun toggleDeveloperMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setDeveloperMode(enabled)
        }
    }
}
