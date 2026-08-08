package com.deep.lumoraai.feature.subscription

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val appPreferences = AppPreferencesRepository.getInstance(application)

    private val plans = listOf(
        SubscriptionPlan(
            id = "pro_monthly",
            name = "Pro Monthly",
            price = "$19.99",
            billingPeriod = "per month",
            features = listOf("500 credits/month", "HD image generation", "Standard queue priority")
        ),
        SubscriptionPlan(
            id = "pro_annual",
            name = "Pro Annual",
            price = "$149.99",
            billingPeriod = "per year",
            features = listOf("6,000 credits/year", "HD image & video", "Priority queue", "Save 37%"),
            highlighted = true
        ),
        SubscriptionPlan(
            id = "elite_pro",
            name = "Elite Pro",
            price = "$499.99",
            billingPeriod = "per year",
            features = listOf("Unlimited credits", "8K rendering", "Instant queue", "Concierge support")
        )
    )

    var uiState: SubscriptionUiState by mutableStateOf(SubscriptionUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            combine(
                appPreferences.isDeveloperMode,
                appPreferences.isDevModeUnlocked
            ) { isDev, _ -> isDev }
                .collect { isDev ->
                    val current = uiState
                    if (current is SubscriptionUiState.Success) {
                        uiState = current.copy(isDeveloperMode = isDev)
                    } else {
                        uiState = SubscriptionUiState.Success(
                            plans = plans,
                            selectedPlanId = plans.first { it.highlighted }.id,
                            isDeveloperMode = isDev
                        )
                    }
                }
        }
    }

    fun selectPlan(planId: String) {
        val current = uiState
        if (current is SubscriptionUiState.Success) {
            uiState = current.copy(selectedPlanId = planId, purchaseMessage = null)
        }
    }

    fun purchaseSelectedPlan() {
        val current = uiState
        if (current !is SubscriptionUiState.Success) return

        uiState = current.copy(isPurchasing = true, purchaseMessage = null)
        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            val plan = current.plans.firstOrNull { it.id == current.selectedPlanId }
            val message = if (isDev) {
                "Developer mode: ${plan?.name ?: "Plan"} activated (no payment required)."
            } else {
                "Payment integration coming soon. Your selection: ${plan?.name ?: "Plan"}."
            }
            uiState = current.copy(isPurchasing = false, purchaseMessage = message)
        }
    }

    fun clearPurchaseMessage() {
        val current = uiState
        if (current is SubscriptionUiState.Success) {
            uiState = current.copy(purchaseMessage = null)
        }
    }
}
