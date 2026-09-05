package com.deep.lumoraai.feature.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.utils.CreditBalanceStore
import com.deep.lumoraai.core.utils.GuestIdentity
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    private val generationRepository = GenerationRepository()
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
            val savedProfile = ProfilePreferences.load(getApplication(), user)
            val name = savedProfile.fullName.ifBlank { GuestIdentity.displayName(getApplication(), user) }
            val email = "@${savedProfile.username.ifBlank { GuestIdentity.subtitle(getApplication(), user).removePrefix("@") }}"
            val plan = if (user.isAnonymous) "Free Tier (Guest)" else ""
            listOf(name, email, plan)
        } else {
            val savedProfile = ProfilePreferences.load(getApplication(), null)
            listOf(
                savedProfile.fullName.ifBlank { GuestIdentity.displayName(getApplication(), null) },
                "@${savedProfile.username.ifBlank { GuestIdentity.subtitle(getApplication(), null).removePrefix("@") }}",
                "Guest Preview"
            )
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
                val credits = LocalCreditBalance.maxWith(getApplication(), generationRepository.getCredits().getOrNull())
                val currentSuccess = uiState as? ProfileUiState.Success
                if (currentSuccess != null) {
                    uiState = currentSuccess.copy(credits = credits)
                }
            }
        }
    }
    
    fun refreshCredits() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            viewModelScope.launch {
                val credits = LocalCreditBalance.maxWith(getApplication(), generationRepository.getCredits().getOrNull())
                val currentSuccess = uiState as? ProfileUiState.Success
                if (currentSuccess != null) {
                    uiState = currentSuccess.copy(credits = credits)
                }
            }
        }
    }

    fun signOut() {
        // Clear the cached credit balance so the next signed-in user never sees
        // a previous account's number flash in the header.
        CreditBalanceStore.clear()
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
                CreditBalanceStore.clear()
                FirebaseAuth.getInstance().signOut()
                onDeleted()
            } catch (error: FirebaseAuthRecentLoginRequiredException) {
                Toast.makeText(
                    getApplication(),
                    "Please login again before deleting this account.",
                    Toast.LENGTH_LONG
                ).show()
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
