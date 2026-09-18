package com.deep.lumoraai.ads

import org.junit.Assert.assertTrue
import org.junit.Test

class AdsIntervalDefaultsTest {
    @Test
    fun interstitial_default_intervals_are_non_zero() {
        val config = AdsConfig.DEFAULT

        assertTrue("inter_all should have a sane default cooldown", config.interAllIntervalMs > 0L)
        assertTrue("inter_back should have a sane default cooldown", config.interBackIntervalMs > 0L)
        assertTrue("inter_generate should have a sane default cooldown", config.interGenerateIntervalMs > 0L)
        assertTrue("global full-screen cooldown should be enabled by default", config.globalFullScreenCooldownMs > 0L)
    }
}
