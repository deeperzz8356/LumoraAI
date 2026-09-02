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
import com.deep.lumoraai.core.utils.parseGenerationFailure
import com.deep.lumoraai.BuildConfig
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
            
            val url = URL(imageGenerateUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.applyAuthHeaders(idToken, user.uid, developerMode)
            connection.doOutput = true
            connection.connectTimeout = 60_000
            connection.readTimeout = 120_000

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
                    return@withContext Result.failure(Exception(errorMessage))
                }
                val imagePayload = extractGeneratedImage(responseJson)
                if (imagePayload != null) {
                    Result.success(imagePayload)
                } else {
                    val message = responseJson.parseApiMessage()
                        ?: "Server returned no image data."
                    Log.e("GenerationRepository", "API error: $message body=$responseBody")
                    Result.failure(Exception(message))
                }
            } else {
                val message = responseBody.parseApiMessage()
                    ?: "Server error ($responseCode). The backend may still be waking up — try again in a moment."
                Log.e("GenerationRepository", "HTTP $responseCode: $message")
                Result.failure(Exception(message))
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
        developerMode: Boolean = false,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val url = URL("$backendUrl/video")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.applyAuthHeaders(idToken, user.uid, developerMode)
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 180_000

            val jsonInputString = JSONObject().apply {
                put("prompt", prompt)
                put("model", engine)
                put("motion_strength", motionStrength)
                put("duration", duration)
                if (sourceImageB64 != null) put("source_image_b64", sourceImageB64)
                if (cameraDirection != null) put("camera_direction", cameraDirection)
            }.toString()

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
                    return@withContext Result.failure(Exception(errorMessage))
                }
                val videoUrl = extractGeneratedVideo(responseJson)
                if (videoUrl != null && responseJson.isSuccessfulApiStatus()) {
                    Result.success(videoUrl)
                } else {
                    val message = responseJson.parseApiMessage()?.let(::humanizeProviderError)
                        ?: formatGenerationErrorMessage(detail = null, mediaType = "video")
                    Log.e("GenerationRepository", "Video API error: $message body=$responseBody")
                    Result.failure(Exception(message))
                }
            } else {
                val message = responseBody.parseApiMessage()?.let(::humanizeProviderError)
                    ?: "Server error ($responseCode). The backend may still be waking up — try again in a moment."
                Log.e("GenerationRepository", "Video HTTP $responseCode: $message")
                Result.failure(Exception(message))
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

    suspend fun addCredits(amount: Int): Result<Int> = withContext(Dispatchers.IO) {
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
