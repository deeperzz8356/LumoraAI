package com.deep.lumoraai.feature.imagetoimage

object ImageToImageBatch {
    /** Maximum number of reference images a user can upload for one generation. */
    const val MAX_SOURCE_IMAGES = 3

    /**
     * Credit cost for an image-to-image run.
     *
     * All uploaded references (1-3 images) are analyzed together for a single
     * generation, so cost depends only on how many outputs the user requested —
     * not on the number of source images. Requesting 2 outputs costs 2 credits
     * regardless of whether 1, 2, or 3 references were uploaded.
     */
    fun creditCost(sourceCount: Int, outputs: Int): Int =
        if (sourceCount <= 0) 0 else outputs.coerceAtLeast(1)
}
