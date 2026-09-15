package com.deep.lumoraai.feature.templates

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.Executors

object TemplateAssetCache {
    private val executor = Executors.newFixedThreadPool(3)
    private val main = Handler(Looper.getMainLooper())
    private val active = mutableSetOf<String>()

    fun cachedFile(context: Context, cacheKey: String): File? =
        fileFor(context, cacheKey).takeIf { it.exists() && it.length() > 0L }

    fun cache(
        context: Context,
        remoteUrl: String?,
        cacheKey: String,
        onReady: (File) -> Unit = {},
    ) {
        if (remoteUrl.isNullOrBlank() || !remoteUrl.startsWith("http")) return
        val target = fileFor(context, cacheKey)
        if (target.exists() && target.length() > 0L) {
            onReady(target)
            return
        }
        val lockKey = target.absolutePath
        synchronized(active) {
            if (!active.add(lockKey)) return
        }
        remoteUrl.toFirebaseStoragePath()?.let { storagePath ->
            downloadFromFirebaseStorage(storagePath, target, lockKey, onReady)
            return
        }
        downloadFromHttp(remoteUrl, target, lockKey, onReady)
    }

    private fun downloadFromFirebaseStorage(
        storagePath: String,
        target: File,
        lockKey: String,
        onReady: (File) -> Unit,
    ) {
        target.parentFile?.mkdirs()
        val temp = File(target.parentFile, "${target.name}.tmp")
        if (temp.exists()) temp.delete()
        FirebaseStorage.getInstance().reference.child(storagePath).getFile(temp)
            .addOnSuccessListener {
                if (temp.length() > 0L) {
                    if (target.exists()) target.delete()
                    temp.renameTo(target)
                } else {
                    temp.delete()
                }
                if (target.exists() && target.length() > 0L) onReady(target)
            }
            .addOnFailureListener {
                temp.delete()
            }
            .addOnCompleteListener {
                synchronized(active) { active.remove(lockKey) }
            }
    }

    private fun downloadFromHttp(
        remoteUrl: String,
        target: File,
        lockKey: String,
        onReady: (File) -> Unit,
    ) {
        executor.execute {
            runCatching {
                target.parentFile?.mkdirs()
                val temp = File(target.parentFile, "${target.name}.tmp")
                val connection = URL(remoteUrl).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 8000
                    connection.readTimeout = 12000
                    connection.instanceFollowRedirects = true
                    if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
                    connection.inputStream.use { input ->
                        temp.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (temp.length() > 0L) {
                        if (target.exists()) target.delete()
                        temp.renameTo(target)
                    } else {
                        temp.delete()
                    }
                } finally {
                    connection.disconnect()
                }
                target.takeIf { it.exists() && it.length() > 0L }
            }.onSuccess { file ->
                if (file != null) main.post { onReady(file) }
            }
            synchronized(active) { active.remove(lockKey) }
        }
    }

    private fun fileFor(context: Context, cacheKey: String): File {
        val safeName = cacheKey.ifBlank { "template_asset" }
            .replace('\\', '/')
            .split('/')
            .joinToString("_") { part ->
                part.replace(Regex("[^A-Za-z0-9._-]"), "_")
            }
        return File(File(context.filesDir, "template_assets"), safeName)
    }

    private fun encodePathPart(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun String.toFirebaseStoragePath(): String? {
        val marker = "/o/"
        val pathStart = indexOf(marker).takeIf { it >= 0 }?.plus(marker.length) ?: return null
        val pathEnd = indexOf('?', startIndex = pathStart).takeIf { it >= 0 } ?: length
        return URLDecoder.decode(substring(pathStart, pathEnd), "UTF-8")
            .takeIf { it.startsWith("template-media/") }
    }
}
