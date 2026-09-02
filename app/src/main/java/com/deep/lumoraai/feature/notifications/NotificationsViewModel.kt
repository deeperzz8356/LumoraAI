package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.notification.NotificationManager
import com.deep.lumoraai.data.local.room.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {

    var uiState: NotificationsUiState by mutableStateOf(NotificationsUiState.Loading)
        private set

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
        observeUnreadCount()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                notificationManager.getAllNotifications().collect { notificationList ->
                    _notifications.value = notificationList
                    uiState = if (notificationList.isEmpty()) {
                        NotificationsUiState.Empty
                    } else {
                        NotificationsUiState.Success(notificationList.map { it.title })
                    }
                }
            } catch (e: Exception) {
                uiState = NotificationsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            notificationManager.getUnreadCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationManager.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationManager.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationManager.deleteNotification(notificationId)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationManager.clearAllNotifications()
        }
    }

    fun getNotificationsByType(type: String) {
        viewModelScope.launch {
            notificationManager.getNotificationsByType(type).collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }

    fun getUnreadNotifications() {
        viewModelScope.launch {
            notificationManager.getUnreadNotifications().collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }

    fun loadAllNotifications() {
        viewModelScope.launch {
            notificationManager.getAllNotifications().collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }
}