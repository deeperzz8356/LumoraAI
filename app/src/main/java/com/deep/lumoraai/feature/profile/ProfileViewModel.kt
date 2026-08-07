package com.deep.lumoraai.feature.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.billing.BillingConstants
import com.google.firebase.auth.FirebaseAuth
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.logOutWith
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ProfileViewModel : ViewModel() {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    private val generationRepository = com.deep.lumoraai.data.repository.GenerationRepository()

    init { load() }

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            uiState = ProfileUiState.Success(
                listOf("Not Logged In", "Please register or sign in."),
                emptyList()
            )
            return
        }

        val name = user.displayName ?: user.email?.substringBefore("@") ?: "Guest User"
        val email = user.email ?: "Anonymous Access"
        uiState = ProfileUiState.Success(listOf(name, email, "Checking subscription…"), emptyList())

        viewModelScope.launch {
            val historyResult = generationRepository.getHistory()
            val creditsResult = generationRepository.getCredits()
            val planLabel = resolvePlanLabel(user.isAnonymous)

            var newState = ProfileUiState.Success(
                items = listOf(name, email, planLabel),
                generations = emptyList(),
            )
            if (historyResult.isSuccess) {
                newState = newState.copy(generations = historyResult.getOrDefault(emptyList()))
            }
            if (creditsResult.isSuccess) {
                newState = newState.copy(credits = creditsResult.getOrDefault(0))
            }
            uiState = newState
        }
    }

    fun signOut() {
        try {
            Purchases.sharedInstance.logOutWith(
                onError = { error -> Log.e(TAG, "RevenueCat logout failed: ${error.message}") },
                onSuccess = { Log.d(TAG, "RevenueCat logout complete") }
            )
        } catch (e: Exception) {
            Log.e(TAG, "RevenueCat logout threw", e)
        }
        FirebaseAuth.getInstance().signOut()
    }

    private suspend fun resolvePlanLabel(isAnonymous: Boolean): String =
        suspendCancellableCoroutine { cont ->
            try {
                Purchases.sharedInstance.getCustomerInfoWith(
                    onError = {
                        cont.resume(if (isAnonymous) "Free Tier (Guest)" else "Free Tier")
                    },
                    onSuccess = { info ->
                        val entitled =
                            info.entitlements[BillingConstants.ENTITLEMENT_ID]?.isActive == true
                        cont.resume(
                            when {
                                entitled -> "MK Tech Media tech"
                                isAnonymous -> "Free Tier (Guest)"
                                else -> "Free Tier"
                            }
                        )
                    }
                )
            } catch (_: Exception) {
                cont.resume(if (isAnonymous) "Free Tier (Guest)" else "Free Tier")
            }
        }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
