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
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LumoraNotificationCenter
import com.deep.lumoraai.data.local.room.LumoraDatabase
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class PhotoEnhanceViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(
        LumoraDatabase.getInstance(application).historyDao
    )
    private val generationRepository = GenerationRepository()
    private val appPreferences = AppPreferencesRepository.getInstance(application)
    private val imagesDir = File(application.filesDir, "media/images").also { it.mkdirs() }

    var uiState: PhotoEnhanceUiState by mutableStateOf(PhotoEnhanceUiState())
        private set

    init {
        loadCredits()
    }

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

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            val credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                generationRepository.getCredits().getOrDefault(0)
            }
            uiState = uiState.copy(credits = credits)
        }
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
                    LumoraNotificationCenter.notifyCompletion(
                        context = getApplication<Application>(),
                        title = "Enhancement ready",
                        message = "Your enhanced photo has been saved.",
                        route = Screen.History.route,
                        mediaType = "IMAGE",
                    )
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
        val denoised = applyEdgePreservingDenoise(scaled, sharpness)
        val lit = applyLighting(denoised, lighting)
        val balanced = applyAutoToneAndVibrance(lit, lighting)
        val deblurred = applyDeblur(balanced, sharpness)
        val detailed = applyLocalContrast(deblurred, sharpness)
        return applyEdgeAwareSharpen(detailed, sharpness)
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

    private fun applyDeblur(source: Bitmap, amount: Float): Bitmap {
        if (source.width < 3 || source.height < 3) return source
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)

        val softBlur = gaussianBlur(input, width, height)
        val strongBlur = gaussianBlur(softBlur, width, height)
        val output = IntArray(input.size)
        val strength = 1.15f + amount * 2.85f

        for (i in input.indices) {
            val original = input[i]
            val blur = strongBlur[i]
            val edge = edgeAmount(input, width, height, i)
            val localStrength = strength * (0.58f + edge * 0.62f)
            val a = original ushr 24
            val r = unsharpChannel(original.red(), blur.red(), localStrength)
            val g = unsharpChannel(original.green(), blur.green(), localStrength)
            val b = unsharpChannel(original.blue(), blur.blue(), localStrength)
            output[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        return applySharpen(Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888), amount)
    }

    private fun gaussianBlur(input: IntArray, width: Int, height: Int): IntArray {
        val output = IntArray(input.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                var weightSum = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val sampleX = (x + dx).coerceIn(0, width - 1)
                        val sampleY = (y + dy).coerceIn(0, height - 1)
                        val weight = if (dx == 0 && dy == 0) 4 else if (dx == 0 || dy == 0) 2 else 1
                        val color = input[sampleY * width + sampleX]
                        a += (color ushr 24) * weight
                        r += color.red() * weight
                        g += color.green() * weight
                        b += color.blue() * weight
                        weightSum += weight
                    }
                }
                output[y * width + x] =
                    ((a / weightSum) shl 24) or
                        ((r / weightSum) shl 16) or
                        ((g / weightSum) shl 8) or
                        (b / weightSum)
            }
        }
        return output
    }

    private fun applyEdgePreservingDenoise(source: Bitmap, amount: Float): Bitmap {
        if (source.width < 3 || source.height < 3) return source
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        val output = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)
        input.copyInto(output)

        val blend = (0.18f - amount * 0.08f).coerceIn(0.08f, 0.18f)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                val center = input[index]
                val edge = edgeAmount(input, width, height, index)
                if (edge > 0.34f) continue

                var r = 0
                var g = 0
                var b = 0
                var count = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val sample = input[(y + dy) * width + x + dx]
                        r += sample.red()
                        g += sample.green()
                        b += sample.blue()
                        count++
                    }
                }

                val localBlend = blend * (1f - edge)
                output[index] =
                    ((center ushr 24) shl 24) or
                        (lerpChannel(center.red(), r / count, localBlend) shl 16) or
                        (lerpChannel(center.green(), g / count, localBlend) shl 8) or
                        lerpChannel(center.blue(), b / count, localBlend)
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun applyAutoToneAndVibrance(source: Bitmap, lighting: EnhanceOption): Bitmap {
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        val output = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)

        var minLum = 255
        var maxLum = 0
        input.forEach { color ->
            val lum = luminance(color)
            minLum = minOf(minLum, lum)
            maxLum = maxOf(maxLum, lum)
        }

        val range = max(1, maxLum - minLum)
        val toneStrength = when (lighting) {
            EnhanceOption.Low -> 0.36f
            EnhanceOption.Med -> 0.46f
            EnhanceOption.High -> 0.56f
            EnhanceOption.Ultra -> 0.62f
        }
        val vibrance = when (lighting) {
            EnhanceOption.Low -> 0.06f
            EnhanceOption.Med -> 0.10f
            EnhanceOption.High -> 0.14f
            EnhanceOption.Ultra -> 0.18f
        }

        for (i in input.indices) {
            val color = input[i]
            val a = color ushr 24
            val r = toneChannel(color.red(), minLum, range, toneStrength)
            val g = toneChannel(color.green(), minLum, range, toneStrength)
            val b = toneChannel(color.blue(), minLum, range, toneStrength)
            val boosted = boostVibrance(r, g, b, vibrance)
            output[i] = (a shl 24) or (boosted[0] shl 16) or (boosted[1] shl 8) or boosted[2]
        }

        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun applyLocalContrast(source: Bitmap, amount: Float): Bitmap {
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        val output = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)
        val blur = gaussianBlur(input, width, height)
        val strength = 0.24f + amount * 0.58f

        for (i in input.indices) {
            val original = input[i]
            val base = blur[i]
            val edge = edgeAmount(input, width, height, i)
            val localStrength = strength * (0.72f + edge * 0.48f)
            val a = original ushr 24
            val r = (original.red() + (original.red() - base.red()) * localStrength).roundToInt().coerceIn(0, 255)
            val g = (original.green() + (original.green() - base.green()) * localStrength).roundToInt().coerceIn(0, 255)
            val b = (original.blue() + (original.blue() - base.blue()) * localStrength).roundToInt().coerceIn(0, 255)
            output[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun applyEdgeAwareSharpen(source: Bitmap, amount: Float): Bitmap {
        if (amount <= 0.01f || source.width < 3 || source.height < 3) return source
        val width = source.width
        val height = source.height
        val input = IntArray(width * height)
        val output = IntArray(width * height)
        source.getPixels(input, 0, width, 0, 0, width, height)
        input.copyInto(output)

        val strength = 0.22f + amount * 0.74f
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                val center = input[index]
                val edge = edgeAmount(input, width, height, index)
                if (edge < 0.05f) continue

                val left = input[index - 1]
                val right = input[index + 1]
                val top = input[index - width]
                val bottom = input[index + width]
                val localStrength = strength * edge.coerceIn(0.18f, 1f)

                val a = center ushr 24
                val r = sharpenChannel(center.red(), left.red(), right.red(), top.red(), bottom.red(), localStrength)
                val g = sharpenChannel(center.green(), left.green(), right.green(), top.green(), bottom.green(), localStrength)
                val b = sharpenChannel(center.blue(), left.blue(), right.blue(), top.blue(), bottom.blue(), localStrength)
                output[index] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun unsharpChannel(original: Int, blur: Int, strength: Float): Int =
        (original + (original - blur) * strength).roundToInt().coerceIn(0, 255)

    private fun sharpenChannel(center: Int, left: Int, right: Int, top: Int, bottom: Int, strength: Float): Int {
        val blurred = (left + right + top + bottom) / 4f
        return (center + (center - blurred) * strength).roundToInt().coerceIn(0, 255)
    }

    private fun toneChannel(value: Int, minLum: Int, range: Int, strength: Float): Int {
        val normalized = ((value - minLum).toFloat() / range).coerceIn(0f, 1f)
        val stretched = (normalized * 255f).roundToInt()
        return lerpChannel(value, stretched, strength)
    }

    private fun boostVibrance(r: Int, g: Int, b: Int, amount: Float): IntArray {
        val maxChannel = max(r, max(g, b))
        val average = (r + g + b) / 3f
        val saturationGap = ((maxChannel - average) / 255f).coerceIn(0f, 1f)
        val adaptive = amount * (1f - saturationGap)
        return intArrayOf(
            (average + (r - average) * (1f + adaptive)).roundToInt().coerceIn(0, 255),
            (average + (g - average) * (1f + adaptive)).roundToInt().coerceIn(0, 255),
            (average + (b - average) * (1f + adaptive)).roundToInt().coerceIn(0, 255),
        )
    }

    private fun edgeAmount(input: IntArray, width: Int, height: Int, index: Int): Float {
        val x = index % width
        val y = index / width
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) return 0f
        val horizontal = abs(luminance(input[index - 1]) - luminance(input[index + 1]))
        val vertical = abs(luminance(input[index - width]) - luminance(input[index + width]))
        return ((horizontal + vertical) / 255f).coerceIn(0f, 1f)
    }

    private fun luminance(color: Int): Int =
        (color.red() * 0.299f + color.green() * 0.587f + color.blue() * 0.114f).roundToInt()

    private fun lerpChannel(start: Int, end: Int, amount: Float): Int =
        (start + (end - start) * amount).roundToInt().coerceIn(0, 255)

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
