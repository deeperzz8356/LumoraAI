package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable

@Composable
fun PrivacySecurityRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    PrivacySecurityScreen(onBack = onBack, onNavigate = onNavigate)
}
