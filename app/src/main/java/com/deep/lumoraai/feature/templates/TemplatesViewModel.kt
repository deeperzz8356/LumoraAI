package com.deep.lumoraai.feature.templates

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.feature.templates.model.TemplateAction
import com.deep.lumoraai.feature.templates.model.TemplateCategory
import com.deep.lumoraai.feature.templates.model.TemplateCategoryData
import com.deep.lumoraai.feature.templates.model.TemplateListItem
import com.deep.lumoraai.feature.templates.model.TemplateSection
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONObject

class TemplatesViewModel(application: Application) : AndroidViewModel(application) {
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)

    var uiState: TemplatesUiState by mutableStateOf(loadTemplates())
        private set

    var selectedCategoryId: String by mutableStateOf(TemplateCategory.IMAGE.id)
        private set

    val scrollMemory = TemplateScrollMemory()

    init {
        loadCredits()
    }

    fun selectCategory(categoryId: String) {
        val state = uiState as? TemplatesUiState.Success ?: return
        if (state.category(categoryId) != null) selectedCategoryId = categoryId
    }

    private fun loadTemplates(): TemplatesUiState.Success =
        try {
            val jsonText = getApplication<Application>()
                .assets
                .open("templates/templates.json")
                .bufferedReader()
                .use { it.readText() }
            val categoryArray = JSONObject(jsonText).getJSONArray("categories")
            val categories = buildList {
                for (categoryIndex in 0 until categoryArray.length()) {
                    val category = categoryArray.getJSONObject(categoryIndex)
                    val sectionArray = category.getJSONArray("sections")
                    val sections = buildList {
                        for (sectionIndex in 0 until sectionArray.length()) {
                            val section = sectionArray.getJSONObject(sectionIndex)
                            val templateArray = section.getJSONArray("templates")
                            val templates = buildList {
                                for (templateIndex in 0 until templateArray.length()) {
                                    val template = templateArray.getJSONObject(templateIndex)
                                    val action = runCatching {
                                        TemplateAction.valueOf(template.getString("action"))
                                    }.getOrNull() ?: continue
                                    add(
                                        TemplateListItem(
                                            id = template.getString("id"),
                                            title = template.getString("title"),
                                            subtitle = template.optString("subtitle"),
                                            prompt = template.getString("prompt"),
                                            assetFileName = template.optString("assetFileName"),
                                            previewAssetFileName = template
                                                .optString("previewAssetFileName")
                                                .takeIf { it.isNotBlank() },
                                            action = action
                                        )
                                    )
                                }
                            }
                            add(
                                TemplateSection(
                                    id = section.getString("id"),
                                    title = section.getString("title"),
                                    templates = templates
                                )
                            )
                        }
                    }
                    add(
                        TemplateCategoryData(
                            id = category.getString("id"),
                            title = category.getString("title"),
                            sections = sections
                        )
                    )
                }
            }
            TemplatesUiState.Success(categories = categories)
        } catch (_: Exception) {
            TemplatesUiState.Success(categories = emptyList())
        }

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                LocalCreditBalance.maxWith(
                    getApplication(),
                    generationRepository.getCredits().getOrNull()
                )
            }
            val current = uiState
            if (current is TemplatesUiState.Success) uiState = current.copy(credits = credits)
        }
    }
}

class TemplateScrollMemory {
    private val categoryPositions = mutableMapOf<String, Int>()
    private val sectionPositions = mutableMapOf<String, Int>()

    fun categoryPosition(categoryId: String): Int = categoryPositions[categoryId] ?: 0

    fun saveCategoryPosition(categoryId: String, position: Int) {
        categoryPositions[categoryId] = position.coerceAtLeast(0)
    }

    fun sectionPosition(categoryId: String, sectionId: String): Int =
        sectionPositions["$categoryId/$sectionId"] ?: 0

    fun saveSectionPosition(categoryId: String, sectionId: String, position: Int) {
        sectionPositions["$categoryId/$sectionId"] = position.coerceAtLeast(0)
    }
}
