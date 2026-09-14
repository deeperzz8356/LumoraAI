package com.deep.lumoraai.core.restrictions

object GenerationGate {
    val CREDITS_PER_IMAGE: Int get() = ToolPolicyStore.cost("text_to_image")
    val CREDITS_PER_VIDEO: Int get() = ToolPolicyStore.cost("text_to_video")
    const val DEVELOPER_MODE_CREDITS_DISPLAY = 9999

    fun canGenerateImage(credits: Int, isDeveloperMode: Boolean, generations: Int = 1): Boolean =
        isDeveloperMode || credits >= CREDITS_PER_IMAGE * generations.coerceAtLeast(1)

    /**
     * Image-to-image cost. All uploaded references (1-3 images) are analyzed
     * together for each requested output, so the cost scales only with the
     * number of outputs — not the number of source images.
     */
    fun imageCreditCost(sourceCount: Int, outputs: Int): Int =
        if (sourceCount <= 0) 0 else CREDITS_PER_IMAGE * outputs.coerceAtLeast(1)

    fun canGenerateVideo(credits: Int, isDeveloperMode: Boolean, generations: Int = 1): Boolean =
        isDeveloperMode || credits >= CREDITS_PER_VIDEO * generations.coerceAtLeast(1)

    fun insufficientCreditsMessage(): String =
        "Insufficient credits. Buy credits or upgrade your subscription to continue."
}
