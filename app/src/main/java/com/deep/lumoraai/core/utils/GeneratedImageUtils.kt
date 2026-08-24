package com.deep.lumoraai.core.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject

private val IMAGE_PAYLOAD_KEYS = listOf(
    "image_url", "imageUrl", "image_b64", "imageB64", "image", "url", "output"
)
private val NESTED_OBJECT_KEYS = listOf("data", "result", "image", "output", "response")
private val VIDEO_PAYLOAD_KEYS = listOf("video_url", "videoUrl", "url")

fun extractGeneratedImage(json: JSONObject): String? {
    IMAGE_PAYLOAD_KEYS.forEach { key ->
        json.optString(key).takeIf { it.isNotBlank() }?.let { return it }
    }

    NESTED_OBJECT_KEYS.forEach { key ->
        json.optJSONObject(key)?.let { nested ->
            extractGeneratedImage(nested)?.let { return it }
        }
    }

    json.optJSONArray("data")?.let { array ->
        extractFromJsonArray(array)?.let { return it }
    }

    return null
}

private fun extractFromJsonArray(array: JSONArray): String? {
    for (index in 0 until array.length()) {
        when (val value = array.opt(index)) {
            is JSONObject -> extractGeneratedImage(value)?.let { return it }
            is String -> if (value.isNotBlank()) return value
        }
    }
    return null
}

fun extractGeneratedVideo(json: JSONObject): String? {
    // Log for debugging
    Log.d("extractGeneratedVideo", "Parsing video from JSON: ${json.toString().take(500)}")
    
    // First check nested data object
    json.optJSONObject("data")?.let { nested ->
        extractGeneratedVideo(nested)?.let { return it }
    }
    
    // Check video-specific keys first
    VIDEO_PAYLOAD_KEYS.forEach { key ->
        json.optString(key).takeIf { it.isNotBlank() }?.let { 
            Log.d("extractGeneratedVideo", "Found video URL in key '$key': $it")
            return it 
        }
    }
    
    // Also check image_url keys (some backends use image_url for both images and videos)
    IMAGE_PAYLOAD_KEYS.forEach { key ->
        if (key != "url") { // Skip url as it's already in VIDEO_PAYLOAD_KEYS
            json.optString(key).takeIf { it.isNotBlank() }?.let { 
                Log.d("extractGeneratedVideo", "Found video URL in image key '$key' (fallback): $it")
                return it 
            }
        }
    }
    
    Log.d("extractGeneratedVideo", "No video URL found in response")
    return null
}

fun JSONObject.isSuccessfulApiStatus(): Boolean {
    val status = optString("status", "success")
    return status.equals("success", ignoreCase = true) ||
        status.equals("completed", ignoreCase = true) ||
        status.equals("ok", ignoreCase = true) ||
        status.isBlank()
}

fun JSONObject.parseGenerationFailure(): String? {
    val status = optString("status", "")
    if (!status.equals("error", ignoreCase = true) && !status.equals("failed", ignoreCase = true)) {
        return null
    }
    return formatGenerationErrorMessage(
        detail = sequenceOf("message", "detail", "error", "errorMessage", "reason")
            .map { optString(it) }
            .firstOrNull { it.isNotBlank() },
        model = optString("model").takeIf { it.isNotBlank() },
        mediaType = "image",
    )
}

fun formatGenerationErrorMessage(
    detail: String?,
    model: String? = null,
    mediaType: String = "image",
): String {
    val normalizedDetail = detail?.let(::humanizeProviderError)
    return when {
        normalizedDetail != null -> normalizedDetail
        model != null ->
            "${mediaType.replaceFirstChar { it.uppercase() }} generation failed ($model). " +
                "The server could not produce a $mediaType — check API keys and quota on Render."
        else -> "${mediaType.replaceFirstChar { it.uppercase() }} generation failed on the server. Please try again."
    }
}

fun humanizeProviderError(raw: String): String {
    val lower = raw.lowercase()
    return when {
        "resource_exhausted" in lower || "quota exceeded" in lower || "429" in raw ->
            "Video generation quota is exhausted on Google Vertex AI. " +
                "Request a quota increase in Google Cloud Console or try the FastDraft engine."
        else -> raw
    }
}

fun isHttpImageUrl(value: String): Boolean =
    value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)

fun isLocalFilePath(value: String): Boolean =
    value.startsWith("/") ||
        value.startsWith("file:", ignoreCase = true) ||
        value.startsWith("content:", ignoreCase = true)

fun isSvgPayload(value: String): Boolean =
    value.contains("image/svg", ignoreCase = true) ||
        value.trimStart().startsWith("<svg", ignoreCase = true)

/**
 * Decode a data URL or raw base64 payload into bytes + mime type.
 */
fun decodeMediaPayload(payload: String): Pair<ByteArray, String>? {
    if (payload.isBlank()) return null
    return when {
        payload.startsWith("data:", ignoreCase = true) -> {
            val header = payload.substringBefore(",", missingDelimiterValue = "")
            val encoded = payload.substringAfter(",", missingDelimiterValue = "")
            if (encoded.isBlank()) return null
            val mime = header
                .removePrefix("data:")
                .substringBefore(";")
                .ifBlank { "application/octet-stream" }
            runCatching { Base64.decode(encoded, Base64.DEFAULT) to mime }.getOrNull()
        }
        else -> {
            runCatching {
                Base64.decode(payload, Base64.DEFAULT) to "application/octet-stream"
            }.getOrNull()?.takeIf { it.first.isNotEmpty() }
        }
    }
}

fun extensionForMimeType(mimeType: String, mediaType: String): String {
    val lower = mimeType.lowercase()
    return when {
        "png" in lower -> "png"
        "jpeg" in lower || "jpg" in lower -> "jpg"
        "webp" in lower -> "webp"
        "gif" in lower -> "gif"
        "mp4" in lower -> "mp4"
        "webm" in lower -> "webm"
        "mov" in lower || "quicktime" in lower -> "mov"
        mediaType.equals("VIDEO", ignoreCase = true) -> "mp4"
        else -> "png"
    }
}

@Composable
fun GeneratedImage(
    imagePayload: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    when {
        isHttpImageUrl(imagePayload) || isLocalFilePath(imagePayload) -> {
            AsyncImage(
                model = imagePayload,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        isSvgPayload(imagePayload) -> {
            val html = remember(imagePayload) { svgPayloadToHtml(imagePayload) }
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            )
        }
        else -> {
            val bitmap = remember(imagePayload) {
                val base64 = if (imagePayload.contains(",")) {
                    imagePayload.substringAfter(",")
                } else {
                    imagePayload
                }
                runCatching {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }.getOrNull()
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            }
        }
    }
}

private fun svgPayloadToHtml(payload: String): String {
    val svgContent = when {
        payload.trimStart().startsWith("<svg", ignoreCase = true) -> payload
        payload.startsWith("data:image/svg+xml", ignoreCase = true) -> {
            val encoded = payload.substringAfter(",", payload)
            if (encoded.contains("<svg")) {
                encoded
            } else {
                String(Base64.decode(encoded, Base64.DEFAULT))
            }
        }
        else -> payload
    }
    return """
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>html,body{margin:0;padding:0;background:transparent;} svg{width:100%;height:100%;}</style>
          </head>
          <body>$svgContent</body>
        </html>
    """.trimIndent()
}
