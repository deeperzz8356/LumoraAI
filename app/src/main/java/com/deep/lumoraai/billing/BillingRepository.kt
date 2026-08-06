package com.deep.lumoraai.billing

import android.app.Activity
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.logOutWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "BillingRepository"

@Singleton
class BillingRepository @Inject constructor() {

    fun isEntitled(customerInfo: CustomerInfo): Boolean =
        customerInfo.hasMkTechMediaTechEntitlement()

    suspend fun getCustomerInfo(): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = { error ->
                    logError("getCustomerInfo", error)
                    cont.resume(BillingResult.Error(error))
                },
                onSuccess = { info -> cont.resume(BillingResult.Success(info)) }
            )
        }

    suspend fun getOfferings(): BillingResult<Offerings> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.getOfferingsWith(
                onError = { error ->
                    logError("getOfferings", error)
                    cont.resume(BillingResult.Error(error))
                },
                onSuccess = { offerings -> cont.resume(BillingResult.Success(offerings)) }
            )
        }

    suspend fun purchase(activity: Activity, pkg: Package): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, pkg).build(),
                onError = { error, userCancelled ->
                    if (userCancelled) {
                        cont.resume(BillingResult.UserCancelled)
                    } else {
                        logError("purchase", error)
                        cont.resume(BillingResult.Error(error))
                    }
                },
                onSuccess = { _, customerInfo ->
                    cont.resume(BillingResult.Success(customerInfo))
                }
            )
        }

    suspend fun restorePurchases(): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error ->
                    logError("restorePurchases", error)
                    cont.resume(BillingResult.Error(error))
                },
                onSuccess = { info -> cont.resume(BillingResult.Success(info)) }
            )
        }

    suspend fun logIn(appUserId: String): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.logInWith(
                appUserId,
                onError = { error ->
                    logError("logIn", error)
                    cont.resume(BillingResult.Error(error))
                },
                onSuccess = { customerInfo, _ ->
                    cont.resume(BillingResult.Success(customerInfo))
                }
            )
        }

    suspend fun logOut(): BillingResult<CustomerInfo> =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.logOutWith(
                onError = { error ->
                    logError("logOut", error)
                    cont.resume(BillingResult.Error(error))
                },
                onSuccess = { info -> cont.resume(BillingResult.Success(info)) }
            )
        }

    fun addCustomerInfoListener(onUpdate: (CustomerInfo) -> Unit) {
        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { customerInfo -> onUpdate(customerInfo) }
    }

    fun clearCustomerInfoListener() {
        Purchases.sharedInstance.updatedCustomerInfoListener = null
    }

    private fun logError(operation: String, error: PurchasesError) {
        Log.e(
            TAG,
            "$operation failed: code=${error.code} message=${error.message} " +
                "underlying=${error.underlyingErrorMessage}"
        )
    }
}
