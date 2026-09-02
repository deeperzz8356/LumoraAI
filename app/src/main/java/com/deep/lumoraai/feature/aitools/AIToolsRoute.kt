package com.deep.lumoraai.feature.aitools

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AIToolsRoute(
    onNavigate: (String) -> Unit = {},
    viewModel: AIToolsViewModel = viewModel(),
) {
    AIToolsScreen(
        credits = viewModel.credits,
        onNavigate = onNavigate,
    )
}
