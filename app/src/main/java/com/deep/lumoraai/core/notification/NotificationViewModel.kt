package com.deep.lumoraai.core.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.notification.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that provides unread notification count for global access
 * Used by AppToolbar and other screens to display notification badge
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        observeUnreadCount()
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            notificationManager.getUnreadCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }
}
