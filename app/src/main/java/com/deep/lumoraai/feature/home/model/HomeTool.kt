package com.deep.lumoraai.feature.home.model

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeTool(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector, // We will map it in UI
    val route: String
)
