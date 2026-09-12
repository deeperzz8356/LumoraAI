package com.deep.lumoraai.feature.templates.model

data class TemplateCatalog(
    val categories: List<TemplateCategoryData>
)

data class TemplateCategoryData(
    val id: String,
    val title: String,
    val sections: List<TemplateSection>
)

data class TemplateSection(
    val id: String,
    val title: String,
    val templates: List<TemplateListItem>
)

data class TemplateListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val assetFileName: String,
    val previewAssetFileName: String?,
    val action: TemplateAction
)

enum class TemplateAction {
    TEXT_TO_IMAGE,
    TEXT_TO_VIDEO,
    PROMO_VIDEO,
    LOGO_CREATION,
    CREATE_AVATAR
}

enum class TemplateCategory(val id: String, val label: String) {
    IMAGE("image", "Images"),
    VIDEO("video", "Video"),
    PROMO_VIDEO("promo_video", "Promo Video"),
    AVATAR("avatar", "Avatar");

    companion object {
        fun fromId(id: String?): TemplateCategory =
            entries.firstOrNull { it.id == id } ?: IMAGE
    }
}
