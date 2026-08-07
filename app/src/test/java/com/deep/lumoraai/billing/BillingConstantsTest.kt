package com.deep.lumoraai.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingConstantsTest {
    @Test
    fun entitlementIdMatchesDashboard() {
        assertEquals("MK Tech Media tech", BillingConstants.ENTITLEMENT_ID)
    }

    @Test
    fun packageIdsMatchRevenueCatDefaults() {
        assertEquals("\$rc_monthly", BillingConstants.PACKAGE_MONTHLY)
        assertEquals("\$rc_annual", BillingConstants.PACKAGE_ANNUAL)
        assertEquals("monthly", BillingConstants.PRODUCT_MONTHLY)
        assertEquals("yearly", BillingConstants.PRODUCT_YEARLY)
    }

    @Test
    fun offeringDefaultIsConfigured() {
        assertTrue(BillingConstants.OFFERING_DEFAULT.isNotBlank())
    }
}
