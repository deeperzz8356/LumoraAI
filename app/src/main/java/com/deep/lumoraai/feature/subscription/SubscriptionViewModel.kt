package com.deep.lumoraai.feature.subscription

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.billing.BillingConstants
import com.deep.lumoraai.billing.BillingRepository
import com.deep.lumoraai.billing.BillingResult
import com.deep.lumoraai.billing.userMessage
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PackageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionViewState(
    val isLoading: Boolean = true,
    val isEntitled: Boolean = false,
    val customerInfo: CustomerInfo? = null,
    val offerings: Offerings? = null,
    val currentOffering: Offering? = null,
    val monthlyPackage: Package? = null,
    val yearlyPackage: Package? = null,
    val selectedBillingPeriod: BillingPeriod = BillingPeriod.YEARLY,
    val isPurchasing: Boolean = false,
    val showPaywall: Boolean = false,
    val showCustomerCenter: Boolean = false,
    val error: String? = null,
    val statusMessage: String? = null,
) {
    val selectedPackage: Package?
        get() = when (selectedBillingPeriod) {
            BillingPeriod.MONTHLY -> monthlyPackage
            BillingPeriod.YEARLY -> yearlyPackage
        }

    val localizedPrice: String?
        get() = selectedPackage?.product?.price?.formatted
}

enum class BillingPeriod { MONTHLY, YEARLY }

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    var uiState by mutableStateOf(SubscriptionViewState())
        private set

    init {
        billingRepository.addCustomerInfoListener { info ->
            uiState = uiState.copy(
                isEntitled = billingRepository.isEntitled(info),
                customerInfo = info,
                showPaywall = if (billingRepository.isEntitled(info)) false else uiState.showPaywall,
            )
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            when (val infoResult = billingRepository.getCustomerInfo()) {
                is BillingResult.Success -> {
                    val offeringsResult = billingRepository.getOfferings()
                    val offerings = (offeringsResult as? BillingResult.Success)?.data
                    val offering = offerings?.current
                        ?: offerings?.getOffering(BillingConstants.OFFERING_DEFAULT)
                        ?: offerings?.all?.values?.firstOrNull()

                    val monthly = offering?.findPackage(
                        BillingConstants.PACKAGE_MONTHLY,
                        PackageType.MONTHLY,
                        BillingConstants.PRODUCT_MONTHLY,
                    )
                    val yearly = offering?.findPackage(
                        BillingConstants.PACKAGE_ANNUAL,
                        PackageType.ANNUAL,
                        BillingConstants.PRODUCT_YEARLY,
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        isEntitled = billingRepository.isEntitled(infoResult.data),
                        customerInfo = infoResult.data,
                        offerings = offerings,
                        currentOffering = offering,
                        monthlyPackage = monthly,
                        yearlyPackage = yearly,
                        error = (offeringsResult as? BillingResult.Error)?.let { it.userMessage() },
                    )
                }
                is BillingResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = infoResult.userMessage(),
                    )
                }
                BillingResult.UserCancelled -> {
                    uiState = uiState.copy(isLoading = false)
                }
            }
        }
    }

    fun selectBillingPeriod(period: BillingPeriod) {
        uiState = uiState.copy(selectedBillingPeriod = period, error = null)
    }

    fun purchase(activity: Activity) {
        val pkg = uiState.selectedPackage ?: run {
            uiState = uiState.copy(error = "No package available for this plan")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isPurchasing = true, error = null, statusMessage = null)
            when (val result = billingRepository.purchase(activity, pkg)) {
                is BillingResult.Success -> {
                    uiState = uiState.copy(
                        isPurchasing = false,
                        isEntitled = billingRepository.isEntitled(result.data),
                        customerInfo = result.data,
                        showPaywall = false,
                        statusMessage = "Subscription activated",
                    )
                }
                is BillingResult.Error -> {
                    uiState = uiState.copy(
                        isPurchasing = false,
                        error = result.userMessage(),
                    )
                }
                BillingResult.UserCancelled -> {
                    uiState = uiState.copy(isPurchasing = false)
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            uiState = uiState.copy(isPurchasing = true, error = null, statusMessage = null)
            when (val result = billingRepository.restorePurchases()) {
                is BillingResult.Success -> {
                    val entitled = billingRepository.isEntitled(result.data)
                    uiState = uiState.copy(
                        isPurchasing = false,
                        isEntitled = entitled,
                        customerInfo = result.data,
                        showPaywall = if (entitled) false else uiState.showPaywall,
                        statusMessage = if (entitled) {
                            "Purchases restored"
                        } else {
                            "No active subscription found"
                        },
                    )
                }
                is BillingResult.Error -> {
                    uiState = uiState.copy(
                        isPurchasing = false,
                        error = result.userMessage(),
                    )
                }
                BillingResult.UserCancelled -> {
                    uiState = uiState.copy(isPurchasing = false)
                }
            }
        }
    }

    fun showPaywall() {
        if (uiState.isEntitled) {
            uiState = uiState.copy(statusMessage = "You're already subscribed")
        } else {
            uiState = uiState.copy(showPaywall = true, error = null)
        }
    }

    fun dismissPaywall() {
        uiState = uiState.copy(showPaywall = false)
    }

    fun showCustomerCenter() {
        uiState = uiState.copy(showCustomerCenter = true)
    }

    fun dismissCustomerCenter() {
        uiState = uiState.copy(showCustomerCenter = false)
        refresh()
    }

    fun clearMessages() {
        uiState = uiState.copy(error = null, statusMessage = null)
    }

    override fun onCleared() {
        billingRepository.clearCustomerInfoListener()
        super.onCleared()
    }

    private fun Offering.findPackage(
        preferredId: String,
        type: PackageType,
        productId: String,
    ): Package? {
        availablePackages.firstOrNull { it.identifier == preferredId }?.let { return it }
        availablePackages.firstOrNull { it.packageType == type }?.let { return it }
        return availablePackages.firstOrNull {
            it.product.id.contains(productId, ignoreCase = true)
        }
    }
}
