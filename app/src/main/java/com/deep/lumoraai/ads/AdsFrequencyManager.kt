package com.deep.lumoraai.ads

import android.content.Context
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized frequency, cooldown and full-screen-lock logic. All screens share
 * this single instance so counters are never duplicated or inconsistent.
 *
 * In-memory state (trigger counts, session count, cooldown timestamps, the
 * full-screen lock) resets per process. Persistent state (reward daily count,
 * post-splash-seen, first-launch) uses SharedPreferences, matching the app's
 * existing lightweight persistence pattern.
 */
@Singleton
class AdsFrequencyManager @Inject constructor() {

    // ---- In-memory session state ----
    private var featureTriggerCount = 0
    private var interstitialsShownThisSession = 0
    private var lastFullScreenShownAt = 0L
    private val lastPlacementShownAt = mutableMapOf<String, Long>()
    private val fullScreenShowing = AtomicBoolean(false)

    // ---- Full-screen lock ----

    /** Try to acquire the single full-screen slot. Returns false if one is up. */
    fun tryAcquireFullScreen(): Boolean = fullScreenShowing.compareAndSet(false, true)

    fun releaseFullScreen() {
        fullScreenShowing.set(false)
    }

    fun isFullScreenShowing(): Boolean = fullScreenShowing.get()

    // ---- Interstitial trigger counting (feature selections) ----

    /** Increment the eligible-feature-selection counter. Bottom-nav must NOT call this. */
    fun recordFeatureTrigger() {
        featureTriggerCount++
    }

    fun triggerReached(config: AdsConfig): Boolean =
        featureTriggerCount >= config.interstitialTriggerCount

    fun resetTriggerCount() {
        featureTriggerCount = 0
    }

    fun currentTriggerCount(): Int = featureTriggerCount

    // ---- Cooldowns ----

    fun placementCooldownRemaining(placement: AdPlacement, cooldownMs: Long): Long {
        val last = lastPlacementShownAt[placement.key] ?: return 0L
        return (last + cooldownMs - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun globalFullScreenCooldownRemaining(cooldownMs: Long): Long =
        (lastFullScreenShownAt + cooldownMs - System.currentTimeMillis()).coerceAtLeast(0L)

    fun sessionLimitReached(config: AdsConfig): Boolean =
        interstitialsShownThisSession >= config.maxInterstitialsPerSession

    /** Record a full-screen impression: updates cooldowns, session count, trigger reset. */
    fun recordFullScreenShown(placement: AdPlacement) {
        val now = System.currentTimeMillis()
        lastFullScreenShownAt = now
        lastPlacementShownAt[placement.key] = now
        if (placement.format == AdFormat.INTERSTITIAL) {
            interstitialsShownThisSession++
            resetTriggerCount()
        }
    }

    // ---- Reward daily limit (persistent) ----

    fun rewardClaimsToday(context: Context): Int {
        val prefs = prefs(context)
        return if (prefs.getInt(KEY_REWARD_DAY, -1) == dayOfYear()) {
            prefs.getInt(KEY_REWARD_COUNT, 0)
        } else 0
    }

    fun canClaimReward(context: Context, config: AdsConfig): Boolean =
        rewardClaimsToday(context) < config.rewardMaxClaimsPerDay

    fun recordRewardClaim(context: Context) {
        val prefs = prefs(context)
        val today = dayOfYear()
        val count = if (prefs.getInt(KEY_REWARD_DAY, -1) == today) prefs.getInt(KEY_REWARD_COUNT, 0) else 0
        prefs.edit()
            .putInt(KEY_REWARD_DAY, today)
            .putInt(KEY_REWARD_COUNT, count + 1)
            .apply()
    }

    // ---- Post-splash / first-launch (persistent) ----

    /** inter_post_splash must never show on the absolute first launch. */
    fun isFirstLaunch(context: Context): Boolean =
        !prefs(context).getBoolean(KEY_LAUNCHED_BEFORE, false)

    fun markLaunched(context: Context) {
        prefs(context).edit().putBoolean(KEY_LAUNCHED_BEFORE, true).apply()
    }

    private fun dayOfYear(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS = "lumora_ads_frequency"
        const val KEY_REWARD_DAY = "reward_day"
        const val KEY_REWARD_COUNT = "reward_count"
        const val KEY_LAUNCHED_BEFORE = "launched_before"
    }
}
