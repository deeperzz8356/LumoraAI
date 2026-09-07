package com.deep.lumoraai.feature.imagetoimage

object ImageToImageBatch {
    const val MAX_SOURCE_IMAGES = 5

    fun creditCost(sourceCount: Int, outputsPerSource: Int): Int =
        sourceCount.coerceIn(0, MAX_SOURCE_IMAGES) * outputsPerSource.coerceAtLeast(1)
}
