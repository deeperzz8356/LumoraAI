package com.deep.lumoraai.data.remote.bg

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device / offline background removal using ML Kit Subject Segmentation.
 *
 * Serves as the fallback path for the WithoutBG API: used when API credits run
 * out (402/403), when the device is offline, or when the API is otherwise
 * unavailable. Runs entirely on-device and consumes no credits.
 */
class SubjectSegmentationFallback {

    private val segmenter by lazy {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .build()
        SubjectSegmentation.getClient(options)
    }

    /**
     * Produces a transparent RGBA cutout of the main subject(s) in [source].
     *
     * @return the cutout bitmap, or null if segmentation failed (caller may then
     *   surface an error to the user).
     */
    suspend fun removeBackground(source: Bitmap): Bitmap? = withContext(Dispatchers.Default) {
        val argbSource = if (source.config == Bitmap.Config.ARGB_8888) {
            source
        } else {
            source.copy(Bitmap.Config.ARGB_8888, false)
        }

        val inputImage = InputImage.fromBitmap(argbSource, 0)
        val result = runCatching {
            // ML Kit tasks are async; block this background thread until done.
            Tasks.await(segmenter.process(inputImage))
        }.getOrNull() ?: return@withContext null

        val mask = result.foregroundConfidenceMask ?: return@withContext null
        val width = argbSource.width
        val height = argbSource.height

        val pixels = IntArray(width * height)
        argbSource.getPixels(pixels, 0, width, 0, 0, width, height)

        mask.rewind()
        for (i in pixels.indices) {
            val confidence = if (mask.hasRemaining()) mask.get() else 0f
            val alpha = (confidence.coerceIn(0f, 1f) * 255f).toInt()
            val original = pixels[i]
            pixels[i] = Color.argb(
                alpha,
                Color.red(original),
                Color.green(original),
                Color.blue(original),
            )
        }

        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
