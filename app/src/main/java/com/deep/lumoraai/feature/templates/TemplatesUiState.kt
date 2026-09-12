package com.deep.lumoraai.feature.templates

import com.deep.lumoraai.feature.templates.model.TemplateCategoryData
import com.deep.lumoraai.feature.templates.model.TemplateSection

sealed interface TemplatesUiState {

    data object Loading : TemplatesUiState

    data class Success(
        val categories: List<TemplateCategoryData>,
        val credits: Int = 0
    ) : TemplatesUiState {
        fun category(id: String): TemplateCategoryData? = categories.firstOrNull { it.id == id }

        fun section(categoryId: String, sectionId: String): TemplateSection? =
            category(categoryId)?.sections?.firstOrNull { it.id == sectionId }
    }

    data class Error(
        val message: String
    ) : TemplatesUiState

    data object Empty : TemplatesUiState
}
