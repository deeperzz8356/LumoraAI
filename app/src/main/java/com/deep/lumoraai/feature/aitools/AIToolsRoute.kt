package com.deep.lumoraai.feature.aitools

import androidx.compose.runtime.Composable

@Composable
fun AIToolsRoute(
    onNavigate: (String) -> Unit = {},
) {
    AIToolsScreen(onNavigate = onNavigate)
}
