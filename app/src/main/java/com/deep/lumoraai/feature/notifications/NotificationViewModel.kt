package com.deep.lumoraai.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.notification.NotificationService
import com.deep.lumoraai.domain.model.Notification
import com.deep.lumoraai.domain.model.NotificationPriority
import com.deep.lumoraai.domain.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val selectedNotification: Notification? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterType: NotificationType? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationService: NotificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    /**
     * Load all notifications
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                notificationService.getAllNotifications().collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
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
     * Load unread notification count
     */
    private fun loadUnreadCount() {
        viewModelScope.launch {
            notificationService.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
    }

    /**
     * Filter notifications by type
     */
    fun filterByType(type: NotificationType?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                filterType = type,
                isLoading = true
            )
            try {
                if (type == null) {
                    notificationService.getAllNotifications().collect { notifications ->
                        _uiState.value = _uiState.value.copy(
                            notifications = notifications,
                            isLoading = false
                        )
                    }
                } else {
                    notificationService.getNotificationsByType(type).collect { notifications ->
                        _uiState.value = _uiState.value.copy(
                            notifications = notifications,
                            isLoading = false
                        )
                    }
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
     * Get notifications by priority
     */
    fun filterByPriority(priority: NotificationPriority) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                notificationService.getNotificationsByPriority(priority).collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false
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
     * Get unread notifications
     */
    fun loadUnreadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                notificationService.getUnreadNotifications().collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false
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
     * Select a notification to view details
     */
    fun selectNotification(notification: Notification) {
        _uiState.value = _uiState.value.copy(selectedNotification = notification)
        // Mark as read when viewed
        if (!notification.isRead) {
            notificationService.markNotificationAsRead(notification.id)
        }
    }

    /**
     * Clear selected notification
     */
    fun clearSelectedNotification() {
        _uiState.value = _uiState.value.copy(selectedNotification = null)
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        notificationService.markNotificationAsRead(notificationId)
    }

    /**
     * Mark all as read
     */
    fun markAllAsRead() {
        notificationService.markAllNotificationsAsRead()
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        notificationService.deleteNotification(notificationId)
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        notificationService.clearAllNotifications()
        _uiState.value = _uiState.value.copy(
            notifications = emptyList(),
            unreadCount = 0
        )
    }

    /**
     * Cleanup old notifications
     */
    fun cleanupOldNotifications(daysOld: Int = 30) {
        notificationService.deleteOldNotifications(daysOld)
    }
}
