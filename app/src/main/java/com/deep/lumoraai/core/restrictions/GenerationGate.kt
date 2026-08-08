package com.deep.lumoraai.core.restrictions

object GenerationGate {
    const val CREDITS_PER_IMAGE = 1
    const val CREDITS_PER_VIDEO = 5
    const val DEVELOPER_MODE_CREDITS_DISPLAY = 9999

    fun canGenerateImage(credits: Int, isDeveloperMode: Boolean): Boolean =
        isDeveloperMode || credits >= CREDITS_PER_IMAGE

    fun canGenerateVideo(credits: Int, isDeveloperMode: Boolean): Boolean =
        isDeveloperMode || credits >= CREDITS_PER_VIDEO

    fun insufficientCreditsMessage(): String =
        "Insufficient credits. Buy credits or upgrade your subscription to continue."
}
