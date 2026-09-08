package com.deep.lumoraai.feature.credits

sealed interface CreditsUiState {
    data object Loading : CreditsUiState
    data class Success(
        val credits: Int,
        val isDeveloperMode: Boolean = false,
        val rewards: List<CreditRewardUi> = emptyList(),
        val rewardMessage: String? = null,
        val isRewardBusy: Boolean = false,
        val checkInDayIndex: Int = 0,
        val purchaseMessage: String? = null,
        val isPurchasing: Boolean = false,
        /**
         * Result of the most recent wheel spin, used to drive the wheel landing
         * animation and the in-dialog result text. Null until a spin resolves.
         */
        val spinResult: SpinResult? = null,
    ) : CreditsUiState
    data class Error(val message: String) : CreditsUiState
}

/**
 * The resolved outcome of a spin. [creditsAwarded] is the server-authoritative
 * amount (0 = "better luck"). [nonce] changes every spin so the UI reacts even
 * when the same amount is won twice in a row.
 */
data class SpinResult(
    val creditsAwarded: Int,
    val nonce: Long,
)

data class CreditRewardUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val rewardLabel: String,
    val actionLabel: String,
    val isAvailable: Boolean,
    val isAutomatic: Boolean = false,
)
