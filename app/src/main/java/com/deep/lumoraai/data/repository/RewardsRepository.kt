package com.deep.lumoraai.data.repository

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

/**
 * Client for the server-authoritative reward endpoints under `/api/v1/rewards`.
 *
 * The backend owns every reward amount, cap, streak, and dedup/idempotency rule
 * (see backend `rewards_config.py` / `rewards_repo.py`). The app only tells the
 * server which reward happened; the server decides the credits and returns the
 * new balance. This removes the old client-authoritative `addCredits(amount)`
 * flow where a tampered client could mint arbitrary credits.
 */
class RewardsRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val baseUrl = "https://lumoraai-backend-rlcy.onrender.com/api/v1/rewards"

    /** Structured outcome of a reward claim. */
    data class RewardResult(
        /** Server status: "success", "already_claimed", "capped", or "error". */
        val status: String,
        /** New credit balance when the server reports one, else null. */
        val balance: Int?,
        /** Credits awarded by this claim (0 for spin "better luck", already-claimed, etc.). */
        val creditsAwarded: Int,
        /** Spin prize when applicable. */
        val prize: Int? = null,
        /** Check-in streak day (1..7) when applicable. */
        val streakDay: Int? = null,
        /** Referral count so far when applicable. */
        val referralCount: Int? = null,
        /** Human-readable message from the server, if any. */
        val message: String? = null,
    ) {
        val isSuccess: Boolean get() = status == "success"
        val isAlreadyClaimed: Boolean get() = status == "already_claimed"
        val isCapped: Boolean get() = status == "capped"
    }

    suspend fun spin(): Result<RewardResult> = post("/spin")

    suspend fun claimDailyReset(): Result<RewardResult> = post("/daily-reset")

    suspend fun claimCheckIn(): Result<RewardResult> = post("/check-in")

    suspend fun claimSignUpBonus(): Result<RewardResult> = post("/sign-up-bonus")

    suspend fun claimEmailLogin(): Result<RewardResult> = post("/email-login")

    suspend fun claimReferral(referredId: String): Result<RewardResult> =
        post("/referral", JSONObject().put("referred_id", referredId))

    suspend fun claimSocialShare(platform: String, shareId: String?): Result<RewardResult> =
        post(
            "/social-share",
            JSONObject().apply {
                put("platform", platform)
                if (!shareId.isNullOrBlank()) put("share_id", shareId)
            },
        )

    private suspend fun post(path: String, body: JSONObject? = null): Result<RewardResult> =
        withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                    ?: return@withContext Result.failure(Exception("Please sign in to claim rewards."))
                val idToken = user.getIdToken(true).await().token
                    ?: return@withContext Result.failure(Exception("Failed to get authentication token."))

                val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $idToken")
                    setRequestProperty("x-user-id", user.uid)
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write((body ?: JSONObject()).toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

                if (responseCode !in 200..299) {
                    Log.e("RewardsRepository", "HTTP $responseCode on $path: ${responseBody.take(300)}")
                    return@withContext Result.failure(Exception("Could not claim reward (HTTP $responseCode)."))
                }

                Result.success(parse(responseBody))
            } catch (e: Exception) {
                Log.e("RewardsRepository", "Reward $path failed: ${e.message}")
                Result.failure(Exception(e.message ?: "Network error while claiming reward."))
            }
        }

    private fun parse(body: String): RewardResult {
        val json = JSONObject(body)
        return RewardResult(
            status = json.optString("status", "error"),
            balance = if (json.has("balance")) json.optInt("balance") else null,
            creditsAwarded = json.optInt("credits_awarded", 0),
            prize = if (json.has("prize")) json.optInt("prize") else null,
            streakDay = if (json.has("streak_day")) json.optInt("streak_day") else null,
            referralCount = if (json.has("referral_count")) json.optInt("referral_count") else null,
            message = json.optString("message").takeIf { it.isNotBlank() },
        )
    }
}
