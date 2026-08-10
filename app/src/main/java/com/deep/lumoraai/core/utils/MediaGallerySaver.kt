package com.deep.lumoraai.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.Manifest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaGallerySaver {

    fun needsWritePermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun hasWritePermission(context: Context): Boolean {
        if (!needsWritePermission()) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Copy an app-private media file into the system gallery
     * (Pictures/LumoraAI or Movies/LumoraAI).
     */
    suspend fun saveToGallery(
        context: Context,
        filePath: String,
        mimeType: String,
        mediaType: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val source = File(filePath)
            if (!source.exists()) error("Media file not found")

            val isVideo = mediaType.equals("VIDEO", ignoreCase = true) ||
                mimeType.startsWith("video/", ignoreCase = true)
            val displayName = source.name
            val resolver = context.contentResolver

            val collection = if (isVideo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relativePath = if (isVideo) {
                        Environment.DIRECTORY_MOVIES + "/LumoraAI"
                    } else {
                        Environment.DIRECTORY_PICTURES + "/LumoraAI"
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(collection, values)
                ?: error("Could not create gallery entry")

            resolver.openOutputStream(uri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: error("Could not write to gallery")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            if (isVideo) "Saved to Movies/LumoraAI" else "Saved to Pictures/LumoraAI"
        }
    }
}
