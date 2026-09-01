package com.deep.lumoraai.feature.compress

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CompressViewModel(application: Application) : AndroidViewModel(application) {

    private val resolver = application.contentResolver
    private val outputDir = File(application.filesDir, "media/compressed").also { it.mkdirs() }
    private val historyRepository = HistoryRepository(LumoraDatabase.getInstance(application).historyDao)

    var uiState: CompressUiState by mutableStateOf(CompressUiState())
        private set

    fun loadFile(uri: Uri) {
        val mimeType = resolver.getType(uri).orEmpty()
        if (!mimeType.startsWith("image/") && !mimeType.startsWith("video/")) {
            uiState = CompressUiState(error = "Only image and video files can be compressed.")
            return
        }

        uiState = CompressUiState(
            selectedUri = uri,
            fileName = displayName(uri),
            mimeType = mimeType,
        )
    }

    fun compress() {
        val uri = uiState.selectedUri ?: run {
            uiState = uiState.copy(error = "Select an image or video first.")
            return
        }
        val mimeType = uiState.mimeType

        viewModelScope.launch {
            uiState = uiState.copy(isCompressing = true, error = null, result = null)
            if (mimeType.startsWith("image/")) {
                compressImage(uri)
            } else {
                compressVideo(uri)
            }
        }
    }

    fun reset() {
        uiState = CompressUiState()
    }

    fun saveResultToDownloads() {
        val result = uiState.result ?: return
        viewModelScope.launch {
            val saveResult = withContext(Dispatchers.IO) {
                runCatching {
                    saveFileToDownloads(File(result.outputPath), result.mimeType)
                }
            }
            uiState = saveResult.fold(
                onSuccess = { uiState.copy(downloadMessage = "Saved to Downloads", error = null) },
                onFailure = { uiState.copy(error = it.message ?: "Could not save this file.") },
            )
        }
    }

    private suspend fun compressImage(uri: Uri) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val originalBytes = fileSize(uri)
                val bitmap = decodeBitmap(uri)
                val output = File(outputDir, "compressed_${UUID.randomUUID()}.jpg")
                output.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 62, out)
                }
                val compressedBytes = output.length()
                saveToHistory(output.absolutePath, "IMAGE")
                CompressionResult(
                    outputPath = output.absolutePath,
                    mimeType = "image/jpeg",
                    originalBytes = originalBytes,
                    compressedBytes = compressedBytes,
                )
            }
        }

        uiState = result.fold(
            onSuccess = { uiState.copy(isCompressing = false, result = it) },
            onFailure = { uiState.copy(isCompressing = false, error = it.message ?: "Could not compress this image.") },
        )
    }

    @OptIn(UnstableApi::class)
    private fun compressVideo(uri: Uri) {
        val originalBytes = fileSize(uri)
        val output = File(outputDir, "compressed_${UUID.randomUUID()}.mp4")
        val transformer = Transformer.Builder(getApplication())
            .setTransformationRequest(
                TransformationRequest.Builder()
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .build()
            )
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    viewModelScope.launch {
                        saveToHistory(output.absolutePath, "VIDEO")
                        uiState = uiState.copy(
                            isCompressing = false,
                            result = CompressionResult(
                                outputPath = output.absolutePath,
                                mimeType = "video/mp4",
                                originalBytes = originalBytes,
                                compressedBytes = output.length(),
                            )
                        )
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    if (output.exists()) output.delete()
                    uiState = uiState.copy(
                        isCompressing = false,
                        error = exportException.message ?: "Could not compress this video."
                    )
                }
            })
            .build()

        transformer.start(MediaItem.fromUri(uri), output.absolutePath)
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            resolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input) ?: error("Unsupported image file.")
            }
        }
    }

    private fun fileSize(uri: Uri): Long {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                val size = cursor.getLong(sizeIndex)
                if (size > 0) return size
            }
        }
        return resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    }

    private fun displayName(uri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            if (cursor?.moveToFirst() == true && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                uri.lastPathSegment ?: "Selected file"
            }
        } finally {
            cursor?.close()
        }
    }

    private suspend fun saveToHistory(path: String, type: String) {
        historyRepository.addHistory(
            historyModel = HistoryModel(
                id = UUID.randomUUID().toString(),
                title = if (type == "VIDEO") "Compressed Video" else "Compressed Image",
                createdAt = currentTimestamp(),
                type = type,
                mediaUrl = path,
            ),
            type = type,
            mediaUrl = path,
        )
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    private fun saveFileToDownloads(source: File, mimeType: String) {
        if (!source.exists()) error("Compressed file is missing.")
        val extension = if (mimeType.startsWith("video/")) "mp4" else "jpg"
        val fileName = "lumora_compressed_${System.currentTimeMillis()}.$extension"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LumoraAI")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values) ?: error("Could not create download file.")
            resolver.openOutputStream(uri)?.use { out ->
                source.inputStream().use { input -> input.copyTo(out) }
            } ?: error("Could not write download file.")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloads, "LumoraAI").also { it.mkdirs() }
            source.copyTo(File(appDir, fileName), overwrite = true)
        }
    }
}
