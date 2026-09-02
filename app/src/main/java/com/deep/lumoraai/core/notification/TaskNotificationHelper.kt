package com.deep.lumoraai.core.notification

/**
 * Helper object for task notification constants and utilities
 * Centralized mapping of task types to display names and deep links
 */
object TaskNotificationHelper {

    // Task type constants
    const val TEXT_TO_IMAGE = "TEXT_TO_IMAGE"
    const val IMAGE_TO_IMAGE = "IMAGE_TO_IMAGE"
    const val IMAGE_TO_VIDEO = "IMAGE_TO_VIDEO"
    const val TEXT_TO_VIDEO = "TEXT_TO_VIDEO"
    const val PROMO_VIDEO = "PROMO_VIDEO"
    const val BG_REMOVE = "BG_REMOVE"
    const val BG_REPLACE = "BG_REPLACE"
    const val PHOTO_ENHANCE = "PHOTO_ENHANCE"
    const val COMPRESS = "COMPRESS"

    /**
     * Get human-readable display name for task type
     */
    fun getDisplayName(taskType: String): String = when (taskType) {
        TEXT_TO_IMAGE -> "Text to Image"
        IMAGE_TO_IMAGE -> "Image to Image"
        IMAGE_TO_VIDEO -> "Image to Video"
        TEXT_TO_VIDEO -> "Text to Video"
        PROMO_VIDEO -> "Promo Video"
        BG_REMOVE -> "Background Remove"
        BG_REPLACE -> "Background Replace"
        PHOTO_ENHANCE -> "Photo Enhance"
        COMPRESS -> "Compress"
        else -> taskType
    }

    /**
     * Get start notification message for task type
     */
    fun getStartMessage(taskType: String): String {
        val displayName = getDisplayName(taskType)
        return "$displayName generation has begun"
    }

    /**
     * Get complete notification message for task type
     */
    fun getCompleteMessage(taskType: String): String = when (taskType) {
        COMPRESS -> "Your files have been compressed successfully"
        PHOTO_ENHANCE -> "Your photo has been enhanced"
        BG_REMOVE -> "Background has been removed"
        BG_REPLACE -> "Background has been replaced"
        else -> "Your creation is ready to view or download"
    }

    /**
     * Get error notification message for task type
     */
    fun getErrorMessage(taskType: String): String {
        val displayName = getDisplayName(taskType)
        return "Failed to process $displayName. Please try again."
    }

    /**
     * Generate deep link URL for result screen
     */
    fun generateResultDeepLink(resultId: String): String {
        return "com.deep.lumoraai://result?resultId=$resultId"
    }

    /**
     * Check if task type is valid
     */
    fun isValidTaskType(taskType: String): Boolean = when (taskType) {
        TEXT_TO_IMAGE, IMAGE_TO_IMAGE, IMAGE_TO_VIDEO, TEXT_TO_VIDEO,
        PROMO_VIDEO, BG_REMOVE, BG_REPLACE, PHOTO_ENHANCE, COMPRESS -> true
        else -> false
    }

    /**
     * Get all supported task types
     */
    fun getAllTaskTypes(): List<String> = listOf(
        TEXT_TO_IMAGE,
        IMAGE_TO_IMAGE,
        IMAGE_TO_VIDEO,
        TEXT_TO_VIDEO,
        PROMO_VIDEO,
        BG_REMOVE,
        BG_REPLACE,
        PHOTO_ENHANCE,
        COMPRESS
    )

    /**
     * Get category for task type
     */
    fun getCategory(taskType: String): String = when (taskType) {
        TEXT_TO_IMAGE, IMAGE_TO_IMAGE, IMAGE_TO_VIDEO, TEXT_TO_VIDEO, PROMO_VIDEO -> "GENERATION"
        BG_REMOVE, BG_REPLACE -> "BACKGROUND"
        PHOTO_ENHANCE -> "ENHANCEMENT"
        COMPRESS -> "UTILITY"
        else -> "UNKNOWN"
    }
}
