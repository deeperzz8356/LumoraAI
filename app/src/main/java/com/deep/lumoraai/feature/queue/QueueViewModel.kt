package com.deep.lumoraai.feature.queue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.repository.FakeRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: FakeRepository = FakeRepository()
) : ViewModel() {
    var uiState: QueueUiState by mutableStateOf(QueueUiState.Loading)
        private set

    init {
        observeJobs()
    }

    private fun observeJobs() {
        viewModelScope.launch {
            GenerationRepository.activeJobs.collect { activeJobs ->
                uiState = if (activeJobs.isEmpty()) QueueUiState.Empty else QueueUiState.Success(activeJobs)
            }
        }
    }
}