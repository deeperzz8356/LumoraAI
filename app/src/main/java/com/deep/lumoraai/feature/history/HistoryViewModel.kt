package com.deep.lumoraai.feature.history

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.repository.HistoryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )

    var uiState: HistoryUiState by mutableStateOf(HistoryUiState.Loading)
        private set

    init {
        load()
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
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(items)
                    }
                }
        }
    }
}
