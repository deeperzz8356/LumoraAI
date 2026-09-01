package com.deep.lumoraai.feature.photoenhance

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.math.roundToInt

class PhotoEnhanceViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )
    private val imagesDir = File(application.filesDir, "media/images").also { it.mkdirs() }

    var uiState: PhotoEnhanceUiState by mutableStateOf(PhotoEnhanceUiState())
        private set

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isEnhancing = true, error = null, savedPath = null)
            val decoded = withContext(Dispatchers.IO) {
                runCatching { decodeBitmap(uri) }.getOrNull()
            }
            uiState = if (decoded == null) {
                uiState.copy(isEnhancing = false, error = "Could not open that image.")
            } else {
                uiState.copy(
                    originalBitmap = decoded,
                    enhancedBitmap = null,
                    isEnhancing = false,
                    error = null,
                    savedPath = null,
                )
            }
        }
    }

    fun setResolution(option: EnhanceOption) {
        uiState = uiState.copy(resolution = option, savedPath = null)
    }

    fun setSharpness(value: Float) {
        uiState = uiState.copy(sharpness = value.coerceIn(0f, 1f), savedPath = null)
    }

    fun setLighting(option: EnhanceOption) {
        uiState = uiState.copy(lighting = option, savedPath = null)
    }

    fun enhance() {
        val original = uiState.originalBitmap
        if (original == null) {
            uiState = uiState.copy(error = "Upload an image first.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isEnhancing = true, error = null, savedPath = null)
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    val enhanced = enhanceBitmap(
                        source = original,
                        resolution = uiState.resolution,
                        sharpness = uiState.sharpness,
                        lighting = uiState.lighting,
                    )
                    val savedPath = saveEnhancedBitmap(enhanced)
                    historyRepository.addHistory(
                        historyModel = HistoryModel(
                            id = UUID.randomUUID().toString(),
                            title = "Photo Enhance",
                            createdAt = currentTimestamp(),
                            type = "IMAGE",
                            mediaUrl = savedPath,
                        ),
                        type = "IMAGE",
                        mediaUrl = savedPath,
                    )
                    enhanced to savedPath
                }
            }

            uiState = result.fold(
                onSuccess = { (bitmap, path) ->
                    uiState.copy(enhancedBitmap = bitmap, savedPath = path, isEnhancing = false)
                },
                onFailure = { error ->
                    uiState.copy(
                        isEnhancing = false,
                        error = error.message ?: "Could not enhance this image."
                    )
                }
            )
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            resolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input) ?: error("Unsupported image file.")
            }
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun enhanceBitmap(
        source: Bitmap,
        resolution: EnhanceOption,
        sharpness: Float,
        lighting: EnhanceOption,
    ): Bitmap {
        val scaled = upscale(source, resolution)
        val lit = applyLighting(scaled, lighting)
        return applySharpen(lit, sharpness)
    }

    private fun upscale(source: Bitmap, resolution: EnhanceOption): Bitmap {
        val factor = when (resolution) {
            EnhanceOption.Low -> 1f
            EnhanceOption.Med -> 1.25f
            EnhanceOption.High -> 1.5f
            EnhanceOption.Ultra -> 2f
        }
        val maxSide = 4096
        val targetWidth = (source.width * factor).roundToInt().coerceAtMost(maxSide)
        val targetHeight = (source.height * factor).roundToInt().coerceAtMost(maxSide)
        if (targetWidth == source.width && targetHeight == source.height) return source
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    private fun applyLighting(source: Bitmap, lighting: EnhanceOption): Bitmap {
        val contrast = when (lighting) {
            EnhanceOption.Low -> 1.04f
            EnhanceOption.Med -> 1.10f
            EnhanceOption.High -> 1.18f
            EnhanceOption.Ultra -> 1.26f
        }
        val brightness = when (lighting) {
            EnhanceOption.Low -> 3f
            EnhanceOption.Med -> 8f
            EnhanceOption.High -> 14f
            EnhanceOption.Ultra -> 20f
        }
        val translate = (-0.5f * contrast + 0.5f) * 255f + brightness
        val matrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        })
        return result
    }

    private fun applySharpen(source: Bitmap, amount: Float): Bitmap {
        if (amount <= 0.01f || source.width < 3 || source.height < 3) return source
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        val output = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)
        input.copyInto(output)

        val strength = 0.35f + amount * 1.15f
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                val center = input[index]
                val left = input[index - 1]
                val right = input[index + 1]
                val top = input[index - width]
                val bottom = input[index + width]

                val a = center ushr 24
                val r = sharpenChannel(center.red(), left.red(), right.red(), top.red(), bottom.red(), strength)
                val g = sharpenChannel(center.green(), left.green(), right.green(), top.green(), bottom.green(), strength)
                val b = sharpenChannel(center.blue(), left.blue(), right.blue(), top.blue(), bottom.blue(), strength)
                output[index] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun sharpenChannel(center: Int, left: Int, right: Int, top: Int, bottom: Int, strength: Float): Int {
        val blurred = (left + right + top + bottom) / 4f
        return (center + (center - blurred) * strength).roundToInt().coerceIn(0, 255)
    }

    private fun Int.red(): Int = this shr 16 and 0xFF
    private fun Int.green(): Int = this shr 8 and 0xFF
    private fun Int.blue(): Int = this and 0xFF

    private fun saveEnhancedBitmap(bitmap: Bitmap): String {
        val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 94, out)
        }
        return file.absolutePath
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
}
