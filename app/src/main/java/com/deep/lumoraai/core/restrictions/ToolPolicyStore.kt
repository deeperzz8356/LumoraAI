package com.deep.lumoraai.core.restrictions

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ToolRule(val enabled: Boolean = true, val creditCost: Int = 1)
data class ToolPolicy(val revision: String? = null, val tools: Map<String, ToolRule>)

/** The backend resolves the global Remote Config prices; the app never submits an amount. */
object ToolPolicyStore {
    private val defaults = mapOf(
        "text_to_image" to 1, "image_to_image" to 1, "logo" to 1, "avatar" to 1,
        "text_to_video" to 5, "image_to_video" to 5, "promo_video" to 5,
        "background_removal" to 1, "background_replace" to 1, "photo_enhance" to 0, "compress" to 0,
    )
    var current by mutableStateOf(ToolPolicy(tools = defaults.mapValues { ToolRule(creditCost = it.value) }))
        private set
    private var preferences: android.content.SharedPreferences? = null
    private val mutex = Mutex()
    fun rule(tool: String): ToolRule = current.tools[tool] ?: ToolRule(enabled = false, creditCost = 0)
    fun cost(tool: String): Int = rule(tool).creditCost

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("tool_policy", Context.MODE_PRIVATE)
        preferences?.getString("snapshot", null)?.let { text -> runCatching { current = parse(text) } }
    }

    suspend fun refresh(): Boolean = mutex.withLock {
        try {
            val text = withContext(Dispatchers.IO) {
                val connection = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/tool-policy").openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    check(connection.responseCode == 200) { "Could not load tool settings." }
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally { connection.disconnect() }
            }
            val policy = parse(text)
            withContext(Dispatchers.Main) { current = policy }
            preferences?.edit()?.putString("snapshot", text)?.apply()
            true
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) { false }
    }

    suspend fun prepare(tool: String): String? {
        val shown = current
        check(rule(tool).enabled) { "Temporarily unavailable." }
        check(refresh()) { "Could not verify tool settings. Check your connection and try again." }
        check(rule(tool).enabled) { "Temporarily unavailable." }
        check(shown.tools[tool] == rule(tool) && (shown.revision == null || shown.revision == current.revision)) {
            "Tool settings changed. Review the updated price and tap again."
        }
        return current.revision
    }

    private fun parse(text: String): ToolPolicy {
        val json = JSONObject(text)
        val tools = json.getJSONObject("tools")
        return ToolPolicy(json.getString("revision"), defaults.mapValues { (key, _) ->
            val entry = tools.getJSONObject(key)
            val cost = entry.get("credit_cost")
            require(cost is Int && cost in 0..100000)
            val enabled = entry.get("enabled")
            require(enabled is Boolean)
            ToolRule(enabled, cost)
        })
    }
}
