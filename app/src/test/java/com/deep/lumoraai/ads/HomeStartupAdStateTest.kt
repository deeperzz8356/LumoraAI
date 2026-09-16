package com.deep.lumoraai.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.After

class HomeStartupAdStateTest {
    private var loggingEnabled = true
    @Before fun disableAndroidLogging() {
        loggingEnabled = AdsLogger.enabled
        AdsLogger.enabled = false
    }
    @After fun restoreLogging() { AdsLogger.enabled = loggingEnabled }

    @Test fun freshInstallDoesNotShowStartupEvenAfterLaunchFlagChanges() {
        val state = HomeStartupAdState()
        state.prepare(firstLaunch = true)
        state.prepare(firstLaunch = false)
        assertFalse(state.consume())
    }

    @Test fun repeatLaunchShowsStartupOnlyOnceAcrossHomeReentries() {
        val state = HomeStartupAdState()
        state.prepare(firstLaunch = false)
        assertTrue(state.consume())
        state.prepare(firstLaunch = false)
        assertFalse(state.consume())
    }

    @Test fun newProcessAllowsAnotherRepeatLaunchAttempt() {
        repeat(3) {
            val state = HomeStartupAdState()
            state.prepare(firstLaunch = false)
            assertTrue(state.consume())
            assertFalse(state.consume())
        }
    }

    @Test fun subscriptionConfigCanDisableReenableAndDefaultsOffWhenMissing() {
        val store = AdsConfigStore()
        store.applyRemoteJson("""{"subscription_enabled":true}""")
        assertTrue(store.current.subscriptionEnabled)
        store.applyRemoteJson("""{"subscription_enabled":false}""")
        assertFalse(store.current.subscriptionEnabled)
        store.applyRemoteJson("""{"subscription_enabled":true}""")
        assertTrue(store.current.subscriptionEnabled)
        store.applyRemoteJson("{}")
        assertFalse(store.current.subscriptionEnabled)
    }

    @Test fun subscriptionsDefaultOffButCanBeReenabled() {
        assertFalse(AdsConfig.DEFAULT.subscriptionEnabled)
        assertTrue(AdsConfig.DEFAULT.copy(subscriptionEnabled = true).subscriptionEnabled)
    }
}
