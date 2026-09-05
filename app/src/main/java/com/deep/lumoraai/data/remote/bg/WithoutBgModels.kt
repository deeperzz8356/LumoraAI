package com.deep.lumoraai.data.remote.bg

/**
 * Typed outcome for a WithoutBG background-removal attempt.
 *
 * The API returns raw `image/png` bytes on success (HTTP 200). Every documented
 * error status is mapped to a user-facing message so the UI layer never has to
 * parse HTTP codes itself.
 */
sealed interface WithoutBgResult {
    /** RGBA PNG cutout bytes returned by the API. */
    data class Success(val pngBytes: ByteArray) : WithoutBgResult {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return pngBytes.contentEquals(other.pngBytes)
        }

        override fun hashCode(): Int = pngBytes.contentHashCode()
    }

    /**
     * A mapped API error.
     *
     * @param code the HTTP status code returned by the server (or -1 for transport failures).
     * @param message a user-facing message.
     * @param retriable true for 429 / transient network errors where a retry or the
     *   on-device fallback should be attempted.
     */
    data class Failure(
        val code: Int,
        val message: String,
        val retriable: Boolean = false,
    ) : WithoutBgResult
}

/**
 * Maps a WithoutBG HTTP status code to a user-facing message, per the API contract.
 */
internal fun withoutBgErrorFor(code: Int, rawBody: String? = null): WithoutBgResult.Failure = when (code) {
    401 -> WithoutBgResult.Failure(code, "Invalid Background Removal API Key")
    402, 403 -> WithoutBgResult.Failure(code, "Insufficient or expired credits")
    413 -> WithoutBgResult.Failure(code, "File size too large (Max 20MB)")
    415 -> WithoutBgResult.Failure(code, "Unsupported image format")
    422 -> WithoutBgResult.Failure(code, "The image could not be processed")
    429 -> WithoutBgResult.Failure(code, "Server busy, try again later", retriable = true)
    in 500..599 -> WithoutBgResult.Failure(code, "Background removal service error. Please try again later.", retriable = true)
    else -> WithoutBgResult.Failure(
        code,
        rawBody?.takeIf { it.isNotBlank() }?.let { "Background removal failed: $it" }
            ?: "Background removal failed (HTTP $code)",
    )
}
