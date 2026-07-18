package com.deep.lumoraai.data.model

import com.deep.lumoraai.domain.model.PromptModel
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class PromptDto(
    @DocumentId val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("prompt") val prompt: String = "",
    @PropertyName("category") val category: String = "",
    @PropertyName("mediaType") val mediaType: String = "",
    @PropertyName("mediaUrl") val mediaUrl: String? = null
) {
    fun toDomainModel(): PromptModel {
        return PromptModel(
            id = id,
            title = title,
            prompt = prompt,
            category = category,
            mediaType = mediaType,
            mediaUrl = mediaUrl
        )
    }
}
