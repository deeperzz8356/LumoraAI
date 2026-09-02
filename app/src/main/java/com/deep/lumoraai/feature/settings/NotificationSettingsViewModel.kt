package com.deep.lumoraai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.notification.OneSignalManager
import com.deep.lumoraai.data.repository.NotificationPreferencesRepository
import com.deep.lumoraai.domain.model.NotificationFrequencies
import com.deep.lumoraai.domain.model.NotificationPreferences
import com.deep.lumoraai.domain.model.NotificationSounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val oneSignalManager: OneSignalManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    val notificationPreferences: Flow<NotificationPreferences> = preferencesRepository.getNotificationPreferences()

    init {
        loadPreferences()
    }

    /**
     * Load notification preferences
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                preferencesRepository.getNotificationPreferences().collect { prefs ->
                    _uiState.value = _uiState.value.copy(
                        preferences = prefs,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Toggle all notifications
     */
    fun toggleNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setNotificationsEnabled(enabled)
                oneSignalManager.setNotificationSubscription(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle task completion notifications
     */
    fun toggleTaskCompletionNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setTaskCompletionNotifications(enabled)
                if (enabled) {
                    oneSignalManager.addTags(mapOf("notifications_task_completion" to "true"))
                } else {
                    oneSignalManager.removeTags(listOf("notifications_task_completion"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle engagement notifications
     */
    fun toggleEngagementNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setEngagementNotifications(enabled)
                if (enabled) {
                    oneSignalManager.addTags(mapOf("notifications_engagement" to "true"))
                } else {
                    oneSignalManager.removeTags(listOf("notifications_engagement"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle feature announcement notifications
     */
    fun toggleFeatureAnnouncementNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setFeatureAnnouncementNotifications(enabled)
                if (enabled) {
                    oneSignalManager.addTags(mapOf("notifications_feature" to "true"))
                } else {
                    oneSignalManager.removeTags(listOf("notifications_feature"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle sound
     */
    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setSoundEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle vibration
     */
    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setVibrationEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Set notification sound
     */
    fun setNotificationSound(sound: String) {
        if (!NotificationSounds.isValid(sound)) return
        viewModelScope.launch {
            try {
                preferencesRepository.setNotificationSound(sound)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Toggle Do Not Disturb
     */
    fun toggleDoNotDisturb(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDoNotDisturbEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Set Do Not Disturb start hour
     */
    fun setDoNotDisturbStartHour(hour: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDoNotDisturbStartHour(hour)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Set Do Not Disturb end hour
     */
    fun setDoNotDisturbEndHour(hour: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDoNotDisturbEndHour(hour)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Set notification frequency
     */
    fun setNotificationFrequency(frequency: String) {
        if (!NotificationFrequencies.isValid(frequency)) return
        viewModelScope.launch {
            try {
                preferencesRepository.setNotificationFrequency(frequency)
                oneSignalManager.addTags(mapOf("notification_frequency" to frequency))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Set max notifications per day
     */
    fun setMaxNotificationsPerDay(max: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setMaxNotificationsPerDay(max)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Reset all preferences to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                preferencesRepository.resetToDefaults()
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
