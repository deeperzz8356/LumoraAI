package com.deep.lumoraai.feature.result

import androidx.compose.runtime.Composable

@Composable
fun ResultRoute(path: String, mediaType: String, mimeType: String, firstNewJobIndex: Int, onBack: () -> Unit) {
    ResultScreen(path, mediaType, mimeType, firstNewJobIndex, onBack)
}
