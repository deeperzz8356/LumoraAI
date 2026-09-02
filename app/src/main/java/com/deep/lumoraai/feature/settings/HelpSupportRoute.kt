package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable

@Composable
fun HelpSupportRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    HelpSupportScreen(onBack = onBack, onNavigate = onNavigate)
}
