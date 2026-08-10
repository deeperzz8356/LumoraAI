package com.deep.lumoraai.data.model

import android.net.Uri

data class SavedMedia(
    val id: String,
    val localUri: Uri,
    val filePath: String,
    val mimeType: String,
    val mediaType: String, // IMAGE | VIDEO
)
