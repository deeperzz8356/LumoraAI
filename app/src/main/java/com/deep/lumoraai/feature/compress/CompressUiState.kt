package com.deep.lumoraai.feature.compress

import android.net.Uri

data class CompressUiState(
    val selectedUri: Uri? = null,
    val fileName: String = "",
    val mimeType: String = "",
    val isCompressing: Boolean = false,
    val result: CompressionResult? = null,
    val downloadMessage: String? = null,
    val error: String? = null,
)

data class CompressionResult(
    val outputPath: String,
    val mimeType: String,
    val originalBytes: Long,
    val compressedBytes: Long,
)
