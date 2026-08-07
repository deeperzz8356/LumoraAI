package com.deep.lumoraai.billing

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError

sealed class BillingResult<out T> {
    data class Success<T>(val data: T) : BillingResult<T>()
    data class Error(val error: PurchasesError) : BillingResult<Nothing>()
    data object UserCancelled : BillingResult<Nothing>()
}

fun BillingResult<*>.userMessage(): String? = when (this) {
    is BillingResult.Success -> null
    BillingResult.UserCancelled -> null
    is BillingResult.Error -> friendlyPurchasesMessage(error)
}

fun friendlyPurchasesMessage(error: PurchasesError): String {
    val code = error.code.name
    return when {
        code.contains("NETWORK", ignoreCase = true) ->
            "Check your connection and try again"
        code.contains("PRODUCT_NOT_AVAILABLE", ignoreCase = true) ->
            "This plan isn't available yet"
        code.contains("PURCHASE_NOT_ALLOWED", ignoreCase = true) ->
            "Purchases are disabled on this device"
        code.contains("STORE_PROBLEM", ignoreCase = true) ->
            "Play Store issue — try again later"
        code.contains("PURCHASE_CANCELLED", ignoreCase = true) ->
            "Purchase cancelled"
        else -> error.message.ifBlank { "Something went wrong with billing" }
    }
}

fun CustomerInfo.hasMkTechMediaTechEntitlement(): Boolean =
    entitlements[BillingConstants.ENTITLEMENT_ID]?.isActive == true
