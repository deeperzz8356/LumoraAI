package com.deep.lumoraai.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterAllIntervalTest {
    @Test fun firstAdNeedsNoClicksAndTwentyFiveSecondsSeparatesImpressions() {
        val frequency = AdsFrequencyManager()
        val gap = AdsConfig.DEFAULT.interAllIntervalMs
        assertEquals(25_000L, gap)
        assertEquals(0L, frequency.globalFullScreenCooldownRemaining(gap, 0L))
        frequency.recordFullScreenShown(AdPlacement.INTER_ALL, 1_000L)
        assertEquals(1L, frequency.globalFullScreenCooldownRemaining(gap, 25_999L))
        assertEquals(0L, frequency.globalFullScreenCooldownRemaining(gap, 26_000L))
        assertEquals(0L, frequency.placementCooldownRemaining(AdPlacement.INTER_ALL, gap, 26_000L))
    }
    @Test fun otherFullscreenAdsAlsoDelayInterAllAndLockStillPreventsOverlap() {
        val frequency = AdsFrequencyManager()
        frequency.recordFullScreenShown(AdPlacement.APP_OPEN, 1_000L)
        assertEquals(25_000L, frequency.globalFullScreenCooldownRemaining(25_000L, 1_000L))
        assertTrue(frequency.tryAcquireFullScreen())
        assertFalse(frequency.tryAcquireFullScreen())
        frequency.releaseFullScreen()
        assertTrue(frequency.tryAcquireFullScreen())
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
