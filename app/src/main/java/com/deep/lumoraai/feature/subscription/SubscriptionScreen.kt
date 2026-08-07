package com.deep.lumoraai.feature.subscription

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.billing.BillingConstants
import com.deep.lumoraai.core.components.GradientButton
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.core.components.PolishedTabScaffold
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
fun SubscriptionScreen(
    uiState: SubscriptionViewState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    onSelectPeriod: (BillingPeriod) -> Unit,
    onPurchase: (Activity) -> Unit,
    onRestore: () -> Unit,
    onShowPaywall: () -> Unit,
    onDismissPaywall: () -> Unit,
    onShowCustomerCenter: () -> Unit,
    onDismissCustomerCenter: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.showCustomerCenter) {
        CustomerCenter(
            modifier = Modifier.fillMaxSize(),
            onDismiss = onDismissCustomerCenter,
        )
        return
    }

    if (uiState.showPaywall) {
        FullScreenPaywall(
            onDismiss = onDismissPaywall,
            onPurchaseCompleted = { onDismissPaywall(); onRefresh() },
            onRestoreCompleted = { onDismissPaywall(); onRefresh() },
        )
        return
    }

    PolishedTabScaffold(
        selectedRoute = "subscription",
        onNavigate = onNavigate,
        modifier = modifier,
    ) {
        when {
            uiState.isLoading -> Loading(modifier = Modifier.fillMaxSize())
            else -> SubscriptionContent(
                uiState = uiState,
                onSelectPeriod = onSelectPeriod,
                onPurchase = onPurchase,
                onRestore = onRestore,
                onShowPaywall = onShowPaywall,
                onShowCustomerCenter = onShowCustomerCenter,
            )
        }
    }
}

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
fun EntitlementGatedPaywallDialog(
    requiredEntitlementId: String = BillingConstants.ENTITLEMENT_ID,
    onPurchaseCompleted: () -> Unit = {},
    onRestoreCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    PaywallDialog(
        PaywallDialogOptions.Builder()
            .setRequiredEntitlementIdentifier(requiredEntitlementId)
            .setDismissRequest(onDismiss)
            .setListener(object : PaywallListener {
                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction,
                ) {
                    onPurchaseCompleted()
                }

                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    onRestoreCompleted()
                }

                override fun onPurchaseError(error: PurchasesError) {
                    // Listener keeps dialog open; caller can show snackbar if needed.
                }
            })
            .build()
    )
}

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
private fun FullScreenPaywall(
    onDismiss: () -> Unit,
    onPurchaseCompleted: () -> Unit,
    onRestoreCompleted: () -> Unit,
) {
    Paywall(
        options = PaywallOptions.Builder(dismissRequest = onDismiss)
            .setListener(object : PaywallListener {
                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction,
                ) {
                    onPurchaseCompleted()
                }

                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    onRestoreCompleted()
                }
            })
            .build()
    )
}

@Composable
private fun SubscriptionContent(
    uiState: SubscriptionViewState,
    onSelectPeriod: (BillingPeriod) -> Unit,
    onPurchase: (Activity) -> Unit,
    onRestore: () -> Unit,
    onShowPaywall: () -> Unit,
    onShowCustomerCenter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "MK Tech Media tech",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (uiState.isEntitled) {
                "You're subscribed. Manage or restore anytime."
            } else {
                "Unlock premium access with a monthly or yearly plan."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        StatusChip(isEntitled = uiState.isEntitled, customerInfo = uiState.customerInfo)

        if (!uiState.isEntitled) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = uiState.selectedBillingPeriod == BillingPeriod.MONTHLY,
                    onClick = { onSelectPeriod(BillingPeriod.MONTHLY) },
                    label = { Text("Monthly") }
                )
                FilterChip(
                    selected = uiState.selectedBillingPeriod == BillingPeriod.YEARLY,
                    onClick = { onSelectPeriod(BillingPeriod.YEARLY) },
                    label = { Text("Yearly") }
                )
            }

            PackageCard(
                title = if (uiState.selectedBillingPeriod == BillingPeriod.YEARLY) {
                    "Yearly"
                } else {
                    "Monthly"
                },
                pkg = uiState.selectedPackage,
                isPurchasing = uiState.isPurchasing,
                onPurchase = {
                    if (activity != null) onPurchase(activity)
                }
            )

            GradientButton(
                text = "View Paywall",
                onClick = onShowPaywall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        TextButton(onClick = onRestore, enabled = !uiState.isPurchasing) {
            Text("Restore purchases")
        }

        if (uiState.isEntitled) {
            GradientButton(
                text = "Manage Subscription",
                onClick = onShowCustomerCenter,
                modifier = Modifier.fillMaxWidth()
            )
        }

        uiState.error?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        uiState.statusMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusChip(isEntitled: Boolean, customerInfo: CustomerInfo?) {
    val entitlement = customerInfo?.entitlements?.get(BillingConstants.ENTITLEMENT_ID)
    val label = when {
        isEntitled && entitlement?.willRenew == false ->
            "Active — cancels on ${entitlement.expirationDate}"
        isEntitled -> "Active"
        else -> "Not subscribed"
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isEntitled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun PackageCard(
    title: String,
    pkg: Package?,
    isPurchasing: Boolean,
    onPurchase: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                pkg?.product?.price?.formatted ?: "Price unavailable",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Entitlement: ${BillingConstants.ENTITLEMENT_ID}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isPurchasing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Loading()
                }
            } else {
                GradientButton(
                    text = "Subscribe",
                    onClick = onPurchase,
                    enabled = pkg != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
