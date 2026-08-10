package com.deep.lumoraai.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object MediaShareUtils {
    private const val AUTHORITY = "com.deep.lumoraai.fileprovider"

    fun shareImage(context: Context, filePath: String) {
        shareFile(context, filePath, "image/*", "Share image")
    }

    fun shareVideo(context: Context, filePath: String) {
        shareFile(context, filePath, "video/*", "Share video")
    }

    fun shareMedia(context: Context, filePath: String, mimeType: String) {
        val type = when {
            mimeType.startsWith("video/") -> "video/*"
            mimeType.startsWith("image/") -> "image/*"
            else -> "*/*"
        }
        shareFile(context, filePath, type, "Share media")
    }

    private fun shareFile(context: Context, filePath: String, mimeType: String, chooserTitle: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val uri: Uri = FileProvider.getUriForFile(context, AUTHORITY, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
