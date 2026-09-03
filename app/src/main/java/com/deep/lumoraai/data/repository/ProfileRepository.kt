package com.deep.lumoraai.data.repository

import com.deep.lumoraai.feature.profile.EditableProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val backendUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/profile"

    suspend fun updateCurrentUserProfile(profile: EditableProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.success(Unit)
        if (user.isAnonymous) return@withContext Result.success(Unit)

        withTimeoutOrNull(8_000) {
            runCatching {
                val idToken = user.getIdToken(false).await().token.orEmpty()
                val connection = (URL(backendUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $idToken")
                    setRequestProperty("X-User-Id", user.uid)
                    doOutput = true
                    connectTimeout = 6_000
                    readTimeout = 6_000
                }
                val body = JSONObject().apply {
                    put("displayName", profile.fullName)
                    put("username", profile.username)
                    put("bio", profile.bio)
                    put("location", profile.location)
                    profile.avatarUri?.let { put("avatarUrl", it) }
                }.toString()
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(body)
                    writer.flush()
                }
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> Unit
                    HttpURLConnection.HTTP_CONFLICT -> throw IllegalArgumentException("Username is already taken.")
                    else -> Unit
                }
            }.recoverCatching { error ->
                if (error is IllegalArgumentException) throw error
                Unit
            }
        } ?: Result.success(Unit)
    }
}
