package com.deep.lumoraai.data.billing

import com.android.billingclient.api.ProductDetails

sealed interface BillingResult {
    data object Launched : BillingResult
    data object PurchaseFinalized : BillingResult
    data class PurchaseReady(val purchaseToken: String, val productIds: List<String>) : BillingResult
    data object Cancelled : BillingResult
    data class Error(val message: String) : BillingResult
}

sealed interface BillingState {
    data object Disconnected : BillingState
    data object Connecting : BillingState
    data object Ready : BillingState
    data class Unavailable(val message: String) : BillingState
}

data class BillingProduct(
    val productId: String,
    val price: String,
    val offerToken: String?,
    val productType: String,
    internal val details: ProductDetails
)

data class RestoredPurchase(
    val productIds: List<String>,
    val purchaseToken: String,
    val isAcknowledged: Boolean
)
