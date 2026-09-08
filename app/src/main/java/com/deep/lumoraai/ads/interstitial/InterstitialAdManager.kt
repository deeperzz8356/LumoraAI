package com.deep.lumoraai.ads.interstitial

import android.app.Activity
import android.content.Context
import com.deep.lumoraai.ads.AdFormat
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.AdsConfigStore
import com.deep.lumoraai.ads.AdsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and shows interstitial ads with preloading and automatic reload.
 * Eligibility/cooldown/frequency decisions live in AdsEligibility/Frequency —
 * this manager only owns loading, the SDK show call, and reload. Screens never
 * touch it directly (they go through AdsManager).
 */
@Singleton
class InterstitialAdManager @Inject constructor(
    private val configStore: AdsConfigStore,
) {
    private var interstitial: InterstitialAd? = null
    private var isLoading = false

    fun preload(context: Context, placement: AdPlacement = AdPlacement.INTER_ALL) {
        val config = configStore.current
        if (!config.formatEnabled(AdFormat.INTERSTITIAL)) return
        if (interstitial != null || isLoading) return

        isLoading = true
        val unitId = config.unitIdFor(AdFormat.INTERSTITIAL)
        AdsLogger.loadStarted(placement, unitId, config.testMode)
        InterstitialAd.load(
            context.applicationContext,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    AdsLogger.loadSucceeded(placement, ad.responseInfo?.mediationAdapterClassName)
                    interstitial = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    AdsLogger.loadFailed(placement, error.code, error.message)
                    interstitial = null
                    isLoading = false
                }
            }
        )
    }

    fun isReady(): Boolean = interstitial != null

    /**
     * Show the (already eligibility-checked) interstitial. [onShown] fires when
     * the ad actually displayed; [onComplete] always fires exactly once so the
     * user flow proceeds. Returns false if there was no ad to show.
     */
    fun show(
        activity: Activity,
        placement: AdPlacement,
        onShown: () -> Unit,
        onComplete: () -> Unit,
    ): Boolean {
        val ad = interstitial ?: run {
            preload(activity, placement)
            return false
        }

        var forwarded = false
        val forwardOnce = {
            if (!forwarded) { forwarded = true; onComplete() }
        }

        AdsLogger.showAttempt(placement)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdsLogger.showSuccess(placement)
                onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                AdsLogger.dismissed(placement)
                interstitial = null
                preload(activity, placement)
                forwardOnce()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AdsLogger.showFailure(placement, error.message)
                interstitial = null
                preload(activity, placement)
                forwardOnce()
            }
        }
        ad.show(activity)
        return true
    }
}
