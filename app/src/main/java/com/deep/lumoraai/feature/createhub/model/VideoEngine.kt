package com.deep.lumoraai.feature.createhub.model

enum class VideoEngine(val displayName: String, val modelId: String) {
    VEO_ULTRA("Veo-1 Ultra", "veo-3.1-generate-001"),
    FAST_DRAFT("FastDraft", "veo-3.1-fast-generate-001");

    companion object {
        fun fromDisplayName(name: String): VideoEngine =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: FAST_DRAFT
    }
}
