package com.deep.lumoraai.ads

import android.app.Activity
import android.content.Context
import com.deep.lumoraai.ads.appopen.AppOpenAdManager
import com.deep.lumoraai.ads.interstitial.InterstitialAdManager
import com.deep.lumoraai.ads.nativead.NativeAdManager
import com.deep.lumoraai.ads.rewarded.AdReward
import com.deep.lumoraai.ads.rewarded.RewardedAdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single façade the app uses for all advertising. Screens call by
 * [AdPlacement] and never touch the SDK. This coordinates the decision pipeline
 * ([AdsEligibility]), frequency/cooldown/lock ([AdsFrequencyManager]) and the
 * per-format managers. Mediation, consent and Remote Config plug in here later
 * without changing any call site.
 */
@Singleton
class AdsManager @Inject constructor(
    private val configStore: AdsConfigStore,
    private val eligibility: AdsEligibility,
    private val frequency: AdsFrequencyManager,
    private val interstitialManager: InterstitialAdManager,
    private val rewardedManager: RewardedAdManager,
    private val appOpenManager: AppOpenAdManager,
    val nativeManager: NativeAdManager,
) {
    private val initialized = AtomicBoolean(false)

    val config: AdsConfig get() = configStore.current

    /** Initialize the Mobile Ads SDK once and warm up preloadable formats. */
    fun initialize(context: Context) {
        if (!config.adsEnabled) return
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext

        if (config.testMode) {
            // Ensure only non-personalized/test-safe content during development.
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .build()
            )
        }

        MobileAds.initialize(appContext) { status ->
            AdsLogger.d("MobileAds initialized: ${status.adapterStatusMap.keys}")
            interstitialManager.preload(appContext)
            rewardedManager.preload(appContext)
            appOpenManager.preload(appContext)
        }
    }

    fun isInitialized(): Boolean = initialized.get()

    fun markLaunched(context: Context) = frequency.markLaunched(context)
    fun isFirstLaunch(context: Context): Boolean = frequency.isFirstLaunch(context)

    // ---- Interstitial ----

    fun preloadInterstitial(context: Context, placement: AdPlacement = AdPlacement.INTER_ALL) =
        interstitialManager.preload(context, placement)

    /** Record an eligible feature selection (NOT bottom-nav) for inter_all trigger counting. */
    fun recordFeatureTrigger() = frequency.recordFeatureTrigger()

    /**
     * Show an interstitial for [placement] then run [onContinue]. Runs the full
     * decision pipeline; if any check fails the ad is skipped and [onContinue]
     * runs immediately. [onContinue] always runs exactly once.
     */
    fun showInterstitial(
        activity: Activity?,
        placement: AdPlacement,
        requireTrigger: Boolean = false,
        onContinue: () -> Unit,
    ) {
        if (activity == null) {
            AdsLogger.rejected(placement, AdRejectionReason.NO_ACTIVITY)
            onContinue(); return
        }
        val verdict = eligibility.evaluate(
            context = activity,
            placement = placement,
            requireTrigger = requireTrigger,
            adReady = { interstitialManager.isReady() },
        )
        if (verdict !is AdEligibility.Allowed) {
            onContinue(); return
        }
        if (!frequency.tryAcquireFullScreen()) {
            AdsLogger.rejected(placement, AdRejectionReason.FULLSCREEN_ALREADY_SHOWING)
            onContinue(); return
        }

        var completed = false
        val complete = {
            if (!completed) {
                completed = true
                frequency.releaseFullScreen()
                onContinue()
            }
        }
        val shown = interstitialManager.show(
            activity = activity,
            placement = placement,
            onShown = { frequency.recordFullScreenShown(placement) },
            onComplete = complete,
        )
        if (!shown) complete()
    }

    fun isInterstitialReady(): Boolean = interstitialManager.isReady()

    // ---- Rewarded ----

    fun preloadRewarded(context: Context) = rewardedManager.preload(context)

    /**
     * Show a rewarded ad. [onReward] fires only on the genuine SDK earned-reward
     * callback and only if the daily claim limit allows; [onClosed] always fires.
     */
    fun showRewarded(
        activity: Activity?,
        placement: AdPlacement = AdPlacement.REWARD_CREDITS,
        onReward: (AdReward) -> Unit,
        onClosed: () -> Unit = {},
    ) {
        if (activity == null) {
            AdsLogger.rejected(placement, AdRejectionReason.NO_ACTIVITY)
            onClosed(); return
        }
        val verdict = eligibility.evaluate(
            context = activity,
            placement = placement,
            adReady = { rewardedManager.isReady() },
        )
        if (verdict !is AdEligibility.Allowed) {
            onClosed(); return
        }
        if (!frequency.tryAcquireFullScreen()) {
            AdsLogger.rejected(placement, AdRejectionReason.FULLSCREEN_ALREADY_SHOWING)
            onClosed(); return
        }

        var closed = false
        val close = {
            if (!closed) {
                closed = true
                frequency.releaseFullScreen()
                onClosed()
            }
        }
        val shown = rewardedManager.show(
            activity = activity,
            placement = placement,
            onShown = { frequency.recordFullScreenShown(placement) },
            onReward = { reward ->
                // Grant ONLY on the genuine SDK callback, and record the claim
                // for the daily limit.
                frequency.recordRewardClaim(activity)
                onReward(reward)
            },
            onClosed = close,
        )
        if (!shown) close()
    }

    fun isRewardedReady(): Boolean = rewardedManager.isReady()

    fun rewardClaimsToday(context: Context): Int = frequency.rewardClaimsToday(context)

    // ---- App Open ----

    fun preloadAppOpen(context: Context) = appOpenManager.preload(context)

    /**
     * Show the App Open ad for [placement]. [onComplete] always fires once so
     * cold-start/resume flows never hang.
     */
    fun showAppOpen(
        activity: Activity?,
        placement: AdPlacement = AdPlacement.APP_OPEN,
        onComplete: () -> Unit = {},
    ) {
        if (activity == null) {
            AdsLogger.rejected(placement, AdRejectionReason.NO_ACTIVITY)
            onComplete(); return
        }
        val verdict = eligibility.evaluate(
            context = activity,
            placement = placement,
            adReady = { appOpenManager.isReady() },
        )
        if (verdict !is AdEligibility.Allowed) {
            onComplete(); return
        }
        if (!frequency.tryAcquireFullScreen()) {
            AdsLogger.rejected(placement, AdRejectionReason.FULLSCREEN_ALREADY_SHOWING)
            onComplete(); return
        }

        var completed = false
        val complete = {
            if (!completed) {
                completed = true
                frequency.releaseFullScreen()
                onComplete()
            }
        }
        val shown = appOpenManager.show(
            activity = activity,
            onShown = { frequency.recordFullScreenShown(placement) },
            onComplete = complete,
        )
        if (!shown) complete()
    }

    fun isAppOpenReady(): Boolean = appOpenManager.isReady()

    // ---- Native ----

    fun clearNativeAds() = nativeManager.destroyAll()
}
