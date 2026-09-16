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
import org.json.JSONArray

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val billing = BillingRepository(application)
    private val backend = GenerationRepository()

    // ── Default hardcoded plans — overridden by Remote Config if provided ─────
    private val hardcodedPlans = listOf(
        SubscriptionPlan(
            id = "pro_monthly",
            name = "Pro Monthly",
            price = "$19.99",
            billingPeriod = "per month",
            features = listOf("500 credits/month", "HD image generation", "Standard queue priority"),
        ),
        SubscriptionPlan(
            id = "pro_annual",
            name = "Pro Annual",
            price = "$149.99",
            billingPeriod = "per year",
            features = listOf("6,000 credits/year", "HD image & video", "Priority queue", "Save 37%"),
            highlighted = true,
        ),
        SubscriptionPlan(
            id = "elite_pro",
            name = "Elite Pro",
            price = "$499.99",
            billingPeriod = "per year",
            features = listOf("Unlimited credits", "8K rendering", "Instant queue", "Concierge support"),
        ),
    )

    // Whether Remote Config has been applied. Until it is, we start Loading so
    // the screen waits briefly rather than flashing hardcoded data.
    private var remoteConfigApplied = false
    private var billingPricesByProductId: Map<String, String> = emptyMap()

    var uiState: SubscriptionUiState by mutableStateOf(SubscriptionUiState.Loading)
        private set

    init {
        billing.connect()

        viewModelScope.launch {
            combine(appPreferences.isDeveloperMode, appPreferences.isDevModeUnlocked) { isDev, _ -> isDev }
                .collect { isDev ->
                    val current = uiState
                    // Don't touch Disabled state — it was set by Remote Config.
                    if (current is SubscriptionUiState.Disabled) return@collect
                    // Don't overwrite with Loading-derived state until RC has been applied.
                    if (!remoteConfigApplied && current is SubscriptionUiState.Loading) {
                        // Apply hardcoded plans immediately so the screen isn't blank forever
                        // if Remote Config never arrives (e.g. no internet).
                        uiState = SubscriptionUiState.Success(
                            plans = hardcodedPlans,
                            selectedPlanId = defaultSelectedId(hardcodedPlans),
                            isDeveloperMode = isDev,
                        )
                        return@collect
                    }
                    uiState = (current as? SubscriptionUiState.Success)?.copy(isDeveloperMode = isDev)
                        ?: return@collect
                }
        }

        viewModelScope.launch {
            combine(
                billing.state, billing.products, billing.purchaseEvents, billing.restoredPurchases,
            ) { state, products, event, restored ->
                BillingSnapshot(state, products, event, restored)
            }.collect { snapshot ->
                val current = uiState as? SubscriptionUiState.Success ?: return@collect
                billingPricesByProductId = snapshot.products.associate { it.productId to it.price }
                val plans = current.plans.withBillingPrices()
                val message = when (val event = snapshot.event) {
                    null -> current.purchaseMessage
                    BillingResult.Launched -> current.purchaseMessage
                    BillingResult.PurchaseFinalized -> current.purchaseMessage
                    is BillingResult.PurchaseReady -> "Purchase acknowledged. Entitlement verification is pending."
                    BillingResult.Cancelled -> "Purchase cancelled."
                    is BillingResult.Error -> event.message
                }
                uiState = current.copy(
                    plans = plans,
                    isPurchasing = snapshot.event == null && current.isPurchasing,
                    purchaseMessage = message,
                    billingState = snapshot.state,
                    restoredProductIds = snapshot.restored.flatMap { it.productIds },
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
                                    purchaseMessage = "Subscription verified and acknowledged.",
                                )
                            }
                        } else {
                            uiState = latest.copy(
                                isPurchasing = false,
                                purchaseMessage = "Subscription verification is pending. Access was not activated.",
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Called from [SubscriptionRoute] once [LocalAdsConfigStore] resolves.
     * Applies the Remote Config subscription toggle and optional plan override.
     * Safe to call multiple times — subsequent calls with the same values are no-ops.
     */
    fun applyRemoteConfig(subscriptionEnabled: Boolean, plansJson: String) {
        remoteConfigApplied = true
        if (!subscriptionEnabled) {
            uiState = SubscriptionUiState.Disabled
            return
        }
        val remotePlans = parsePlans(plansJson)
        val plans = remotePlans.ifEmpty { hardcodedPlans }.withBillingPrices()
        val current = uiState
        when (current) {
            is SubscriptionUiState.Loading, SubscriptionUiState.Disabled -> {
                // RC arrived before the dev-mode flow set a Success state; bootstrap now.
                uiState = SubscriptionUiState.Success(
                    plans = plans,
                    selectedPlanId = defaultSelectedId(plans),
                    isDeveloperMode = false,
                )
            }
            is SubscriptionUiState.Success -> {
                // Refresh plan list while keeping selection and other state intact.
                val updatedSelected = plans.firstOrNull { it.id == current.selectedPlanId }?.id
                    ?: defaultSelectedId(plans)
                uiState = current.copy(plans = plans, selectedPlanId = updatedSelected)
            }
            else -> Unit // Disabled — don't touch.
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
                uiState = current.copy(
                    isPurchasing = false,
                    purchaseMessage = "Developer mode: ${current.selectedPlanId} activated (no payment required).",
                )
                return@launch
            }
            when (val result = billing.launchPurchase(activity, current.selectedPlanId)) {
                BillingResult.Launched, is BillingResult.PurchaseReady, is BillingResult.PurchaseFinalized -> Unit
                BillingResult.Cancelled -> uiState = current.copy(isPurchasing = false, purchaseMessage = "Purchase cancelled.")
                is BillingResult.Error -> uiState = current.copy(isPurchasing = false, purchaseMessage = result.message)
            }
        }
    }

    fun restorePurchases() = billing.restorePurchases()

    fun clearPurchaseMessage() {
        (uiState as? SubscriptionUiState.Success)?.let { uiState = it.copy(purchaseMessage = null) }
    }

    fun clearPurchasingState() {
        (uiState as? SubscriptionUiState.Success)?.let {
            if (it.isPurchasing) uiState = it.copy(isPurchasing = false)
        }
    }

    override fun onCleared() {
        billing.disconnect()
        super.onCleared()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Parses a JSON array of plan objects from Remote Config.
     * Returns an empty list on any failure — caller falls back to hardcoded plans.
     *
     * Expected element:
     * { "id":"pro_monthly","name":"Pro Monthly","price":"$19.99",
     *   "billing_period":"per month","highlighted":false,
     *   "features":["500 credits/month","HD image"] }
     */
    private fun parsePlans(json: String): List<SubscriptionPlan> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = obj.optString("id").trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val featuresArr = obj.optJSONArray("features")
                val features = if (featuresArr != null) {
                    (0 until featuresArr.length())
                        .map { featuresArr.optString(it) }
                        .filter { it.isNotBlank() }
                } else emptyList()
                SubscriptionPlan(
                    id = id,
                    name = obj.optString("name", id),
                    price = obj.optString("price", ""),
                    billingPeriod = obj.optString("billing_period", ""),
                    features = features,
                    highlighted = obj.optBoolean("highlighted", false),
                )
            }
        }.getOrElse { emptyList() }
    }

    private fun defaultSelectedId(plans: List<SubscriptionPlan>): String =
        plans.firstOrNull { it.highlighted }?.id ?: plans.firstOrNull()?.id ?: "pro_annual"

    private fun List<SubscriptionPlan>.withBillingPrices(): List<SubscriptionPlan> =
        map { plan -> plan.copy(price = billingPricesByProductId[plan.id] ?: plan.price) }

    private data class BillingSnapshot(
        val state: BillingState,
        val products: List<BillingProduct>,
        val event: BillingResult?,
        val restored: List<RestoredPurchase>,
    )
}
