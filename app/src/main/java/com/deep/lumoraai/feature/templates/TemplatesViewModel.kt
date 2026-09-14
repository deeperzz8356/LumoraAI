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

    var uiState: TemplatesUiState by mutableStateOf(TemplatesUiState.Loading)
        private set

    var selectedCategoryId: String by mutableStateOf(TemplateCategory.IMAGE.id)
        private set

    val scrollMemory = TemplateScrollMemory()



    fun selectCategory(categoryId: String) {
        val state = uiState as? TemplatesUiState.Success ?: return
        if (state.category(categoryId) != null) selectedCategoryId = categoryId
    }

    private val templatesRepository = com.deep.lumoraai.data.repository.TemplateRepository(application)

    init { refreshTemplates() }

    fun refreshTemplates() {
        viewModelScope.launch {
            if (uiState !is TemplatesUiState.Success) {
                try {
                    uiState = TemplatesUiState.Success(templatesRepository.bundled())
                } catch (error: kotlinx.coroutines.CancellationException) { throw error
                } catch (_: Exception) { uiState = TemplatesUiState.Error("Could not read starter templates.") }
            }
            try {
                val categories = templatesRepository.remote()
                val credits = (uiState as? TemplatesUiState.Success)?.credits ?: 0
                uiState = TemplatesUiState.Success(categories, credits)
            } catch (error: kotlinx.coroutines.CancellationException) { throw error
            } catch (_: Exception) {
                val current = uiState as? TemplatesUiState.Success
                if (current != null) uiState = current.copy(offlineMessage = "Showing saved templates. Connect to load the full library.")
                else uiState = TemplatesUiState.Error("Connect to load templates, then try again.")
            }
            loadCredits()
        }
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
