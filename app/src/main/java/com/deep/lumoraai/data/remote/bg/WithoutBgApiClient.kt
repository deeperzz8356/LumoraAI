package com.deep.lumoraai.data.remote.bg

import android.content.ContentResolver
import android.net.Uri
import com.deep.lumoraai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Thin client for the WithoutBG "Pro Model" background-removal API.
 *
 * Endpoint: POST https://api.withoutbg.com/v1.0/image-without-background
 * Auth:     X-API-Key header (read from BuildConfig, never hardcoded).
 * Body:     multipart/form-data with a single `file` binary part.
 * Response: image/png (RGBA cutout) on HTTP 200.
 *
 * Uses HttpURLConnection to stay consistent with the rest of the codebase
 * (no Retrofit/OkHttp dependency is present) and to stream raw bytes without
 * base64 overhead.
 */
class WithoutBgApiClient(
    private val apiKey: String = BuildConfig.WITHOUTBG_API_KEY,
    private val baseUrl: String = "https://api.withoutbg.com",
) {

    /** 20 MB cap enforced by the API; checked client-side to avoid a wasted round trip. */
    private val maxFileSizeBytes = 20L * 1024 * 1024

    /**
     * Removes the background from the image at [imageUri].
     *
     * Implements a bounded retry with exponential backoff for HTTP 429
     * (rate limiting) as required by the API error-handling spec.
     */
    suspend fun removeBackground(
        resolver: ContentResolver,
        imageUri: Uri,
        maxRetries: Int = 2,
    ): WithoutBgResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext WithoutBgResult.Failure(-1, "Invalid Background Removal API Key")
        }

        val mimeType = resolver.getType(imageUri).orEmpty()
        if (!mimeType.startsWith("image/")) {
            return@withContext WithoutBgResult.Failure(-1, "Upload an image file only.")
        }

        var attempt = 0
        var lastFailure: WithoutBgResult.Failure? = null
        while (attempt <= maxRetries) {
            val result = runCatching { performRequest(resolver, imageUri, mimeType) }
                .getOrElse { error ->
                    WithoutBgResult.Failure(
                        code = -1,
                        message = "Network error. Please check your connection and try again.",
                        retriable = true,
                    ).also { it.attachCause(error) }
                }

            when (result) {
                is WithoutBgResult.Success -> return@withContext result
                is WithoutBgResult.Failure -> {
                    if (!result.retriable || attempt == maxRetries) {
                        return@withContext result
                    }
                    lastFailure = result
                    // Exponential backoff: 800ms, 1600ms, ...
                    delay(800L * (attempt + 1))
                    attempt++
                }
            }
        }
        lastFailure ?: WithoutBgResult.Failure(-1, "Background removal failed. Please try again.")
    }

    private fun performRequest(
        resolver: ContentResolver,
        imageUri: Uri,
        mimeType: String,
    ): WithoutBgResult {
        val declaredSize = runCatching {
            resolver.openAssetFileDescriptor(imageUri, "r")?.use { it.length }
        }.getOrNull() ?: -1L
        if (declaredSize in 1..Long.MAX_VALUE && declaredSize > maxFileSizeBytes) {
            return WithoutBgResult.Failure(413, "File size too large (Max 20MB)")
        }

        val boundary = "LumoraBoundary${UUID.randomUUID()}"
        val lineEnd = "\r\n"
        val connection = (URL("$baseUrl/v1.0/image-without-background").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("X-API-Key", apiKey)
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Accept", "image/png")
            connectTimeout = 30_000
            readTimeout = 120_000
            doInput = true
            doOutput = true
        }

        return try {
            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary$lineEnd")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"source.${extensionForMimeType(mimeType)}\"$lineEnd"
                )
                output.writeBytes("Content-Type: $mimeType$lineEnd$lineEnd")
                resolver.openInputStream(imageUri)?.use { input -> input.copyTo(output) }
                    ?: return WithoutBgResult.Failure(-1, "Could not open the selected image.")
                output.writeBytes(lineEnd)
                output.writeBytes("--$boundary--$lineEnd")
                output.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.isEmpty()) {
                    WithoutBgResult.Failure(responseCode, "Background remover returned an empty image.")
                } else {
                    WithoutBgResult.Success(bytes)
                }
            } else {
                val body = connection.errorStream?.use { it.readBytes().toString(Charsets.UTF_8) }
                withoutBgErrorFor(responseCode, body)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun extensionForMimeType(mimeType: String): String =
        when (mimeType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            "image/png" -> "png"
            "image/heic", "image/heif" -> "heic"
            "image/tiff" -> "tiff"
            "image/bmp" -> "bmp"
            "image/gif" -> "gif"
            else -> "png"
        }

    // A throwable cause is useful for logging but must not leak into the UI message.
    private fun WithoutBgResult.Failure.attachCause(@Suppress("UNUSED_PARAMETER") cause: Throwable) = Unit
}
