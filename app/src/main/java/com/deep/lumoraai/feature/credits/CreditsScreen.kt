package com.deep.lumoraai.feature.credits

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate

@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    viewModel: CreditsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val ads = LocalAdsManager.current
    val adActivity = rememberCurrentActivity()
    val rewardAmount = LocalAdsConfigStore.current?.current?.rewardCreditsAmount ?: 2
    val onWatchAdForCredits: () -> Unit = {
        ads?.showRewarded(
            activity = adActivity,
            placement = AdPlacement.REWARD_CREDITS,
            onReward = { viewModel.addRewardedCredits(rewardAmount) },
            onClosed = {},
        )
    }

    LumoraXmlScreen(
        selectedTab = "credits",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        binding.setupTopBar("Reward Center", "Credits, rewards and bonuses", onBack = onBack)
        binding.resetContent()
        when (uiState) {
            CreditsUiState.Loading -> binding.content.addTextCard("Loading", "Reading your credit balance.")
            is CreditsUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is CreditsUiState.Success -> {
                val balance = if (uiState.isDeveloperMode || uiState.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
                    "Unlimited"
                } else {
                    uiState.credits.toString()
                }
                binding.content.addTextCard("Lumora Credits", balance)
                binding.content.addActionRow("Upgrade to Pro", "Unlock more credits and premium workflows", R.drawable.ic_lumora_star) {
                    onNavigate(Screen.Subscription.route)
                }
                binding.content.addActionRow("Start Creating", "Use credits for a new generation", R.drawable.ic_lumora_magic) {
                    onNavigate(Screen.Home.route)
                }
                uiState.purchaseMessage?.let { binding.content.addTextCard("Purchase", it) }
                uiState.rewardMessage?.let {
                    binding.content.addTextCard("Reward", it) {
                        viewModel.clearRewardMessage()
                    }
                }
                uiState.spinResult?.let {
                    binding.content.addTextCard("Spin Result", "${it.creditsAwarded} credits awarded") {
                        viewModel.clearSpinResult()
                    }
                }
                binding.content.addSectionTitle("Earn Credits")
                uiState.rewards.forEach { reward ->
                    binding.content.addActionRow(
                        title = reward.title,
                        subtitle = "${reward.subtitle}  |  ${reward.rewardLabel}  |  ${reward.actionLabel}",
                        icon = reward.iconRes,
                        onClick = if (reward.isAvailable && !uiState.isRewardBusy) {
                            { viewModel.claimReward(reward.id) }
                        } else {
                            null
                        }
                    )
                }
                if (ads != null && !uiState.isDeveloperMode) {
                    binding.content.addPrimaryButton("WATCH AD FOR $rewardAmount CREDITS", enabled = !uiState.isRewardBusy) {
                        onWatchAdForCredits()
                    }
                }
            }
        }
    }
}

private val CreditRewardUi.iconRes: Int
    get() = when (id) {
        "spin" -> R.drawable.ic_lumora_refresh
        "check_in" -> R.drawable.ic_lumora_check
        "share" -> R.drawable.ic_lumora_share
        else -> R.drawable.ic_lumora_star
    }
