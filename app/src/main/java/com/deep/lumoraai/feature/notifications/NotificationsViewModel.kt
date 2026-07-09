package com.deep.lumoraai.feature.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.deep.lumoraai.data.repository.FakeRepository

class NotificationsViewModel(
    private val repository: FakeRepository = FakeRepository()
) : ViewModel() {
    var uiState: NotificationsUiState by mutableStateOf(NotificationsUiState.Loading)
        private set

    init { load() }

    fun load() {
        val items = when ("notifications") {
            "templates" -> repository.getTemplates().map { it.title }
            "history" -> repository.getHistory().map { it.title }
            "credits" -> repository.getCredits().map { "${it.label}: ${it.amount}" }
            "notifications" -> repository.getNotifications().map { it.title }
            "queue" -> repository.getQueue().map { it.title }
            "result" -> repository.getResults().map { it.title }
            "profile" -> listOf(repository.getProfile().name, repository.getProfile().plan, "${repository.getProfile().credits} credits")
            else -> listOf("Notifications ready", "Fake data only", "No Firebase, AI, Room, Retrofit, or network")
        }
        uiState = if (items.isEmpty()) NotificationsUiState.Empty else NotificationsUiState.Success(items)
    }
}