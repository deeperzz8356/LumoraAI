package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

private val previewPlans = listOf(
    SubscriptionPlan("pro_monthly", "Pro Monthly", "$19.99", "per month", listOf("500 credits/month")),
    SubscriptionPlan("pro_annual", "Pro Annual", "$149.99", "per year", listOf("6,000 credits/year"), highlighted = true)
)

private val previewState = SubscriptionUiState.Success(
    plans = previewPlans,
    selectedPlanId = "pro_annual",
    isDeveloperMode = false
)

@Preview(name = "Subscription Light Preview", showBackground = true)
@Composable
fun SubscriptionLightPreview() {
    LumoraTheme(darkTheme = false) {
        SubscriptionScreen(
            uiState = previewState,
            onSelectPlan = {},
            onPurchase = {},
            onClearMessage = {},
            onBack = {}
        )
    }
}

@Preview(name = "Subscription Dark Preview", showBackground = true)
@Composable
fun SubscriptionDarkPreview() {
    LumoraTheme(darkTheme = true) {
        SubscriptionScreen(
            uiState = previewState.copy(isDeveloperMode = true),
            onSelectPlan = {},
            onPurchase = {},
            onClearMessage = {},
            onBack = {}
        )
    }
}
