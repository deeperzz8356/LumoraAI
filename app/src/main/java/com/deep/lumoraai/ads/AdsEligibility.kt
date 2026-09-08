package com.deep.lumoraai.ads

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/** Result of an eligibility evaluation. */
sealed interface AdEligibility {
    data object Allowed : AdEligibility
    data class Rejected(val reason: AdRejectionReason) : AdEligibility
}

/**
 * Implements the ad decision pipeline exactly once, centrally:
 *
 * ads_enabled -> format enabled -> placement enabled -> user eligible ->
 * policy eligible -> trigger/frequency -> placement cooldown ->
 * global full-screen cooldown -> ad ready
 *
 * Anything that fails returns a typed [AdRejectionReason]; the caller then just
 * continues the user's action normally.
 */
@Singleton
class AdsEligibility @Inject constructor(
    private val configStore: AdsConfigStore,
    private val frequency: AdsFrequencyManager,
    private val policyGuard: AdPolicyGuard,
) {

    /**
     * Evaluate whether [placement] may be shown right now.
     *
     * @param requireTrigger when true (interstitial navigation triggers), the
     *   trigger-count requirement is enforced.
     * @param adReady lambda returning whether the underlying ad object is loaded.
     */
    fun evaluate(
        context: Context,
        placement: AdPlacement,
        requireTrigger: Boolean = false,
        adReady: () -> Boolean = { true },
    ): AdEligibility {
        val config = configStore.current

        if (!config.adsEnabled) return reject(placement, AdRejectionReason.ADS_GLOBALLY_DISABLED)
        if (!config.formatEnabled(placement.format)) return reject(placement, AdRejectionReason.FORMAT_DISABLED)
        if (!config.isPlacementEnabled(placement)) return reject(placement, AdRejectionReason.PLACEMENT_DISABLED)

        if (!policyGuard.isPlacementPolicySafe(placement)) {
            return reject(placement, AdRejectionReason.POLICY_BLOCKED)
        }

        val isFullScreen = placement.format in FULL_SCREEN_FORMATS
        if (isFullScreen) {
            if (frequency.isFullScreenShowing()) {
                return reject(placement, AdRejectionReason.FULLSCREEN_ALREADY_SHOWING)
            }
            if (placement.format == AdFormat.INTERSTITIAL && frequency.sessionLimitReached(config)) {
                return reject(placement, AdRejectionReason.SESSION_LIMIT)
            }
            if (placement == AdPlacement.REWARD_CREDITS && !frequency.canClaimReward(context, config)) {
                return reject(placement, AdRejectionReason.REWARD_DAILY_LIMIT)
            }

            if (requireTrigger && placement.format == AdFormat.INTERSTITIAL && !frequency.triggerReached(config)) {
                return reject(
                    placement,
                    AdRejectionReason.TRIGGER_NOT_REACHED,
                )
            }

            val placementCooldownMs = if (placement.format == AdFormat.INTERSTITIAL) {
                config.interstitialPlacementCooldownMs
            } else {
                config.appOpenMinIntervalMs
            }
            if (frequency.placementCooldownRemaining(placement, placementCooldownMs) > 0) {
                return reject(placement, AdRejectionReason.COOLDOWN)
            }
            // Rewarded is user-initiated and intentional; exempt from the global
            // full-screen cooldown so watching for credits always works.
            if (placement.format != AdFormat.REWARDED &&
                frequency.globalFullScreenCooldownRemaining(config.globalFullScreenCooldownMs) > 0
            ) {
                return reject(placement, AdRejectionReason.COOLDOWN)
            }
        }

        if (!adReady()) return reject(placement, AdRejectionReason.AD_NOT_READY)

        return AdEligibility.Allowed
    }

    private fun reject(placement: AdPlacement, reason: AdRejectionReason): AdEligibility.Rejected {
        AdsLogger.rejected(placement, reason)
        return AdEligibility.Rejected(reason)
    }

    private companion object {
        val FULL_SCREEN_FORMATS = setOf(AdFormat.INTERSTITIAL, AdFormat.REWARDED, AdFormat.APP_OPEN)
    }
}
