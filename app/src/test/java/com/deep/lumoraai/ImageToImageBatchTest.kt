package com.deep.lumoraai

import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.imagetoimage.ImageToImageBatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageToImageBatchTest {
    @Test
    fun creditCost_matchesEachSourceOutputRequest() {
        assertEquals(1, ImageToImageBatch.creditCost(1, 1))
        assertEquals(6, ImageToImageBatch.creditCost(3, 2))
        assertEquals(20, ImageToImageBatch.creditCost(5, 4))
    }

    @Test
    fun creditCost_capsSourcesAtFive() {
        assertEquals(20, ImageToImageBatch.creditCost(7, 4))
    }

    @Test
    fun generationGate_usesTheFullBatchCost() {
        val cost = GenerationGate.imageCreditCost(sourceCount = 5, outputsPerSource = 4)
        assertTrue(GenerationGate.canGenerateImage(credits = cost, isDeveloperMode = false, generations = cost))
        assertFalse(GenerationGate.canGenerateImage(credits = cost - 1, isDeveloperMode = false, generations = cost))
    }
}
