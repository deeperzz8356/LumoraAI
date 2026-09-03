package com.deep.lumoraai.feature.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.GuestIdentity
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )

    init {
        load()
    }

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        val isGuest = user == null || user.isAnonymous
        val items = if (user != null) {
            val name = GuestIdentity.displayName(getApplication(), user)
            val email = GuestIdentity.subtitle(getApplication(), user)
            val plan = if (user.isAnonymous) "Free Tier (Guest)" else "Premium Account"
            listOf(name, email, plan)
        } else {
            listOf(GuestIdentity.displayName(getApplication(), null), GuestIdentity.subtitle(getApplication(), null), "Guest Preview")
        }

        uiState = ProfileUiState.Success(items = items, generations = emptyList(), isGuest = isGuest)

        viewModelScope.launch {
            historyRepository.getHistory()
                .catch { /* keep empty generations */ }
                .collect { history ->
                    val current = uiState as? ProfileUiState.Success ?: return@collect
                    uiState = current.copy(generations = history)
                }
        }

        if (user != null) {
            viewModelScope.launch {
                val credits = if (appPreferences.isDeveloperModeEnabled()) {
                    GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
                } else {
                    generationRepository.getCredits().getOrDefault(0)
                }
                val currentSuccess = uiState as? ProfileUiState.Success
                if (currentSuccess != null) {
                    uiState = currentSuccess.copy(credits = credits)
                }
            }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            try {
                if (user != null && !user.isAnonymous) {
                    user.delete().await()
                }
                clearLocalData()
                FirebaseAuth.getInstance().signOut()
                onDeleted()
            } catch (error: Exception) {
                Toast.makeText(
                    getApplication(),
                    error.localizedMessage ?: "Account deletion failed. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun clearLocalData() {
        val database = LumoraDatabase.getInstance(getApplication())
        database.historyDao.clearAllHistory()
        database.notificationDao.clearAllNotifications()
    }
}
