package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.model.ActiveJobInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.deep.lumoraai.core.utils.extractGeneratedImage
import com.deep.lumoraai.core.utils.extractGeneratedVideo
import com.deep.lumoraai.core.utils.isSuccessfulApiStatus
import com.deep.lumoraai.core.utils.formatGenerationErrorMessage
import com.deep.lumoraai.core.utils.humanizeProviderError
import com.deep.lumoraai.core.utils.isRetriableRateLimit
import com.deep.lumoraai.core.utils.parseGenerationFailure
import com.deep.lumoraai.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GenerationRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val backendUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/generation"
    private val imageGenerateUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/images/generate"

    companion object {
        private const val GROQ_PROMPT_MODEL = "openai/gpt-oss-120b"
        private val generationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val _activeJobs = MutableStateFlow<List<ActiveJobInfo>>(emptyList())
        val activeJobs: StateFlow<List<ActiveJobInfo>> = _activeJobs.asStateFlow()

        // --- App-wide generation throttle -----------------------------------
        // The upstream provider (Vertex generate_content) returns a shared-
        // capacity 429 RESOURCE_EXHAUSTED when several generations hit it in
        // quick succession. Even though each screen dispatches its own
        // generations sequentially, independent jobs (text-to-image, video,
        // background studio) can overlap and burst. This gate serializes ALL
        // provider-bound generation requests across the whole app and enforces
        // a minimum spacing between them, so the client never fires a burst.
        private val generationGate = Mutex()
        private const val MIN_REQUEST_SPACING_MS = 1_200L
        @Volatile
        private var lastRequestStartedAt = 0L

        /**
         * Runs [block] as the sole in-flight generation request, waiting until at
         * least [MIN_REQUEST_SPACING_MS] has elapsed since the previous request
         * started. Serializing here (rather than per-screen) means overlapping
         * jobs from different features cannot burst the provider.
         */
        private suspend fun <T> withGenerationSlot(block: suspend () -> T): T =
            generationGate.withLock {
                val now = System.currentTimeMillis()
                val sinceLast = now - lastRequestStartedAt
                if (lastRequestStartedAt != 0L && sinceLast < MIN_REQUEST_SPACING_MS) {
                    delay(MIN_REQUEST_SPACING_MS - sinceLast)
                }
                lastRequestStartedAt = System.currentTimeMillis()
                block()
            }

        fun addJob(job: ActiveJobInfo) {
            _activeJobs.update { it + job }
        }

        fun updateJob(title: String, updateFn: (ActiveJobInfo) -> ActiveJobInfo) {
            _activeJobs.update { list ->
                list.map { job ->
                    if (job.title == title) updateFn(job) else job
                }
            }
        }

        fun runImageGeneration(
            repository: GenerationRepository,
            jobTitle: String,
            prompt: String,
            style: String,
            width: Int,
            height: Int,
            negativePrompt: String?,
            sourceImageB64: String?,
            developerMode: Boolean = false,
            onResult: (Result<String>) -> Unit
        ) {
            generationScope.launch {
                val result = repository.generateImage(
                    prompt = prompt,
                    style = style,
                    width = width,
                    height = height,
                    negativePrompt = negativePrompt,
                    sourceImageB64 = sourceImageB64,
                    developerMode = developerMode,
                )
                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            }
        }
    }

    suspend fun generateImage(
        prompt: String, 
        style: String, 
        width: Int = 1024, 
        height: Int = 1024,
        negativePrompt: String? = null,
        sourceImageB64: String? = null,
        developerMode: Boolean = false,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
                ?: return@withContext Result.failure(Exception("Please sign in to generate images."))
            
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token
                ?: return@withContext Result.failure(Exception("Failed to get authentication token."))

            val jsonInputString = JSONObject().apply {
                put("prompt", prompt)
                put("style", style)
                put("width", width)
                put("height", height)
                if (!negativePrompt.isNullOrBlank()) {
                    put("negative_prompt", negativePrompt)
                }
                if (sourceImageB64 != null) {
                    put("source_image_b64", sourceImageB64)
                }
            }.toString()

            // Batch requests can trip an upstream 429 (RESOURCE_EXHAUSTED). Retry
            // a bounded number of times with exponential backoff before surfacing
            // a clear "server busy" message (never a raw "no content" message).
            retryOnRateLimit(mediaType = "image") {
                val url = URL(imageGenerateUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.applyAuthHeaders(idToken, user.uid, developerMode)
                connection.doOutput = true
                connection.connectTimeout = 60_000
                connection.readTimeout = 120_000

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val responseBody = connection.readResponseBody()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseJson = JSONObject(responseBody)
                    Log.d("GenerationRepository", "Image response (${responseBody.length} chars): ${responseBody.take(800)}")
                    responseJson.parseGenerationFailure()?.let { errorMessage ->
                        Log.e("GenerationRepository", "Generation failed: $errorMessage body=$responseBody")
                        // A body-level rate-limit failure is retriable even on HTTP 200.
                        return@retryOnRateLimit GenerationAttempt.of(errorMessage, mediaType = "image")
                    }
                    val imagePayload = extractGeneratedImage(responseJson)
                    if (imagePayload != null) {
                        GenerationAttempt.Success(imagePayload)
                    } else {
                        val message = responseJson.parseApiMessage()
                        Log.e("GenerationRepository", "API error: ${message ?: "no image data"} body=$responseBody")
                        GenerationAttempt.of(message, mediaType = "image")
                    }
                } else if (responseCode == 429) {
                    val message = responseBody.parseApiMessage()
                    Log.w("GenerationRepository", "HTTP 429 (rate limited): ${message ?: responseBody.take(200)}")
                    GenerationAttempt.RateLimited(message)
                } else {
                    val message = responseBody.parseApiMessage()
                        ?: "Server error ($responseCode). The backend may still be waking up — try again in a moment."
                    Log.e("GenerationRepository", "HTTP $responseCode: $message")
                    GenerationAttempt.Failed(message)
                }
            }
        } catch (e: Exception) {
            Log.e("GenerationRepository", "Exception: ${e.message}")
            Result.failure(
                Exception(
                    when {
                        e.message?.contains("timeout", ignoreCase = true) == true ->
                            "Request timed out. The server may be starting up — please try again."
                        else -> e.message ?: "Network error during image generation."
                    }
                )
            )
        }
    }

    suspend fun generateVideo(
        prompt: String, 
        engine: String,
        sourceImageB64: String? = null,
        motionStrength: Int = 65,
        cameraDirection: String? = null,
        duration: Int = 10,
        aspectRatio: String? = null,
        style: String? = null,
        developerMode: Boolean = false,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val jsonInputString = JSONObject().apply {
                put("prompt", prompt)
                put("model", engine)
                put("motion_strength", motionStrength)
                put("duration", duration)
                if (sourceImageB64 != null) put("source_image_b64", sourceImageB64)
                if (cameraDirection != null) put("camera_direction", cameraDirection)
                // Structured parameter-mapping (Bugs 3 & 5): the selected aspect ratio and
                // style are the authoritative carriers to Vertex; the prompt-text hints
                // built by buildPrompt() remain only as a redundant fallback. Style follows
                // generateImage()'s convention: omit the default sentinel ("Default") so
                // no-style requests keep the default/unstyled behavior.
                if (aspectRatio != null) put("aspect_ratio", aspectRatio)
                if (style != null && style != "Default") put("style", style)
            }.toString()

            // Retry a bounded number of times with exponential backoff on 429
            // before surfacing a clear "server busy" message.
            retryOnRateLimit(mediaType = "video") {
                val url = URL("$backendUrl/video")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.applyAuthHeaders(idToken, user.uid, developerMode)
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 180_000

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val responseBody = connection.readResponseBody()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseJson = JSONObject(responseBody)
                    Log.d("GenerationRepository", "Video response (${responseBody.length} chars): ${responseBody.take(800)}")
                    responseJson.parseGenerationFailure()?.let { errorMessage ->
                        Log.e("GenerationRepository", "Video generation failed: $errorMessage body=$responseBody")
                        return@retryOnRateLimit GenerationAttempt.of(errorMessage, mediaType = "video")
                    }
                    val videoUrl = extractGeneratedVideo(responseJson)
                    if (videoUrl != null && responseJson.isSuccessfulApiStatus()) {
                        GenerationAttempt.Success(videoUrl)
                    } else {
                        val message = responseJson.parseApiMessage()
                        Log.e("GenerationRepository", "Video API error: ${message ?: "no video data"} body=$responseBody")
                        GenerationAttempt.of(message, mediaType = "video")
                    }
                } else if (responseCode == 429) {
                    val message = responseBody.parseApiMessage()
                    Log.w("GenerationRepository", "Video HTTP 429 (rate limited): ${message ?: responseBody.take(200)}")
                    GenerationAttempt.RateLimited(message)
                } else {
                    val message = responseBody.parseApiMessage()?.let(::humanizeProviderError)
                        ?: "Server error ($responseCode). The backend may still be waking up — try again in a moment."
                    Log.e("GenerationRepository", "Video HTTP $responseCode: $message")
                    GenerationAttempt.Failed(message)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<List<GenerationHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val url = URL("$backendUrl/history")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $idToken")
            connection.setRequestProperty("x-user-id", user.uid)
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.use { it.readText() }
                val responseJson = JSONObject(responseString)
                if (responseJson.getString("status") == "success") {
                    val dataArray = responseJson.getJSONArray("data")
                    val items = mutableListOf<GenerationHistoryItem>()
                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        items.add(
                            GenerationHistoryItem(
                                id = obj.optString("id"),
                                prompt = obj.optString("prompt"),
                                imageUrl = obj.optString("image_url")
                                    .ifBlank { obj.optString("imageUrl") },
                                style = obj.optString("style")
                            )
                        )
                    }
                    Result.success(items)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "API status was not success")))
                }
            } else {
                Result.failure(Exception("HTTP Error $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCredits(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val creditsUrl = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/credits")
            val connection = creditsUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $idToken")
            connection.setRequestProperty("x-user-id", user.uid)
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.use { it.readText() }
                val responseJson = JSONObject(responseString)
                if (responseJson.getString("status") == "success") {
                    Result.success(responseJson.getInt("balance"))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "API status was not success")))
                }
            } else {
                Result.failure(Exception("HTTP Error $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Adds credits for a single logical reward/purchase event.
     *
     * Idempotency (Bug 4b, isBugCondition4): the caller owns the logical-event
     * identity and MUST supply a [idempotencyKey] that is STABLE across retries of
     * the same logical event (so a duplicate delivery does not double-apply) and
     * DISTINCT across different logical events (so genuinely separate rewards each
     * apply once). Derive it deterministically from the event, e.g.
     * "<uid>:daily_reset:<yyyy-MM-dd>" or "<uid>:spin:<year-week>" — never a
     * fresh per-call/per-retry UUID. The backend (Task 3.5) applies the effect at
     * most once per key. The key is sent in the JSON body as "idempotency_key".
     */
    suspend fun addCredits(amount: Int, idempotencyKey: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val creditsUrl = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/credits/add")
            val connection = creditsUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $idToken")
            connection.setRequestProperty("x-user-id", user.uid)
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val jsonInputString = JSONObject().apply {
                put("amount", amount)
                // Bug 4b: stable, caller-owned idempotency key so duplicate deliveries
                // of the SAME logical event do not double-apply, while distinct events
                // each apply once.
                put("idempotency_key", idempotencyKey)
            }.toString()

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.use { it.readText() }
                val responseJson = JSONObject(responseString)
                if (responseJson.getString("status") == "success") {
                    Result.success(responseJson.optInt("balance", amount))
                } else {
                    Result.failure(Exception(responseJson.optString("message", "API status was not success")))
                }

            } else {
                Result.failure(Exception("HTTP Error $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Outcome of a single generation attempt, classified so [retryOnRateLimit]
     * knows whether to back off and retry or return immediately.
     */
    private sealed interface GenerationAttempt {
        data class Success(val payload: String) : GenerationAttempt
        /** Retriable transient rate limiting (HTTP 429 or a rate-limit body). */
        data class RateLimited(val message: String?) : GenerationAttempt
        /** Non-retriable failure; surfaced to the caller as-is. */
        data class Failed(val message: String) : GenerationAttempt

        companion object {
            /**
             * Classifies a message from a success-shaped or error body: a
             * rate-limit message becomes retriable, anything else is a terminal
             * failure with a humanized, non-empty message (never "no content").
             */
            fun of(message: String?, mediaType: String): GenerationAttempt =
                if (isRetriableRateLimit(message)) {
                    RateLimited(message)
                } else {
                    Failed(
                        message?.let(::humanizeProviderError)
                            ?: formatGenerationErrorMessage(detail = null, mediaType = mediaType)
                    )
                }
        }
    }

    /**
     * Runs [attempt] with bounded exponential backoff on transient rate limiting.
     *
     * Batch/quick-succession requests can trip an upstream Vertex AI 429
     * (RESOURCE_EXHAUSTED). Rather than immediately failing with an unhelpful
     * message, retry a few times with increasing delays. If retries are
     * exhausted, return a clear "server busy" message so the UI never shows a
     * confusing "no content provided".
     */
    private suspend fun retryOnRateLimit(
        mediaType: String,
        maxAttempts: Int = 4,
        attempt: suspend () -> GenerationAttempt,
    ): Result<String> {
        var lastRateLimitMessage: String? = null
        repeat(maxAttempts) { index ->
            // Each attempt (including retries) goes through the app-wide slot so
            // concurrent jobs are serialized and spaced apart.
            when (val outcome = withGenerationSlot { attempt() }) {
                is GenerationAttempt.Success -> return Result.success(outcome.payload)
                is GenerationAttempt.Failed -> return Result.failure(Exception(outcome.message))
                is GenerationAttempt.RateLimited -> {
                    lastRateLimitMessage = outcome.message
                    if (index < maxAttempts - 1) {
                        // Exponential backoff with jitter: ~1s, 2s, 4s (+0-500ms).
                        val backoff = (1_000L shl index) + (0..500L).random()
                        Log.w(
                            "GenerationRepository",
                            "Rate limited ($mediaType). Backing off ${backoff}ms before retry ${index + 2}/$maxAttempts",
                        )
                        delay(backoff)
                    }
                }
            }
        }
        val message = lastRateLimitMessage?.let(::humanizeProviderError)
            ?: "The server is busy right now (too many requests). Please wait a moment and try again."
        Log.e("GenerationRepository", "Rate limit retries exhausted for $mediaType: $message")
        return Result.failure(Exception(message))
    }

    /** Server verifies the Play token and derives the entitlement; amount is never client-supplied. */
    suspend fun verifyGooglePlayPurchase(productId: String, purchaseToken: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
                val idToken = user.getIdToken(true).await().token
                    ?: return@withContext Result.failure(Exception("Failed to get authentication token"))
                val connection = (URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/billing/google-play/verify")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $idToken")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                OutputStreamWriter(connection.outputStream).use {
                    it.write(JSONObject().apply {
                        put("product_id", productId)
                        put("purchase_token", purchaseToken)
                    }.toString())
                }
                val body = connection.readResponseBody()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(body)
                    if (json.optString("status") == "success") Result.success(json.optInt("balance"))
                    else Result.failure(Exception(json.optString("message", "Purchase verification failed")))
                } else {
                    Result.failure(Exception("Purchase verification failed (HTTP ${connection.responseCode})"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun enhancePrompt(
        prompt: String,
        mediaType: String,
        style: String,
        negativePrompt: String? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GROQ_PROMPT_API_KEY
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Groq prompt enhancer key is not configured."))
            }
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000

            val medium = if (mediaType.equals("VIDEO", ignoreCase = true)) "video" else "image"
            val avoidLine = negativePrompt
                ?.takeIf { it.isNotBlank() }
                ?.let { "\nAvoid these negative prompt items: $it" }
                .orEmpty()
            val instruction = """
                Rewrite the user's prompt into one polished production-ready prompt for an AI $medium generator.
                Keep the user's intent, do not add extra concepts that change the subject, and keep it under 900 characters.
                Return only the improved prompt, no markdown and no explanation.

                Style: $style
                User prompt: $prompt$avoidLine
            """.trimIndent()

            val jsonInputString = JSONObject().apply {
                put("model", GROQ_PROMPT_MODEL)
                put("temperature", 0.45)
                put("max_tokens", 260)
                put(
                    "messages",
                    org.json.JSONArray()
                        .put(
                            JSONObject().apply {
                                put("role", "system")
                                put("content", "You improve prompts for image and video generation. Return only the enhanced prompt.")
                            }
                        )
                        .put(
                            JSONObject().apply {
                                put("role", "user")
                                put("content", instruction)
                            }
                        )
                )
            }.toString()

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = connection.readResponseBody()
            if (responseCode in 200..299) {
                val responseJson = JSONObject(responseBody)
                val enhanced = responseJson
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?.trim()
                    .orEmpty()
                if (enhanced.isNotBlank()) {
                    Result.success(enhanced.trim('"').take(1000))
                } else {
                    Result.failure(Exception(responseJson.parseApiMessage() ?: "Groq returned no enhanced prompt."))
                }
            } else {
                Result.failure(Exception(responseBody.parseApiMessage() ?: "Groq prompt enhancer failed ($responseCode)."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not improve prompt."))
        }
    }

}

data class GenerationHistoryItem(
    val id: String,
    val prompt: String,
    val imageUrl: String,
    val style: String?
)

private fun HttpURLConnection.applyAuthHeaders(
    idToken: String,
    userId: String,
    developerMode: Boolean,
) {
    setRequestProperty("Authorization", "Bearer $idToken")
    setRequestProperty("x-user-id", userId)
    if (developerMode) {
        setRequestProperty("X-Developer-Mode", "true")
    }
}

private fun HttpURLConnection.readResponseBody(): String {
    val stream = if (responseCode in 200..299) inputStream else errorStream
    return stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
}

private fun JSONObject.parseApiMessage(): String? =
    sequenceOf("message", "detail", "error")
        .map { optString(it) }
        .firstOrNull { it.isNotBlank() }

private fun String.parseApiMessage(): String? =
    runCatching { JSONObject(this).parseApiMessage() }.getOrNull()
