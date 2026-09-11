package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.navigation.Screen

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(modifier = modifier) { binding ->
        binding.setupTopBar("Subscription", "Upgrade your studio", onBack = onBack)
        binding.resetContent()
        when (uiState) {
            SubscriptionUiState.Loading -> binding.content.addTextCard("Loading", "Checking available plans.")
            is SubscriptionUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is SubscriptionUiState.Success -> {
                binding.content.addTextCard(
                    title = if (uiState.isDeveloperMode) "Developer Pro" else "Lumora Pro",
                    subtitle = "Premium generation tools, higher limits and creator workflow perks."
                )
                uiState.purchaseMessage?.let {
                    binding.content.addTextCard("Purchase", it) { onClearMessage() }
                }
                binding.content.addSectionTitle("Plans")
                uiState.plans.forEach { plan ->
                    val selected = plan.id == uiState.selectedPlanId
                    binding.content.addActionRow(
                        title = if (selected) "Selected  ${plan.name}" else plan.name,
                        subtitle = "${plan.price} / ${plan.billingPeriod}\n${plan.features.joinToString("  |  ")}",
                        icon = if (plan.highlighted || selected) R.drawable.ic_lumora_star else R.drawable.ic_lumora_check,
                        onClick = { onSelectPlan(plan.id) }
                    )
                }
                binding.content.addPrimaryButton(
                    text = if (uiState.isPurchasing) "PROCESSING..." else "CONTINUE",
                    enabled = !uiState.isPurchasing,
                    onClick = onPurchase
                )
                binding.content.addActionRow("Restore Purchases", "Recover active subscriptions", R.drawable.ic_lumora_refresh, onRestore)
                binding.content.addActionRow("Need credits only?", "Open Reward Center", R.drawable.ic_lumora_star) {
                    onNavigate(Screen.Credits.route)
                }
            }
        }
    }
}
