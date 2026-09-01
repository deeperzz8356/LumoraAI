package com.deep.lumoraai.feature.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.R
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.local.room.entity.HistoryEntity
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    var uiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    private val historyDao = LumoraDatabase.getInstance(application).historyDao
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private var latestCredits: Int? = null

    init {
        observeHomeData()
        loadCredits()
    }

    private fun observeHomeData() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "Creator"
        val planLabel = when {
            user == null -> "Free"
            user.isAnonymous -> "Guest"
            else -> "Premium"
        }

        viewModelScope.launch {
            historyDao.getAllHistory().collect { history ->
                val recent = history.take(2).map { it.toRecentItem() }
                val currentCredits = latestCredits ?: (uiState as? HomeUiState.Success)?.credits ?: 0
                uiState = HomeUiState.Success(
                    userName = name,
                    creationCount = history.size,
                    planLabel = planLabel,
                    recentItems = recent,
                    credits = currentCredits,
                )
            }
        }
    }

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return

        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            val credits = if (isDev) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                generationRepository.getCredits().getOrDefault(0)
            }
            latestCredits = credits
            val current = uiState
            if (current is HomeUiState.Success) {
                uiState = current.copy(credits = credits)
            }
        }
    }

    private fun HistoryEntity.toRecentItem(): HomeRecentItem =
        HomeRecentItem(
            id = id,
            title = title,
            timeLabel = formatRelativeTime(createdAt),
            mediaType = type,
            mediaUrl = mediaUrl,
            fallbackImageRes = when (type.uppercase()) {
                "VIDEO" -> R.drawable.style_digital
                else -> R.drawable.style_fantasy
            }
        )

    private fun formatRelativeTime(createdAt: String): String {
        val instant = runCatching { Instant.parse(createdAt) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            }.getOrNull()

        if (instant == null) return createdAt

        val minutes = ChronoUnit.MINUTES.between(instant, Instant.now())
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            minutes < 10080 -> "${minutes / 1440}d ago"
            else -> DateTimeFormatter.ofPattern("MMM d")
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }
    }
}
