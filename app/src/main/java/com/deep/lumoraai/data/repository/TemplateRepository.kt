package com.deep.lumoraai.data.repository

import android.content.Context
import android.util.Log
import com.deep.lumoraai.feature.templates.TemplateAssetCache
import com.deep.lumoraai.feature.templates.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TemplateRepository(
    private val context: Context
) {

    companion object {
        private const val TAG = "TemplateCatalog"
        private const val COLLECTION = "template_catalogs"
        private const val DOCUMENT = "v1"
        private const val FIELD_CATALOG = "catalog"
    }

    suspend fun load(): List<TemplateCategoryData> {
        return try {
            val remote = remote()

            if (remote.isEmpty()) {
                Log.w(TAG, "Firestore catalog parsed but was empty. Using bundled fallback.")
                bundled()
            } else {
                Log.i(TAG, "loaded from Firestore")
                remote
            }
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Firestore failed, using bundled fallback",
                error
            )

            bundled()
        }
    }

    private suspend fun remote(): List<TemplateCategoryData> =
        withContext(Dispatchers.IO) {

            val snapshot = FirebaseFirestore
                .getInstance()
                .collection(COLLECTION)
                .document(DOCUMENT)
                .get()
                .await()

            if (!snapshot.exists()) {
                error("Firestore document $COLLECTION/$DOCUMENT does not exist")
            }

            val catalog = snapshot
                .getString(FIELD_CATALOG)
                ?.trim()
                .orEmpty()

            if (catalog.isBlank()) {
                error("Firestore field '$FIELD_CATALOG' is missing or blank")
            }

            try {
                parse(catalog, remote = true)
            } catch (error: Exception) {
                Log.e(
                    TAG,
                    "Failed to parse Firestore template catalog",
                    error
                )
                throw error
            }
        }

    suspend fun bundled(): List<TemplateCategoryData> =
        withContext(Dispatchers.IO) {
            val text = context.assets
                .open("templates/template.json")
                .bufferedReader()
                .use { it.readText() }

            parse(text)
        }

    fun warmPreviewCache(
        categories: List<TemplateCategoryData>,
        limit: Int = 18
    ) {
        categories
            .asSequence()
            .flatMap { it.sections.asSequence() }
            .flatMap { it.templates.asSequence() }
            .mapNotNull { template ->

                val url = template.previewUrl
                    ?.takeIf { it.startsWith("http") }
                    ?: return@mapNotNull null

                val key = (
                    template.previewAssetFileName
                        ?: template.assetFileName
                    )
                    .ifBlank { "${template.id}.asset" }

                url to key
            }
            .distinctBy { it.second }
            .take(limit)
            .forEach { (url, key) ->
                TemplateAssetCache.cache(
                    context = context,
                    remoteUrl = url,
                    cacheKey = key
                )
            }
    }

    internal fun parse(
        text: String,
        remote: Boolean = false
    ): List<TemplateCategoryData> {

        val categories = JSONObject(text)
            .getJSONArray("categories")

        return buildList {

            for (ci in 0 until categories.length()) {

                val category = categories.getJSONObject(ci)
                val sections = category.optJSONArray("sections")
                    ?: continue

                val parsedSections = buildList {

                    for (si in 0 until sections.length()) {

                        val section = sections.getJSONObject(si)
                        val templates = section.optJSONArray("templates")
                            ?: continue

                        val parsedTemplates = buildList {

                            for (ti in 0 until templates.length()) {

                                val item = templates.getJSONObject(ti)

                                val action = item
                                    .optString("action")
                                    .toTemplateAction()
                                    ?: continue

                                val mediaUrl = item
                                    .optString("mediaUrl")
                                    .takeIf { it.startsWith("https://") }

                                val previewUrl = item
                                    .optString("previewUrl")
                                    .takeIf { it.startsWith("https://") }

                                val assetFileName =
                                    item.optString("assetFileName")

                                val previewAssetFileName =
                                    item.optString("previewAssetFileName")
                                        .takeIf { it.isNotBlank() }
                                        ?: assetFileName.takeIf {
                                            it.isNotBlank()
                                        }

                                add(
                                    TemplateListItem(
                                        id = item.getString("id"),
                                        title = item.getString("title"),
                                        subtitle = item.optString("subtitle"),
                                        prompt = item.getString("prompt"),
                                        assetFileName = assetFileName,
                                        previewAssetFileName = previewAssetFileName,
                                        action = action,
                                        mediaUrl = mediaUrl,
                                        previewUrl = previewUrl
                                    )
                                )
                            }
                        }

                        if (parsedTemplates.isNotEmpty()) {
                            add(
                                TemplateSection(
                                    id = section.getString("id"),
                                    title = section.getString("title"),
                                    templates = parsedTemplates
                                )
                            )
                        }
                    }
                }

                add(
                    TemplateCategoryData(
                        id = category.getString("id"),
                        title = category.getString("title"),
                        sections = parsedSections
                    )
                )
            }
        }
    }
}

private fun String.toTemplateAction(): TemplateAction? {
    val normalized = trim()
        .replace('-', '_')
        .replace(' ', '_')
        .uppercase()

    return TemplateAction.entries
        .firstOrNull { it.name == normalized }
}