package com.deep.lumoraai.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterAllIntervalTest {
    @Test fun firstAdNeedsNoClicksAndTwentySecondsSeparatesImpressions() {
        val frequency = AdsFrequencyManager()
        val gap = AdsConfig.DEFAULT.interAllIntervalMs
        assertEquals(20_000L, gap)
        assertEquals(0L, frequency.globalFullScreenCooldownRemaining(gap, 0L))
        frequency.recordFullScreenShown(AdPlacement.INTER_ALL, 1_000L)
        assertEquals(1_001L, frequency.globalFullScreenCooldownRemaining(gap, 19_999L))
        assertEquals(1_000L, frequency.globalFullScreenCooldownRemaining(gap, 20_000L))
        assertEquals(0L, frequency.globalFullScreenCooldownRemaining(gap, 21_000L))
        assertEquals(0L, frequency.placementCooldownRemaining(AdPlacement.INTER_ALL, gap, 21_000L))
    }
    @Test fun otherFullscreenAdsAlsoDelayInterAllAndLockStillPreventsOverlap() {
        val frequency = AdsFrequencyManager()
        frequency.recordFullScreenShown(AdPlacement.APP_OPEN, 1_000L)
        assertEquals(20_000L, frequency.globalFullScreenCooldownRemaining(20_000L, 1_000L))
        assertTrue(frequency.tryAcquireFullScreen())
        assertFalse(frequency.tryAcquireFullScreen())
        frequency.releaseFullScreen()
        assertTrue(frequency.tryAcquireFullScreen())
    }
    @Test fun independentInterstitialPlacementsDoNotShareGlobalCooldown() {
        assertTrue(AdPlacement.INTER_ALL.isIndependentInterstitial())
        assertTrue(AdPlacement.INTER_BACK.isIndependentInterstitial())
        assertTrue(AdPlacement.INTER_GENERATE.isIndependentInterstitial())
        assertFalse(AdPlacement.INTER_POST_SPLASH.isIndependentInterstitial())
        assertFalse(AdPlacement.APP_OPEN.isIndependentInterstitial())
    }
    @Test fun remoteIntervalCanIncreaseDecreaseAndIsBounded() {
        val previous = AdsLogger.enabled
        AdsLogger.enabled = false
        try {
            val store = AdsConfigStore()
            store.applyRemoteJson("""{"inter_all_interval_ms":40000}""")
            assertEquals(40_000L, store.current.interAllIntervalMs)
            store.applyRemoteJson("""{"inter_all_interval_ms":10000,"inter_trigger_count":3}""")
            assertEquals(10_000L, store.current.interAllIntervalMs)
            store.applyRemoteJson("""{"inter_all_interval_ms":-10}""")
            assertEquals(0L, store.current.interAllIntervalMs)
            store.applyRemoteJson("""{"inter_all_interval_ms":900000}""")
            assertEquals(600_000L, store.current.interAllIntervalMs)
        } finally { AdsLogger.enabled = previous }
    }
}
