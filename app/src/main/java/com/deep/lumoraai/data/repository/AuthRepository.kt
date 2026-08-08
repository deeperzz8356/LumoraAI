package com.deep.lumoraai.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val backendUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/auth"

    /**
     * Signs in anonymously with Firebase, gets the ID token, and syncs it.
     */
    suspend fun loginAnonymouslyAndSync(): Boolean = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInAnonymously().await()
            return@withContext processAuthResultAndSync(authResult.user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during anonymous login: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Signs up a new user with Email and Password, gets the ID token, and syncs it.
     */
    suspend fun signUpWithEmail(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            return@withContext processAuthResultAndSync(authResult.user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during email signup: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Logs in an existing user with Email and Password, gets the ID token, and syncs it.
     */
    suspend fun loginWithEmail(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            return@withContext processAuthResultAndSync(authResult.user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during email login: ${e.message}")
            return@withContext false
        }
    }

    suspend fun syncCurrentUser(): Boolean = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext false
        try {
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token ?: return@withContext false
            syncWithBackend(idToken)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error syncing current user: ${e.message}")
            false
        }
    }

    private suspend fun processAuthResultAndSync(user: com.google.firebase.auth.FirebaseUser?): Boolean {
        if (user != null) {
            val tokenResult = user.getIdToken(true).await()
            val idToken = tokenResult.token
            if (idToken != null) {
                return syncWithBackend(idToken)
            }
        }
        return false
    }

    private fun syncWithBackend(idToken: String): Boolean {
        return try {
            val url = URL("$backendUrl/sync")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Send the id_token in the body
            val jsonInputString = JSONObject().apply {
                put("id_token", idToken)
            }.toString()

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInputString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d("AuthRepository", "Backend sync response code: $responseCode")
            
            // Return true if synced successfully (HTTP 200 OK)
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error syncing with backend: ${e.message}")
            false
        }
    }
}
