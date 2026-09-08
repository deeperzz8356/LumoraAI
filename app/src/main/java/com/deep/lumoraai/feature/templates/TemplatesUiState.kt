package com.deep.lumoraai.feature.templates

import com.deep.lumoraai.feature.templates.model.TemplateListItem

sealed interface TemplatesUiState {

    data object Loading : TemplatesUiState

    data class Success(
        val imageTemplates: List<TemplateListItem>,
        val videoTemplates: List<TemplateListItem>,
        val promoVideoTemplates: List<TemplateListItem> = emptyList(),
        val logoCreationTemplates: List<TemplateListItem> = emptyList(),
        val avatarTemplates: List<TemplateListItem> = emptyList(),
        val credits: Int = 0
    ) : TemplatesUiState

    data class Error(
        val message: String
    ) : TemplatesUiState

    data object Empty : TemplatesUiState
}