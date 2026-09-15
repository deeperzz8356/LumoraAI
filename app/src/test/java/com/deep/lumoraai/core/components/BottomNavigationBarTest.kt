package com.deep.lumoraai.core.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomNavigationBarTest {
    @Test
    fun `subscription route hides bottom navigation`() {
        assertFalse(shouldShowBottomNavigation(route = "subscription", screenWidthDp = 500))
    }

    @Test
    fun `main tabs still show bottom navigation`() {
        assertTrue(shouldShowBottomNavigation(route = "home", screenWidthDp = 500))
        assertTrue(shouldShowBottomNavigation(route = "templates", screenWidthDp = 500))
        assertTrue(shouldShowBottomNavigation(route = "history", screenWidthDp = 500))
        assertTrue(shouldShowBottomNavigation(route = "profile", screenWidthDp = 500))
    }
}
