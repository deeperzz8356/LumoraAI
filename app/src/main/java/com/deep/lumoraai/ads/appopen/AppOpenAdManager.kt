package com.deep.lumoraai.ads.appopen

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
import com.google.android.gms.ads.appopen.AppOpenAd
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads and shows App Open ads. Enforces a freshness cap (AdMob ≤4h). The
 * min-interval and full-screen lock are enforced centrally by AdsEligibility /
 * AdsFrequencyManager before [show] is called.
 */
@Singleton
class AppOpenAdManager @Inject constructor(
    private val configStore: AdsConfigStore,
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var loadedAt = 0L

    fun preload(context: Context) {
        val config = configStore.current
        if (!config.formatEnabled(AdFormat.APP_OPEN)) return
        if (appOpenAd != null && isFresh(config)) return
        if (isLoading) return

        isLoading = true
        val unitId = config.unitIdFor(AdFormat.APP_OPEN)
        AdsLogger.loadStarted(AdPlacement.APP_OPEN, unitId, config.testMode)
        AppOpenAd.load(
            context.applicationContext,
            unitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    AdsLogger.loadSucceeded(AdPlacement.APP_OPEN, ad.responseInfo?.mediationAdapterClassName)
                    appOpenAd = ad
                    loadedAt = System.currentTimeMillis()
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    AdsLogger.loadFailed(AdPlacement.APP_OPEN, error.code, error.message)
                    appOpenAd = null
                    isLoading = false
                }
            }
        )
    }

    private fun isFresh(config: com.deep.lumoraai.ads.AdsConfig): Boolean =
        System.currentTimeMillis() - loadedAt < config.appOpenMaxCacheMs

    fun isReady(): Boolean = appOpenAd != null && isFresh(configStore.current)

    /**
     * Show the App Open ad. [onShown] fires when displayed; [onComplete] always
     * fires once. Returns false if no fresh ad was ready.
     */
    fun show(
        activity: Activity,
        onShown: () -> Unit,
        onComplete: () -> Unit,
    ): Boolean {
        if (!isReady()) {
            preload(activity)
            return false
        }
        val ad = appOpenAd ?: return false

        var forwarded = false
        val forwardOnce = { if (!forwarded) { forwarded = true; onComplete() } }

        AdsLogger.showAttempt(AdPlacement.APP_OPEN)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdsLogger.showSuccess(AdPlacement.APP_OPEN)
                onShown()
            }

            override fun onAdDismissedFullScreenContent() {
                AdsLogger.dismissed(AdPlacement.APP_OPEN)
                appOpenAd = null
                preload(activity)
                forwardOnce()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AdsLogger.showFailure(AdPlacement.APP_OPEN, error.message)
                appOpenAd = null
                preload(activity)
                forwardOnce()
            }
        }
        ad.show(activity)
        return true
    }
}
