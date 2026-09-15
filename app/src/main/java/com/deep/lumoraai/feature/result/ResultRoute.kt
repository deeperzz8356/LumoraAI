package com.deep.lumoraai.feature.result

import androidx.compose.runtime.Composable

@Composable
fun ResultRoute(path: String, mediaType: String, mimeType: String, onBack: () -> Unit) {
    ResultScreen(path, mediaType, mimeType, onBack)
}
