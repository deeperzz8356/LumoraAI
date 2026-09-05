package com.deep.lumoraai.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.deep.lumoraai.data.remote.bg.SubjectSegmentationFallback
import com.deep.lumoraai.data.remote.bg.WithoutBgApiClient
import com.deep.lumoraai.data.remote.bg.WithoutBgResult

/**
 * Orchestrates background removal:
 *   1. Primary  — WithoutBG Pro Model API (raw PNG bytes, 1 credit per success).
 *   2. Fallback — ML Kit Subject Segmentation on-device (offline, no credits),
 *      used when the API reports exhausted/expired credits, a transient/network
 *      error, or when [online] is false (offline mode).
 */
class BackgroundRemovalRepository(
    private val apiClient: WithoutBgApiClient = WithoutBgApiClient(),
    private val fallback: SubjectSegmentationFallback = SubjectSegmentationFallback(),
) {

    /** The outcome of a removal, carrying enough info for the UI to react. */
    sealed interface Outcome {
        /** Result produced by the WithoutBG API. */
        data class ApiSuccess(val pngBytes: ByteArray) : Outcome {
            override fun equals(other: Any?): Boolean =
                this === other || (other is ApiSuccess && pngBytes.contentEquals(other.pngBytes))
            override fun hashCode(): Int = pngBytes.contentHashCode()
        }

        /** Result produced on-device by the ML Kit fallback. */
        data class FallbackSuccess(val bitmap: Bitmap) : Outcome

        /** Both paths failed; [message] is user-facing. */
        data class Failure(val message: String) : Outcome
    }

    /**
     * Removes the background from [imageUri], preferring the API and falling back
     * to on-device segmentation.
     *
     * @param decodedBitmap a decoded copy of the source image, required for the
     *   on-device fallback.
     * @param online whether the device currently has connectivity. When false the
     *   API is skipped entirely and the fallback runs immediately.
     */
    suspend fun removeBackground(
        resolver: ContentResolver,
        imageUri: Uri,
        decodedBitmap: Bitmap,
        online: Boolean = true,
    ): Outcome {
        if (online) {
            when (val apiResult = apiClient.removeBackground(resolver, imageUri)) {
                is WithoutBgResult.Success -> return Outcome.ApiSuccess(apiResult.pngBytes)
                is WithoutBgResult.Failure -> {
                    // Fall back to on-device only when it makes sense: exhausted
                    // credits (402/403), rate limiting / server / network issues.
                    val shouldFallback = apiResult.retriable ||
                        apiResult.code == 402 ||
                        apiResult.code == 403 ||
                        apiResult.code == -1
                    if (!shouldFallback) {
                        return Outcome.Failure(apiResult.message)
                    }
                    val cutout = fallback.removeBackground(decodedBitmap)
                    return if (cutout != null) {
                        Outcome.FallbackSuccess(cutout)
                    } else {
                        Outcome.Failure(apiResult.message)
                    }
                }
            }
        }

        // Offline mode: go straight to on-device segmentation.
        val cutout = fallback.removeBackground(decodedBitmap)
        return if (cutout != null) {
            Outcome.FallbackSuccess(cutout)
        } else {
            Outcome.Failure("Could not remove the background offline. Connect to the internet and try again.")
        }
    }
}
