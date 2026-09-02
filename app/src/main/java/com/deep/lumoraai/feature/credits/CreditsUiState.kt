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
    ) : CreditsUiState
    data class Error(val message: String) : CreditsUiState
}

data class CreditRewardUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val rewardLabel: String,
    val actionLabel: String,
    val isAvailable: Boolean,
    val isAutomatic: Boolean = false,
)
