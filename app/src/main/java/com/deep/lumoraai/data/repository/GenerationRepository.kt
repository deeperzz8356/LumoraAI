package com.deep.lumoraai.data.repository

import com.deep.lumoraai.data.model.ActiveJobInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GenerationRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val backendUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/generation"

    companion object {
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
    }

    suspend fun generateImage(
        prompt: String, 
        style: String, 
        width: Int = 1024, 
        height: Int = 1024,
        negativePrompt: String? = null,
        sourceImageB64: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            if (user == null) {
                Log.e("GenerationRepository", "User not logged in")
                return@withContext null
            }
            
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token
            
            if (idToken == null) {
                Log.e("GenerationRepository", "Failed to get ID token")
                return@withContext null
            }
            
            val url = URL("$backendUrl/image")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $idToken")
            connection.setRequestProperty("x-user-id", user.uid)
            connection.doOutput = true
            connection.connectTimeout = 30000 // 30 seconds timeout
            connection.readTimeout = 60000 // 60 seconds timeout

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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.use { it.readText() }
                
                val responseJson = JSONObject(responseString)
                if (responseJson.getString("status") == "success") {
                    return@withContext responseJson.getString("image_url")
                } else {
                    Log.e("GenerationRepository", "API returned status: ${responseJson.getString("status")}")
                }
            } else {
                Log.e("GenerationRepository", "HTTP Error $responseCode")
            }
        } catch (e: Exception) {
            Log.e("GenerationRepository", "Exception: ${e.message}")
        }
        return@withContext null
    }

    suspend fun generateVideo(
        prompt: String, 
        engine: String,
        sourceImageB64: String? = null,
        motionStrength: Int = 65,
        cameraDirection: String? = null,
        duration: Int = 10
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext Result.failure(Exception("Failed to get ID token"))

            val url = URL("$backendUrl/video")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $idToken")
            connection.setRequestProperty("x-user-id", user.uid)
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.use { it.readText() }
                val responseJson = JSONObject(responseString)
                if (responseJson.getString("status") == "success") {
                    Result.success(responseJson.getString("video_url"))
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
                                imageUrl = obj.optString("image_url"),
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

    suspend fun addCredits(amount: Int): Result<Unit> = withContext(Dispatchers.IO) {
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
                    Result.success(Unit)
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
}

data class GenerationHistoryItem(
    val id: String,
    val prompt: String,
    val imageUrl: String,
    val style: String?
)
