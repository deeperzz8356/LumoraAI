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
import com.deep.lumoraai.core.utils.CreditBalanceStore
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.RewardsRepository
import com.deep.lumoraai.data.billing.BillingRepository
import com.deep.lumoraai.data.billing.BillingResult
import kotlinx.coroutines.flow.collect
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreditsViewModel(application: Application) : AndroidViewModel(application) {
    private val generationRepository = GenerationRepository()
    private val rewardsRepository = RewardsRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val rewardPrefs = application.getSharedPreferences("lumora_credit_rewards", Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val billing = BillingRepository(application)

    var uiState: CreditsUiState by mutableStateOf(CreditsUiState.Loading)
        private set

    /**
     * Bug 2 (isBugCondition2) guard state for the credits fetch.
     *
     * The credits fetch used to fire on every invocation of load() (which was
     * itself called from init and could be re-driven by recomposition-sensitive
     * triggers), producing a burst of GET /api/v1/credits calls per screen
     * entry. These fields collapse that into a single fetch per entry:
     *  - [creditsFetchJob] holds the in-flight fetch so concurrent/rapid
     *    triggers coalesce onto the SAME request instead of starting new ones.
     *  - [lastFetchedAtMs] records when the last successful/completed fetch
     *    happened so a repeated trigger within [FRESH_WINDOW_MS] is served from
     *    the already-loaded state instead of re-fetching.
     * Legitimate refresh events (successful generation, purchase, sign-in/token
     * refresh) bypass both guards via [forceRefresh].
     */
    private var creditsFetchJob: Job? = null
    private var lastFetchedAtMs: Long = 0L

    init {
        billing.connect()
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

    /**
     * Drives the single credits fetch for a screen entry.
     *
     * Call this from a stable, entry-scoped trigger (e.g. LaunchedEffect(Unit)
     * in CreditsRoute). It is safe to call on every recomposition: if a fetch
     * is already in flight it coalesces onto that request, and if fresh data is
     * already loaded within the freshness window it serves the cached state
     * without a network call. This is what makes entering the screen fetch
     * exactly once regardless of recomposition count (Bug 2 fix).
     */
    fun ensureLoaded() {
        load(force = false)
    }

    /**
     * Forces a fresh credits fetch for a legitimate refresh event (successful
     * generation, purchase, sign-in / token refresh). Bypasses the freshness
     * window but still coalesces onto any in-flight request so a single logical
     * event yields a single network call (preservation: one call per distinct
     * legitimate event).
     */
    fun forceRefresh() {
        load(force = true)
    }

    fun load(force: Boolean = true) {
        // Coalesce rapid/concurrent triggers onto the in-flight request.
        if (creditsFetchJob?.isActive == true) return
        // Serve fresh cached data instead of re-fetching for non-forced triggers.
        if (!force &&
            uiState is CreditsUiState.Success &&
            (System.currentTimeMillis() - lastFetchedAtMs) < FRESH_WINDOW_MS
        ) {
            return
        }

        if (uiState !is CreditsUiState.Success) {
            uiState = CreditsUiState.Loading
        }
        creditsFetchJob = viewModelScope.launch {
            try {
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
                // Claim any pending automatic rewards on the server first; it
                // returns an authoritative balance when it grants anything.
                val autoBalance = grantAutomaticRewards()
                // Then read the authoritative balance (auto-claim balance wins if present).
                val backendCredits = autoBalance ?: generationRepository.getCredits().getOrNull()
                CreditBalanceStore.set(backendCredits)
                uiState = CreditsUiState.Success(
                    credits = backendCredits ?: 0,
                    isDeveloperMode = false,
                    rewards = buildRewardTasks(isDeveloperMode = false),
                    rewardMessage = null,
                    checkInDayIndex = checkInIndex()
                )
            } finally {
                lastFetchedAtMs = System.currentTimeMillis()
                creditsFetchJob = null
            }
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

        uiState = currentState.copy(isRewardBusy = true, rewardMessage = "Claiming reward...")
        viewModelScope.launch {
            // Server is authoritative: it decides the amount, enforces caps/streak,
            // and returns the new balance. The client only says which reward happened.
            val result = when (rewardId) {
                REWARD_SPIN -> rewardsRepository.spin()
                REWARD_CHECK_IN -> rewardsRepository.claimCheckIn()
                REWARD_DAILY_RESET -> rewardsRepository.claimDailyReset()
                REWARD_SIGNUP -> rewardsRepository.claimSignUpBonus()
                REWARD_EMAIL_LOGIN -> rewardsRepository.claimEmailLogin()
                else -> Result.failure(IllegalArgumentException("Reward $rewardId is not claimable here."))
            }

            val reward2 = result.getOrNull()
            if (reward2 == null) {
                uiState = (uiState as? CreditsUiState.Success ?: currentState).copy(
                    isRewardBusy = false,
                    rewardMessage = result.exceptionOrNull()?.message ?: "Could not claim reward. Try again.",
                )
                return@launch
            }

            // Persist the local "claimed" marker only for UI gating (the server is
            // the real guard via idempotency). Only mark on a real success/consumed
            // outcome so a transient failure doesn't hide an unclaimed reward.
            if (reward2.isSuccess || reward2.isAlreadyClaimed) {
                markRewardClaimed(rewardId)
            }

            val awarded = reward2.creditsAwarded
            val message = when {
                reward2.isAlreadyClaimed -> reward2.message ?: "Already claimed."
                reward2.isCapped -> reward2.message ?: "Reward cap reached."
                rewardId == REWARD_SPIN && awarded == 0 ->
                    reward2.message ?: "Better luck next time. Your free weekly spin was used."
                rewardId == REWARD_CHECK_IN && reward2.streakDay != null ->
                    "+$awarded credits · Day ${reward2.streakDay} check-in."
                else -> "+$awarded credits added from ${reward.title}."
            }

            // Prefer the server-reported balance as the source of truth.
            val newCredits = reward2.balance ?: run {
                (generationRepository.getCredits().getOrNull() ?: (currentState.credits + awarded))
            }
            // Publish so the header (and any other screen) reflects earned credits.
            CreditBalanceStore.set(newCredits)

            uiState = (uiState as? CreditsUiState.Success ?: currentState).copy(
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

    /**
     * Claims the automatic rewards (daily reset, sign-up bonus, email-login
     * bonus) via the server, which owns the amounts and idempotency. Returns the
     * latest server-reported balance if any claim produced one, else null.
     *
     * The SharedPrefs markers are only a client-side UI hint to avoid firing the
     * request every load; the server's idempotency is the real guard, so a stale
     * or reset marker cannot cause a double-grant.
     */
    private suspend fun grantAutomaticRewards(): Int? {
        var latestBalance: Int? = null
        val today = todayKey()

        if (rewardPrefs.getString(KEY_DAILY_RESET_DATE, "") != today) {
            rewardsRepository.claimDailyReset().getOrNull()?.let { r ->
                if (r.isSuccess || r.isAlreadyClaimed) {
                    rewardPrefs.edit().putString(KEY_DAILY_RESET_DATE, today).apply()
                }
                r.balance?.let { latestBalance = it }
            }
        }
        if (!rewardPrefs.getBoolean(KEY_SIGNUP_CLAIMED, false) && hasSignedUpUser()) {
            rewardsRepository.claimSignUpBonus().getOrNull()?.let { r ->
                if (r.isSuccess || r.isAlreadyClaimed) {
                    rewardPrefs.edit().putBoolean(KEY_SIGNUP_CLAIMED, true).apply()
                }
                r.balance?.let { latestBalance = it }
            }
        }
        if (!rewardPrefs.getBoolean(KEY_EMAIL_LOGIN_CLAIMED, false) && hasEmailLogin()) {
            rewardsRepository.claimEmailLogin().getOrNull()?.let { r ->
                if (r.isSuccess || r.isAlreadyClaimed) {
                    rewardPrefs.edit().putBoolean(KEY_EMAIL_LOGIN_CLAIMED, true).apply()
                }
                r.balance?.let { latestBalance = it }
            }
        }
        return latestBalance
    }

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
        /**
         * How long a completed credits fetch is considered fresh. A non-forced
         * trigger (screen entry / recomposition) within this window is served
         * from the loaded state instead of re-fetching, collapsing a burst of
         * recomposition-driven triggers into a single network call (Bug 2 fix).
         */
        private const val FRESH_WINDOW_MS = 30_000L

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
        // Display-only mirror of the server's check-in streak amounts
        // (backend rewards_config.CHECK_IN_STREAK_CREDITS). The server is
        // authoritative for the actual grant; this only drives the "+N" label.
        private val WEEKLY_CHECK_IN_REWARDS = listOf(1, 1, 2, 2, 3, 4, 5)
    }
}
