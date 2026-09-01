package com.deep.lumoraai.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.deep.lumoraai.core.utils.decodeMediaPayload
import com.deep.lumoraai.core.utils.extensionForMimeType
import com.deep.lumoraai.core.utils.isHttpImageUrl
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.data.model.SavedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class MediaStorageRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val imagesDir = File(appContext.filesDir, "media/images").also { it.mkdirs() }
    private val videosDir = File(appContext.filesDir, "media/videos").also { it.mkdirs() }

    suspend fun saveImageFromPayload(payload: String): SavedMedia = withContext(Dispatchers.IO) {
        savePayload(payload = payload, mediaType = MEDIA_IMAGE, outputDir = imagesDir)
    }

    suspend fun saveVideoFromPayload(payload: String): SavedMedia = withContext(Dispatchers.IO) {
        savePayload(payload = payload, mediaType = MEDIA_VIDEO, outputDir = videosDir)
    }

    suspend fun saveImageBitmap(
        bitmap: Bitmap,
        mimeType: String = "image/png",
        quality: Int = 100,
    ): SavedMedia = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val extension = extensionForMimeType(mimeType, MEDIA_IMAGE)
        val file = File(imagesDir, "$id.$extension")
        file.outputStream().use { output ->
            val format = when (mimeType.lowercase()) {
                "image/jpeg", "image/jpg" -> Bitmap.CompressFormat.JPEG
                "image/webp" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                else -> Bitmap.CompressFormat.PNG
            }
            bitmap.compress(format, quality, output)
        }
        runCatching {
            MediaGallerySaver.saveToGallery(
                context = appContext,
                filePath = file.absolutePath,
                mimeType = mimeType,
                mediaType = MEDIA_IMAGE,
            )
        }
        SavedMedia(
            id = id,
            localUri = Uri.fromFile(file),
            filePath = file.absolutePath,
            mimeType = mimeType,
            mediaType = MEDIA_IMAGE,
        )
    }

    fun deleteMedia(filePath: String): Boolean {
        return runCatching { File(filePath).takeIf { it.exists() }?.delete() == true }.getOrDefault(false)
    }

    fun contentUriForFile(filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(appContext, AUTHORITY, file)
    }

    private suspend fun savePayload(payload: String, mediaType: String, outputDir: File): SavedMedia {
        val id = UUID.randomUUID().toString()
        val (bytes, mimeType) = if (isHttpImageUrl(payload)) {
            downloadRemote(payload, defaultMime = if (mediaType == MEDIA_VIDEO) "video/mp4" else "image/png")
        } else {
            decodeMediaPayload(payload)
                ?: error("Unsupported media payload for $mediaType")
        }
        val extension = extensionForMimeType(mimeType, mediaType)
        val file = File(outputDir, "$id.$extension")
        file.writeBytes(bytes)
        runCatching {
            MediaGallerySaver.saveToGallery(
                context = appContext,
                filePath = file.absolutePath,
                mimeType = mimeType,
                mediaType = mediaType,
            )
        }
        val localUri = Uri.fromFile(file)
        return SavedMedia(
            id = id,
            localUri = localUri,
            filePath = file.absolutePath,
            mimeType = mimeType,
            mediaType = mediaType,
        )
    }

    private fun downloadRemote(url: String, defaultMime: String): Pair<ByteArray, String> {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 30_000
            readTimeout = 120_000
        }
        return try {
            val code = connection.responseCode
            if (code !in 200..299) {
                error("Failed to download media ($code)")
            }
            val mime = connection.contentType?.substringBefore(";")?.trim().orEmpty()
                .ifBlank { defaultMime }
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty()) error("Downloaded media was empty")
            bytes to mime
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val MEDIA_IMAGE = "IMAGE"
        const val MEDIA_VIDEO = "VIDEO"
        private const val AUTHORITY = "com.deep.lumoraai.fileprovider"

        @Volatile
        private var instance: MediaStorageRepository? = null

        fun getInstance(context: Context): MediaStorageRepository =
            instance ?: synchronized(this) {
                instance ?: MediaStorageRepository(context.applicationContext).also { instance = it }
            }
    }
}
