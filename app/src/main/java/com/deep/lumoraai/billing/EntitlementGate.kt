package com.deep.lumoraai.billing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.deep.lumoraai.feature.subscription.EntitlementGatedPaywallDialog
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith

/**
 * Remembers whether the current user has the MK Tech Media tech entitlement.
 */
@Composable
fun rememberIsEntitled(): Boolean {
    var isEntitled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { isEntitled = false },
            onSuccess = { info ->
                isEntitled = info.hasMkTechMediaTechEntitlement()
            }
        )
    }

    return isEntitled
}

/**
 * Shows a RevenueCat PaywallDialog only when the user lacks the required entitlement.
 * Drop this into any gated premium screen.
 */
@Composable
fun RequireMkTechMediaTechEntitlement(
    content: @Composable (isEntitled: Boolean) -> Unit,
) {
    val isEntitled = rememberIsEntitled()
    content(isEntitled)
    if (!isEntitled) {
        EntitlementGatedPaywallDialog()
    }
}
