package com.deep.lumoraai.feature.subscription

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.billing.BillingProduct
import com.deep.lumoraai.data.billing.BillingRepository
import com.deep.lumoraai.data.billing.BillingResult
import com.deep.lumoraai.data.billing.BillingState
import com.deep.lumoraai.data.billing.RestoredPurchase
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val billing = BillingRepository(application)
    private val backend = GenerationRepository()

    private val defaultPlans = listOf(
        SubscriptionPlan("pro_monthly", "Pro Monthly", "$19.99", "per month", listOf("500 credits/month", "HD image generation", "Standard queue priority")),
        SubscriptionPlan("pro_annual", "Pro Annual", "$149.99", "per year", listOf("6,000 credits/year", "HD image & video", "Priority queue", "Save 37%"), true),
        SubscriptionPlan("elite_pro", "Elite Pro", "$499.99", "per year", listOf("Unlimited credits", "8K rendering", "Instant queue", "Concierge support"))
    )

    var uiState: SubscriptionUiState by mutableStateOf(SubscriptionUiState.Loading)
        private set

    init {
        billing.connect()
        viewModelScope.launch {
            combine(appPreferences.isDeveloperMode, appPreferences.isDevModeUnlocked) { isDev, _ -> isDev }
                .collect { isDev ->
                    val current = uiState
                    uiState = (current as? SubscriptionUiState.Success)?.copy(isDeveloperMode = isDev)
                        ?: SubscriptionUiState.Success(defaultPlans, "pro_annual", isDev)
                }
        }
        viewModelScope.launch {
            combine(billing.state, billing.products, billing.purchaseEvents, billing.restoredPurchases) {
                    state, products, event, restored -> BillingSnapshot(state, products, event, restored)
                }.collect { snapshot ->
                    val current = uiState as? SubscriptionUiState.Success ?: return@collect
                    val prices = snapshot.products.associateBy { it.productId }
                    val plans = current.plans.map { it.copy(price = prices[it.id]?.price ?: it.price) }
                    val message = when (val event = snapshot.event) {
                        null -> current.purchaseMessage
                        BillingResult.Launched -> current.purchaseMessage
                        is BillingResult.PurchaseReady -> "Purchase acknowledged. Entitlement verification is pending backend confirmation."
                        BillingResult.Cancelled -> "Purchase cancelled."
                        is BillingResult.Error -> event.message
                    }
                    uiState = current.copy(
                        plans = plans,
                        isPurchasing = snapshot.event == null && current.isPurchasing,
                        purchaseMessage = message,
                        billingState = snapshot.state,
                        restoredProductIds = snapshot.restored.flatMap { it.productIds }
                    )
                    if (snapshot.event != null) billing.clearPurchaseEvent()
                    val purchase = snapshot.event as? BillingResult.PurchaseReady
                    if (purchase != null && purchase.productIds.isNotEmpty()) {
                        viewModelScope.launch {
                            val productId = purchase.productIds.first()
                            val verified = backend.verifyGooglePlayPurchase(productId, purchase.purchaseToken)
                            val latest = uiState as? SubscriptionUiState.Success ?: return@launch
                            if (verified.isSuccess) {
                                billing.finalizePurchase(purchase.purchaseToken, consume = false) {
                                    uiState = latest.copy(
                                        isPurchasing = false,
                                        purchaseMessage = "Subscription verified and acknowledged."
                                    )
                                }
                            } else {
                                uiState = latest.copy(
                                    isPurchasing = false,
                                    purchaseMessage = "Subscription verification is pending. Access was not activated."
                                )
                            }
                        }
                    }
                }
        }
    }

    fun selectPlan(planId: String) {
        (uiState as? SubscriptionUiState.Success)?.let {
            uiState = it.copy(selectedPlanId = planId, purchaseMessage = null)
        }
    }

    fun purchaseSelectedPlan(activity: Activity) {
        val current = uiState as? SubscriptionUiState.Success ?: return
        uiState = current.copy(isPurchasing = true, purchaseMessage = null)
        viewModelScope.launch {
            if (appPreferences.isDeveloperModeEnabled()) {
                uiState = current.copy(isPurchasing = false, purchaseMessage = "Developer mode: ${current.selectedPlanId} activated (no payment required).")
                return@launch
            }
            when (val result = billing.launchPurchase(activity, current.selectedPlanId)) {
                BillingResult.Launched, is BillingResult.PurchaseReady -> Unit
                BillingResult.Cancelled -> uiState = current.copy(isPurchasing = false, purchaseMessage = "Purchase cancelled.")
                is BillingResult.Error -> uiState = current.copy(isPurchasing = false, purchaseMessage = result.message)
            }
        }
    }

    fun restorePurchases() = billing.restorePurchases()

    fun clearPurchaseMessage() {
        (uiState as? SubscriptionUiState.Success)?.let { uiState = it.copy(purchaseMessage = null) }
    }

    override fun onCleared() {
        billing.disconnect()
        super.onCleared()
    }

    private data class BillingSnapshot(
        val state: BillingState,
        val products: List<BillingProduct>,
        val event: BillingResult?,
        val restored: List<RestoredPurchase>
    )
}
