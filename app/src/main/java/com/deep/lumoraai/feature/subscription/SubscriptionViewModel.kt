package com.deep.lumoraai.feature.subscription

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Payment gateway imports are disabled for now - payment processing disconnected
// Keeping monitoring and credit/billing logic intact
// import com.revenuecat.purchases.CustomerInfo
// import com.revenuecat.purchases.Offerings
// import com.revenuecat.purchases.Purchases
// import com.revenuecat.purchases.PurchasesError
// import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
// import com.revenuecat.purchases.interfaces.LogInCallback
// import com.revenuecat.purchases.interfaces.PurchaseCallback
// import com.revenuecat.purchases.models.PurchaseParams
// import com.revenuecat.purchases.models.StoreTransaction

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
        // Payment gateway initialization disabled - monitoring only mode
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // TODO: When payment gateway is re-enabled, initialize RevenueCat here
            // Purchases.sharedInstance.logIn(
            //     user.uid,
            //     object : LogInCallback {
            //         override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
            //             // User identified successfully in RevenueCat
            //         }
            //         override fun onError(error: PurchasesError) {
            //             // Ignore login error, user can still proceed with checkout
            //         }
            //     }
            // )
        }

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

    fun purchaseSelectedPlan(activity: Activity) {
        val current = uiState
        if (current !is SubscriptionUiState.Success) return

        uiState = current.copy(isPurchasing = true, purchaseMessage = null)
        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            val plan = current.plans.firstOrNull { it.id == current.selectedPlanId }
            
            // Payment gateway is currently disabled - operating in monitoring mode
            // The app tracks billing/credit usage but does not process payments
            if (isDev) {
                val message = "Developer mode: ${plan?.name ?: "Plan"} activated (no payment required)."
                uiState = current.copy(isPurchasing = false, purchaseMessage = message)
            } else {
                // Payment processing is disabled - show informational message
                val message = "Payment gateway is currently unavailable. " +
                    "Your billing and credit usage are being monitored. " +
                    "Payment processing will be enabled soon."
                uiState = current.copy(
                    isPurchasing = false,
                    purchaseMessage = message
                )
                
                // TODO: When payment gateway is re-enabled, implement:
                // Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                //     override fun onReceived(offerings: Offerings) {
                //         val pkg = offerings.current?.availablePackages?.firstOrNull {
                //             it.identifier == current.selectedPlanId || it.product.id == current.selectedPlanId
                //         }
                //         if (pkg != null) {
                //             val purchaseParams = PurchaseParams.Builder(activity, pkg).build()
                //             Purchases.sharedInstance.purchase(
                //                 purchaseParams,
                //                 object : PurchaseCallback {
                //                     override fun onCompleted(
                //                         storeTransaction: StoreTransaction,
                //                         customerInfo: CustomerInfo
                //                     ) {
                //                         uiState = current.copy(
                //                             isPurchasing = false,
                //                             purchaseMessage = "Purchase successful! Plan ${plan?.name} activated."
                //                         )
                //                     }
                //
                //                     override fun onError(error: PurchasesError, userCancelled: Boolean) {
                //                         uiState = current.copy(
                //                             isPurchasing = false,
                //                             purchaseMessage = if (userCancelled) "Purchase cancelled." else "Error: ${error.message}"
                //                         )
                //                     }
                //                 }
                //             )
                //         } else {
                //             uiState = current.copy(
                //                 isPurchasing = false,
                //                 purchaseMessage = "Selected plan is not configured in Play Store."
                //             )
                //         }
                //     }
                //
                //     override fun onError(error: PurchasesError) {
                //         uiState = current.copy(
                //             isPurchasing = false,
                //             purchaseMessage = "Failed to load store offerings: ${error.message}"
                //         )
                //     }
                // })
            }
        }
    }

    fun clearPurchaseMessage() {
        val current = uiState
        if (current is SubscriptionUiState.Success) {
            uiState = current.copy(purchaseMessage = null)
        }
    }
}
