package com.deep.lumoraai

import com.deep.lumoraai.data.repository.GenerationRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardedAdCreditGrantTest {
    @Test
    fun rewardedAdIdempotencyKey_isCreated_withStableUserAndAmountMarkers() {
        val key = GenerationRepository.buildRewardedAdIdempotencyKey("uid-123", 2)

        assertFalse(key.isBlank())
        assertTrue(key.startsWith("rewarded-ad:uid-123:2:"))
    }
}
