package com.deep.lumoraai.data.repository

import android.content.Context
import com.deep.lumoraai.feature.templates.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TemplateRepository(private val context: Context) {
    suspend fun bundled(): List<TemplateCategoryData> = withContext(Dispatchers.IO) {
        parse(context.assets.open("templates/template.json").bufferedReader().use { it.readText() })
    }

    suspend fun remote(): List<TemplateCategoryData> {
        val catalog = runCatching {
            val document = FirebaseFirestore.getInstance().collection("template_catalogs").document("v1").get().await()
            document.getString("catalog") ?: error("Missing catalog")
        }.getOrElse {
            withContext(Dispatchers.IO) {
                val connection = URL("https://lumoraai-backend-rlcy.onrender.com/api/v1/templates").openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    check(connection.responseCode == 200) { "Template service unavailable" }
                    val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    JSONObject().put("categories", response.getJSONArray("items")).toString()
                } finally { connection.disconnect() }
            }
        }
        return withContext(Dispatchers.Default) { parse(catalog, remote = true) }
    }

    internal fun parse(text: String, remote: Boolean = false): List<TemplateCategoryData> {
        val categories = JSONObject(text).getJSONArray("categories")
        return buildList {
            for (ci in 0 until categories.length()) {
                val category = categories.getJSONObject(ci)
                val sections = category.getJSONArray("sections")
                add(TemplateCategoryData(category.getString("id"), category.getString("title"), buildList {
                    for (si in 0 until sections.length()) {
                        val section = sections.getJSONObject(si)
                        val templates = section.getJSONArray("templates")
                        add(TemplateSection(section.getString("id"), section.getString("title"), buildList {
                            for (ti in 0 until templates.length()) {
                                val item = templates.getJSONObject(ti)
                                val action = TemplateAction.entries.firstOrNull { it.name == item.optString("action") } ?: continue
                                val mediaUrl = item.optString("mediaUrl").takeIf { it.startsWith("https://") }
                                val previewUrl = item.optString("previewUrl").takeIf { it.startsWith("https://") }
                                if (remote && mediaUrl == null) continue
                                add(TemplateListItem(item.getString("id"), item.getString("title"), item.optString("subtitle"),
                                    item.getString("prompt"), item.optString("assetFileName"), item.optString("previewAssetFileName").takeIf { it.isNotBlank() },
                                    action, mediaUrl, previewUrl))
                            }
                        }))
                    }
                }))
            }
        }
    }
}
