package com.deep.lumoraai

import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.feature.imagetoimage.ImageToImageBatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageToImageBatchTest {
    @Test
    fun creditCost_dependsOnlyOnOutputCount() {
        // All references are analyzed together, so cost scales with outputs only.
        assertEquals(1, ImageToImageBatch.creditCost(sourceCount = 1, outputs = 1))
        assertEquals(2, ImageToImageBatch.creditCost(sourceCount = 3, outputs = 2))
        assertEquals(2, ImageToImageBatch.creditCost(sourceCount = 1, outputs = 2))
        assertEquals(3, ImageToImageBatch.creditCost(sourceCount = 2, outputs = 3))
    }

    @Test
    fun creditCost_isZeroWithoutSources() {
        assertEquals(0, ImageToImageBatch.creditCost(sourceCount = 0, outputs = 4))
    }

    @Test
    fun maxSourceImages_isThree() {
        assertEquals(3, ImageToImageBatch.MAX_SOURCE_IMAGES)
    }

    @Test
    fun generationGate_costMatchesOutputs() {
        val cost = GenerationGate.imageCreditCost(sourceCount = 3, outputs = 4)
        assertEquals(4, cost)
        assertTrue(GenerationGate.canGenerateImage(credits = cost, isDeveloperMode = false, generations = cost))
        assertFalse(GenerationGate.canGenerateImage(credits = cost - 1, isDeveloperMode = false, generations = cost))
    }
}
