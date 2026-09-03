package com.deep.lumoraai.feature.credits

import android.app.Application
import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.billing.BillingRepository
import com.deep.lumoraai.data.billing.BillingResult
import kotlinx.coroutines.flow.collect
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.launch

class CreditsViewModel(application: Application) : AndroidViewModel(application) {
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val rewardPrefs = application.getSharedPreferences("lumora_credit_rewards", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val billing = BillingRepository(application)

    var uiState: CreditsUiState by mutableStateOf(CreditsUiState.Loading)
        private set

    init {
        billing.connect()
        load()
        viewModelScope.launch {
            billing.purchaseEvents.collect { event ->
                if (event is BillingResult.Cancelled || event is BillingResult.Error) {
                    (uiState as? CreditsUiState.Success)?.let {
                        uiState = it.copy(isPurchasing = false, purchaseMessage = when (event) {
                            BillingResult.Cancelled -> "Purchase cancelled."
                            is BillingResult.Error -> event.message
                            else -> null
                        })
                    }
                    billing.clearPurchaseEvent()
                    return@collect
                }
                val purchase = event as? BillingResult.PurchaseReady ?: return@collect
                val productId = purchase.productIds.firstOrNull { it in BillingRepository.CREDIT_IDS }
                if (productId != null) {
                    val result = generationRepository.verifyGooglePlayPurchase(productId, purchase.purchaseToken)
                    val current = uiState as? CreditsUiState.Success
                    if (current != null) {
                        uiState = if (result.isSuccess) {
                            current.copy(credits = result.getOrThrow(), isPurchasing = false,
                                purchaseMessage = "Credit pack verified and added to your balance.")
                        } else current.copy(isPurchasing = false,
                            purchaseMessage = "Payment received, but verification is pending. Credits were not added on this device.")
                    }
                    billing.finalizePurchase(purchase.purchaseToken, consume = true) {
                        billing.clearPurchaseEvent()
                    }
                }
            }
        }
    }

    fun load() {
        uiState = CreditsUiState.Loading
        viewModelScope.launch {
            val isDev = appPreferences.isDeveloperModeEnabled()
            if (isDev) {
                uiState = CreditsUiState.Success(
                    credits = GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY,
                    isDeveloperMode = true,
                    rewards = buildRewardTasks(isDeveloperMode = true),
                    checkInDayIndex = checkInIndex()
                )
                return@launch
            }
            val result = generationRepository.getCredits()
            val automaticBonus = grantAutomaticRewards()
            val backendCredits = result.getOrNull()
            val localBalance = localRewardBalance()
            val visibleCredits = maxOf((backendCredits ?: 0) + automaticBonus, localBalance)
            uiState = CreditsUiState.Success(
                credits = visibleCredits,
                isDeveloperMode = false,
                rewards = buildRewardTasks(isDeveloperMode = false),
                rewardMessage = when {
                    automaticBonus > 0 && backendCredits != null -> "+$automaticBonus daily/account credits added automatically."
                    else -> null
                },
                checkInDayIndex = checkInIndex()
            )
        }
    }

    fun buyCredits(amount: Int, activity: Activity?) {
        val currentState = uiState
        if (currentState is CreditsUiState.Success) {
            if (currentState.isDeveloperMode) {
                uiState = currentState.copy(
                    credits = GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY,
                    rewards = buildRewardTasks(isDeveloperMode = true),
                    checkInDayIndex = checkInIndex()
                )
                return
            }
            val productId = when (amount) {
                50 -> BillingRepository.CREDITS_STARTER
                150 -> BillingRepository.CREDITS_CREATOR
                500 -> BillingRepository.CREDITS_STUDIO
                else -> null
            }
            if (activity == null || productId == null) return
            uiState = currentState.copy(isPurchasing = true, purchaseMessage = null)
            viewModelScope.launch {
                when (val result = billing.launchPurchase(activity, productId)) {
                    BillingResult.Launched -> Unit
                    BillingResult.PurchaseFinalized -> Unit
                    BillingResult.Cancelled -> uiState = currentState.copy(isPurchasing = false, purchaseMessage = "Purchase cancelled.")
                    is BillingResult.Error -> uiState = currentState.copy(isPurchasing = false, purchaseMessage = result.message)
                    is BillingResult.PurchaseReady -> Unit
                }
            }
        }

    }

    fun clearPurchaseMessage() {
        (uiState as? CreditsUiState.Success)?.let { uiState = it.copy(purchaseMessage = null) }
    }

    override fun onCleared() {
        billing.disconnect()
        super.onCleared()
    }

    fun claimReward(rewardId: String) {
        val currentState = uiState as? CreditsUiState.Success ?: return
        if (currentState.isDeveloperMode || currentState.isRewardBusy) return
        val reward = currentState.rewards.firstOrNull { it.id == rewardId } ?: return
        if (!reward.isAvailable) return

        val amount = rewardAmountFor(rewardId)
        val message = if (rewardId == REWARD_SPIN && amount == 0) {
            "Better luck next time. Your free weekly spin was used."
        } else {
            "+$amount credits added from ${reward.title}."
        }

        uiState = currentState.copy(isRewardBusy = true, rewardMessage = "Adding reward credits...")
        viewModelScope.launch {
            val result = if (amount > 0) generationRepository.addCredits(amount) else Result.success(currentState.credits)
            markRewardClaimed(rewardId)
            addLocalRewardBalance(amount)
            val syncedCredits = result.getOrNull() ?: if (amount > 0) generationRepository.getCredits().getOrNull() else currentState.credits
            val expectedCredits = currentState.credits + amount
            val newCredits = maxOf(syncedCredits ?: expectedCredits, expectedCredits, localRewardBalance())
            uiState = currentState.copy(
                credits = newCredits,
                rewards = buildRewardTasks(isDeveloperMode = false),
                rewardMessage = message,
                isRewardBusy = false,
                checkInDayIndex = checkInIndex()
            )
        }
    }

    fun clearRewardMessage() {
        val currentState = uiState as? CreditsUiState.Success ?: return
        uiState = currentState.copy(rewardMessage = null)
    }

    private fun buildRewardTasks(isDeveloperMode: Boolean): List<CreditRewardUi> {
        val today = todayKey()
        val weeklySpinAvailable = rewardPrefs.getString(KEY_SPIN_WEEK, "") != weekKey()
        val checkInAvailable = rewardPrefs.getString(KEY_CHECK_IN_DATE, "") != today
        val dailyResetClaimed = rewardPrefs.getString(KEY_DAILY_RESET_DATE, "") == today
        val signupClaimed = rewardPrefs.getBoolean(KEY_SIGNUP_CLAIMED, false)
        val emailLoginClaimed = rewardPrefs.getBoolean(KEY_EMAIL_LOGIN_CLAIMED, false)
        val checkInAmount = WEEKLY_CHECK_IN_REWARDS[checkInIndex()]

        fun available(value: Boolean) = value && !isDeveloperMode
        fun label(action: String, value: Boolean) = when {
            isDeveloperMode -> "Unlimited"
            value -> action
            else -> "Claimed"
        }
        fun autoLabel(claimed: Boolean) = when {
            isDeveloperMode -> "Unlimited"
            claimed -> "Added"
            else -> "Auto"
        }

        return listOf(
            CreditRewardUi(REWARD_SPIN, "Spin the Wheel", "1 free spin resets every week.", "Up to +50", label("Spin", weeklySpinAvailable), available(weeklySpinAvailable)),
            CreditRewardUi(REWARD_CHECK_IN, "Daily Week Check-in", "Claim today on the 7-day track below.", "+$checkInAmount", label("Claim", checkInAvailable), available(checkInAvailable)),
            CreditRewardUi(REWARD_DAILY_RESET, "Every Day Reset", "Automatically adds 2 credits when you open the app each day.", "+2", autoLabel(dailyResetClaimed), false, isAutomatic = true),
            CreditRewardUi(REWARD_SIGNUP, "Signup Bonus", "Automatically adds after account signup.", "+2", autoLabel(signupClaimed), false, isAutomatic = true),
            CreditRewardUi(REWARD_EMAIL_LOGIN, "Email Login Bonus", "Automatically adds after email login.", "+1", autoLabel(emailLoginClaimed), false, isAutomatic = true),
            CreditRewardUi(REWARD_REFERRAL, "App Referral", "Referrer earns after a verified Play Store install from their referral link.", "+5", "Verify", false),
            CreditRewardUi(REWARD_SOCIAL_SHARE, "Social Share", "Earn after a creation is posted to supported social apps.", "+3", "Verify", false),
        )
    }

    private suspend fun grantAutomaticRewards(): Int {
        var total = 0
        val today = todayKey()
        if (rewardPrefs.getString(KEY_DAILY_RESET_DATE, "") != today) {
            generationRepository.addCredits(2)
            total += 2
            LocalCreditBalance.add(getApplication(), 2)
            rewardPrefs.edit()
                .putString(KEY_DAILY_RESET_DATE, today)
                .apply()
        }
        if (!rewardPrefs.getBoolean(KEY_SIGNUP_CLAIMED, false) && hasSignedUpUser()) {
            generationRepository.addCredits(2)
            total += 2
            LocalCreditBalance.add(getApplication(), 2)
            rewardPrefs.edit()
                .putBoolean(KEY_SIGNUP_CLAIMED, true)
                .apply()
        }
        if (!rewardPrefs.getBoolean(KEY_EMAIL_LOGIN_CLAIMED, false) && hasEmailLogin()) {
            generationRepository.addCredits(1)
            total += 1
            LocalCreditBalance.add(getApplication(), 1)
            rewardPrefs.edit()
                .putBoolean(KEY_EMAIL_LOGIN_CLAIMED, true)
                .apply()
        }
        return total
    }

    private fun rewardAmountFor(rewardId: String): Int =
        when (rewardId) {
            REWARD_SPIN -> weightedSpinReward()
            REWARD_CHECK_IN -> WEEKLY_CHECK_IN_REWARDS[checkInIndex()]
            REWARD_DAILY_RESET -> 2
            REWARD_SIGNUP -> 2
            REWARD_EMAIL_LOGIN -> 1
            REWARD_REFERRAL -> 5
            REWARD_SOCIAL_SHARE -> 3
            else -> 0
        }

    private fun weightedSpinReward(): Int {
        val totalWeight = SPIN_PRIZES.sumOf { it.weight }
        var ticket = Random.nextInt(totalWeight)
        SPIN_PRIZES.forEach { prize ->
            ticket -= prize.weight
            if (ticket < 0) return prize.amount
        }
        return 0
    }

    private fun addLocalRewardBalance(amount: Int) {
        LocalCreditBalance.add(getApplication(), amount)
    }

    private fun localRewardBalance(): Int =
        LocalCreditBalance.get(getApplication())

    private fun markRewardClaimed(rewardId: String) {
        val today = todayKey()
        rewardPrefs.edit().apply {
            when (rewardId) {
                REWARD_SPIN -> putString(KEY_SPIN_WEEK, weekKey())
                REWARD_CHECK_IN -> {
                    putString(KEY_CHECK_IN_DATE, today)
                    putInt(KEY_CHECK_IN_STREAK, (checkInIndex() + 1) % WEEKLY_CHECK_IN_REWARDS.size)
                }
                REWARD_DAILY_RESET -> putString(KEY_DAILY_RESET_DATE, today)
                REWARD_SIGNUP -> putBoolean(KEY_SIGNUP_CLAIMED, true)
                REWARD_EMAIL_LOGIN -> putBoolean(KEY_EMAIL_LOGIN_CLAIMED, true)
                REWARD_REFERRAL -> putString(KEY_REFERRAL_DATE, today)
                REWARD_SOCIAL_SHARE -> putString(KEY_SOCIAL_SHARE_DATE, today)
            }
        }.apply()
    }

    private fun checkInIndex(): Int {
        val lastCheckIn = rewardPrefs.getString(KEY_CHECK_IN_DATE, "").orEmpty()
        val storedStreak = rewardPrefs.getInt(KEY_CHECK_IN_STREAK, 0).coerceIn(0, WEEKLY_CHECK_IN_REWARDS.lastIndex)
        return if (lastCheckIn.isBlank() || lastCheckIn == yesterdayKey() || lastCheckIn == todayKey()) storedStreak else 0
    }

    private fun hasSignedUpUser(): Boolean =
        auth.currentUser?.isAnonymous == false

    private fun hasEmailLogin(): Boolean =
        auth.currentUser?.providerData?.any { it.providerId == "password" } == true || !auth.currentUser?.email.isNullOrBlank()

    private fun todayKey(): String = DATE_FORMAT.format(System.currentTimeMillis())

    private fun yesterdayKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return DATE_FORMAT.format(calendar.time)
    }

    private fun weekKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }

    companion object {
        private const val REWARD_SPIN = "spin"
        private const val REWARD_CHECK_IN = "check_in"
        private const val REWARD_DAILY_RESET = "daily_reset"
        private const val REWARD_SIGNUP = "signup"
        private const val REWARD_EMAIL_LOGIN = "email_login"
        private const val REWARD_REFERRAL = "referral"
        private const val REWARD_SOCIAL_SHARE = "social_share"

        private const val KEY_SPIN_WEEK = "spin_week"
        private const val KEY_CHECK_IN_DATE = "check_in_date"
        private const val KEY_CHECK_IN_STREAK = "check_in_streak"
        private const val KEY_DAILY_RESET_DATE = "daily_reset_date"
        private const val KEY_SIGNUP_CLAIMED = "signup_claimed"
        private const val KEY_EMAIL_LOGIN_CLAIMED = "email_login_claimed"
        private const val KEY_REFERRAL_DATE = "referral_date"
        private const val KEY_SOCIAL_SHARE_DATE = "social_share_date"
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val SPIN_PRIZES = listOf(
            SpinPrize(amount = 0, weight = 40),
            SpinPrize(amount = 2, weight = 15),
            SpinPrize(amount = 2, weight = 15),
            SpinPrize(amount = 10, weight = 15),
            SpinPrize(amount = 25, weight = 5),
            SpinPrize(amount = 50, weight = 1),
        )
        private val WEEKLY_CHECK_IN_REWARDS = listOf(1, 1, 2, 2, 2, 3, 4)
    }
}

private data class SpinPrize(
    val amount: Int,
    val weight: Int,
)
