package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

private val previewState = SubscriptionUiState.Success(
    plans = listOf(
        SubscriptionPlan(
            code = "free",
            name = "Free",
            priceUsd = 0.0,
            monthlyCredits = 0,
            videoCredits = 0,
            features = listOf("1 free image generation on signup", "Basic templates"),
            signupBonusCredits = 1,
        ),
        SubscriptionPlan(
            code = "starter",
            name = "Starter",
            priceUsd = 4.99,
            monthlyCredits = 50,
            videoCredits = 2,
            features = listOf("50 image credits", "2 video generations"),
            isPopular = true,
        )
    ),
    currentPlan = "free"
)

@Preview(name = "Subscription Light Preview", showBackground = true)
@Composable
fun SubscriptionLightPreview() {
    LumoraTheme(darkTheme = false) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Dark Preview", showBackground = true)
@Composable
fun SubscriptionDarkPreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun SubscriptionTabletPreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}

@Preview(name = "Subscription Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun SubscriptionLandscapePreview() {
    LumoraTheme(darkTheme = true) { SubscriptionScreen(uiState = previewState, onNext = {}) }
}