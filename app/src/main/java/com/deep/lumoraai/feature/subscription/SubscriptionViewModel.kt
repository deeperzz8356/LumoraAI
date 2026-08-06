package com.deep.lumoraai.feature.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.auth.FirebaseAuth

class SubscriptionViewModel : ViewModel() {
    var uiState: SubscriptionUiState by mutableStateOf(SubscriptionUiState.Loading)
        private set

    private val generationRepository = GenerationRepository()
    private val auth = FirebaseAuth.getInstance()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: run {
                    uiState = SubscriptionUiState.Error("Please sign in to view plans")
                    return@launch
                }

                val tokenResult = user.getIdToken(true).await()
                val idToken = tokenResult.token ?: run {
                    uiState = SubscriptionUiState.Error("Unable to authenticate")
                    return@launch
                }

                val url = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/subscriptions/plans")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $idToken")
                connection.setRequestProperty("x-user-id", user.uid)
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    uiState = SubscriptionUiState.Error("Failed to load plans")
                    return@launch
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)
                val plansArray = json.getJSONArray("plans")
                val plans = mutableListOf<SubscriptionPlan>()

                for (i in 0 until plansArray.length()) {
                    val obj = plansArray.getJSONObject(i)
                    plans.add(
                        SubscriptionPlan(
                            code = obj.getString("code"),
                            name = obj.getString("name"),
                            priceUsd = obj.optDouble("price_usd", 0.0),
                            monthlyCredits = obj.optInt("monthly_credits", 0),
                            videoCredits = obj.optInt("video_credits", 0),
                            features = obj.optJSONArray("features")?.let { array ->
                                List(array.length()) { index -> array.getString(index) }
                            } ?: emptyList(),
                            isPopular = obj.optBoolean("is_popular", false),
                            signupBonusCredits = obj.optInt("signup_bonus_credits", 0),
                        )
                    )
                }

                val currentPlan = if (plans.isEmpty()) null else plans.firstOrNull { it.code == "free" }?.code
                uiState = if (plans.isEmpty()) SubscriptionUiState.Empty else SubscriptionUiState.Success(plans, currentPlan)
            } catch (e: Exception) {
                uiState = SubscriptionUiState.Error(e.message ?: "Unable to load plans")
            }
        }
    }

    fun activatePlan(planCode: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: run {
                    uiState = SubscriptionUiState.Error("Please sign in to continue")
                    return@launch
                }

                val tokenResult = user.getIdToken(true).await()
                val idToken = tokenResult.token ?: run {
                    uiState = SubscriptionUiState.Error("Unable to authenticate")
                    return@launch
                }

                val url = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/subscriptions/activate")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $idToken")
                connection.setRequestProperty("x-user-id", user.uid)
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val payload = JSONObject().apply { put("planCode", planCode) }.toString()
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    uiState = SubscriptionUiState.Error("Failed to activate plan")
                    return@launch
                }

                uiState = SubscriptionUiState.Success(
                    plans = (uiState as? SubscriptionUiState.Success)?.plans ?: emptyList(),
                    currentPlan = planCode
                )
            } catch (e: Exception) {
                uiState = SubscriptionUiState.Error(e.message ?: "Unable to activate plan")
            }
        }
    }
}