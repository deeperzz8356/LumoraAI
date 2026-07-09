package com.deep.lumoraai.feature.createhub.model

import androidx.compose.ui.graphics.vector.ImageVector

data class CreateTool(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String? = null, // e.g. "PRO", "NEW"
    val icon: ImageVector,
    val route: String
)
