package com.deep.lumoraai.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult as PlayBillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Collections

/**
 * Thin Play Billing boundary. Entitlements/credits must only be granted by a
 * backend after verifying purchaseToken with the Google Play Developer API.
 */
class BillingRepository(context: Context) : PurchasesUpdatedListener {
    companion object {
        const val PRO_MONTHLY = "pro_monthly"
        const val PRO_ANNUAL = "pro_annual"
        const val ELITE_PRO = "elite_pro"
        const val CREDITS_STARTER = "credits_starter"
        const val CREDITS_CREATOR = "credits_creator"
        const val CREDITS_STUDIO = "credits_studio"
        val SUBSCRIPTION_IDS = listOf(PRO_MONTHLY, PRO_ANNUAL, ELITE_PRO)
        val CREDIT_IDS = listOf(CREDITS_STARTER, CREDITS_CREATOR, CREDITS_STUDIO)
    }

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private val _state = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    val products: StateFlow<List<BillingProduct>> = _products.asStateFlow()

    private val _purchaseEvents = MutableStateFlow<BillingResult?>(null)
    val purchaseEvents: StateFlow<BillingResult?> = _purchaseEvents.asStateFlow()

    private val _restoredPurchases = MutableStateFlow<List<RestoredPurchase>>(emptyList())
    val restoredPurchases: StateFlow<List<RestoredPurchase>> = _restoredPurchases.asStateFlow()
    private val acknowledgedTokens = Collections.synchronizedSet(mutableSetOf<String>())

    fun connect() {
        if (client.isReady || _state.value == BillingState.Connecting) return
        _state.value = BillingState.Connecting
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: PlayBillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = BillingState.Ready
                    queryProducts()
                    restorePurchases()
                } else {
                    _state.value = BillingState.Unavailable(result.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = BillingState.Disconnected
            }
        })
    }

    fun disconnect() {
        if (client.isReady) client.endConnection()
        _state.value = BillingState.Disconnected
    }

    fun queryProducts() {
        if (!client.isReady) return
        val products = (SUBSCRIPTION_IDS to BillingClient.ProductType.SUBS).let { (ids, type) ->
            ids.map {
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            }
        } + CREDIT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP).build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _purchaseEvents.value = BillingResult.Error("Unable to load Google Play plans: ${result.debugMessage}")
                return@queryProductDetailsAsync
            }
            _products.value = details.productDetailsList.mapNotNull {
                it.toBillingProduct(if (it.productId in CREDIT_IDS) BillingClient.ProductType.INAPP else BillingClient.ProductType.SUBS)
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String): BillingResult {
        val product = _products.value.firstOrNull { it.productId == productId }
            ?: return BillingResult.Error("Selected plan is not configured in Google Play.")
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product.details)
            .apply { product.offerToken?.let(::setOfferToken) }
            .build()
        val result = client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(params))
                .build()
        )
        return result.toBillingResult()
    }

    fun restorePurchases() {
        if (!client.isReady) return
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _restoredPurchases.value = purchases.map {
                    RestoredPurchase(it.products, it.purchaseToken, it.isAcknowledged)
                }
                client.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
                ) { result, purchases ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach(::processPurchase)
                }
                purchases.forEach(::processPurchase)
            } else {
                _purchaseEvents.value = BillingResult.Error("Unable to restore purchases: ${result.debugMessage}")
            }
        }
    }

    fun clearPurchaseEvent() {
        _purchaseEvents.value = null
    }

    override fun onPurchasesUpdated(result: PlayBillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::processPurchase)
            BillingClient.BillingResponseCode.USER_CANCELED -> _purchaseEvents.value = BillingResult.Cancelled
            else -> _purchaseEvents.value = BillingResult.Error("Purchase failed: ${result.debugMessage}")
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseEvents.value = BillingResult.Error("Purchase is pending approval.")
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.isAcknowledged) {
            acknowledgedTokens += purchase.purchaseToken
            _purchaseEvents.value = BillingResult.PurchaseReady(purchase.purchaseToken, purchase.products)
            return
        }
        // Verification must happen before acknowledgement/finalization.
        _purchaseEvents.value = BillingResult.PurchaseReady(purchase.purchaseToken, purchase.products)
    }

    fun finalizePurchase(
        purchaseToken: String,
        consume: Boolean,
        onComplete: (BillingResult) -> Unit
    ) {
        if (!client.isReady) {
            onComplete(BillingResult.Error("Google Play billing is not connected."))
            return
        }
        // Play rejects a second acknowledgement. Restored purchases can
        // legitimately reach this path after the app has already finalized
        // them in an earlier session.
        if (!consume && acknowledgedTokens.contains(purchaseToken)) {
            onComplete(BillingResult.PurchaseFinalized)
            return
        }
        client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        ) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onComplete(BillingResult.Error("Purchase acknowledgement failed: ${result.debugMessage}"))
            } else if (consume) {
                client.consumeAsync(
                    com.android.billingclient.api.ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
                ) { consumeResult, _ ->
                    onComplete(
                        if (consumeResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            BillingResult.PurchaseFinalized
                        } else BillingResult.Error("Credit pack finalization failed: ${consumeResult.debugMessage}")
                    )
                }
            } else {
                onComplete(BillingResult.PurchaseFinalized)
            }
        }
    }

    private fun ProductDetails.toBillingProduct(type: String): BillingProduct? {
        if (type == BillingClient.ProductType.INAPP) {
            val price = oneTimePurchaseOfferDetails?.formattedPrice ?: return null
            return BillingProduct(productId, price, null, type, this)
        }
        val offer = subscriptionOfferDetails?.firstOrNull() ?: return null
        val price = offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice ?: return null
        return BillingProduct(productId, price, offer.offerToken, type, this)
    }

    private fun PlayBillingResult.toBillingResult(): BillingResult =
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> BillingResult.Launched
            BillingClient.BillingResponseCode.USER_CANCELED -> BillingResult.Cancelled
            else -> BillingResult.Error(debugMessage)
        }
}
