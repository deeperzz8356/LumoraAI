package com.deep.lumoraai.feature.templates.model

data class TemplateCatalog(
    val categories: List<TemplateCategoryData>
)

data class TemplateCategoryData(
    val id: String,
    val title: String,
    val templates: List<TemplateData>
)

data class TemplateData(
    val id: String,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val assetFileName: String,
    val action: TemplateAction
)

data class TemplateListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val assetFileName: String,
    val action: TemplateAction
)

enum class TemplateAction {
    TEXT_TO_IMAGE,
    TEXT_TO_VIDEO,
    PROMO_VIDEO,
    LOGO_CREATION,
    CREATE_AVATAR
}

enum class TemplateCategory {
    IMAGE,
    VIDEO,
    PROMO_VIDEO,
    LOGO_CREATION,
    AVATAR
}