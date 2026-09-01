package com.deep.lumoraai.feature.history

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private var latestCredits: Int? = null

    var uiState: HistoryUiState by mutableStateOf(HistoryUiState.Loading)
        private set

    init {
        load()
        loadCredits()
    }

    fun load() {
        viewModelScope.launch {
            uiState = HistoryUiState.Loading
            historyRepository.getHistory()
                .catch { e ->
                    uiState = HistoryUiState.Error(e.message ?: "Failed to load history")
                }
                .collect { items ->
                    uiState = if (items.isEmpty()) {
                        HistoryUiState.Empty(latestCredits ?: 0)
                    } else {
                        HistoryUiState.Success(items, latestCredits ?: 0)
                    }
                }
        }
    }

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                generationRepository.getCredits().getOrDefault(0)
            }
            latestCredits = credits
            val current = uiState
            uiState = when (current) {
                is HistoryUiState.Success -> current.copy(credits = credits)
                is HistoryUiState.Empty -> current.copy(credits = credits)
                else -> current
            }
        }
    }
}
