package com.deep.lumoraai.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.deep.lumoraai.core.utils.GuestIdentity
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class EditableProfile(
    val fullName: String,
    val username: String,
    val email: String,
    val bio: String,
    val location: String,
    val avatarUri: String?,
)

object ProfilePreferences {
    private const val PREFS = "lumora_profile"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_BIO = "bio"
    private const val KEY_LOCATION = "location"
    private const val KEY_AVATAR_URI = "avatar_uri"

    fun load(context: Context, user: FirebaseUser?): EditableProfile {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fallbackName = GuestIdentity.displayName(context, user)
        val fallbackUsername = GuestIdentity.subtitle(context, user).removePrefix("@")
        return EditableProfile(
            fullName = prefs.getString(scopedKey(user, KEY_FULL_NAME), null)?.ifBlank { null } ?: fallbackName,
            username = prefs.getString(scopedKey(user, KEY_USERNAME), null)?.ifBlank { null } ?: fallbackUsername,
            email = prefs.getString(scopedKey(user, KEY_EMAIL), null)?.ifBlank { null } ?: user?.email.orEmpty(),
            bio = prefs.getString(scopedKey(user, KEY_BIO), "").orEmpty(),
            location = prefs.getString(scopedKey(user, KEY_LOCATION), "").orEmpty(),
            avatarUri = prefs.getString(scopedKey(user, KEY_AVATAR_URI), null),
        )
    }

    fun save(context: Context, user: FirebaseUser?, profile: EditableProfile) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(scopedKey(user, KEY_FULL_NAME), profile.fullName.trim())
            .putString(scopedKey(user, KEY_USERNAME), profile.username.trim().removePrefix("@").lowercase(Locale.US))
            .putString(scopedKey(user, KEY_EMAIL), profile.email.trim())
            .putString(scopedKey(user, KEY_BIO), profile.bio.trim())
            .putString(scopedKey(user, KEY_LOCATION), profile.location.trim())
            .putString(scopedKey(user, KEY_AVATAR_URI), profile.avatarUri)
            .apply()
    }

    fun copyAvatarToPrivateStorage(
        context: Context,
        sourceUri: Uri,
        viewportSize: Int = 0,
        zoom: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
    ): String? {
        val directory = File(context.applicationContext.filesDir, "profile")
        if (!directory.exists()) directory.mkdirs()
        val destination = File(directory, "avatar_${System.currentTimeMillis()}.jpg")
        return runCatching {
            val bitmap = decodeBitmap(context, sourceUri)
            val baseViewport = viewportSize.takeIf { it > 0 } ?: minOf(bitmap.width, bitmap.height)
            val baseScale = maxOf(
                baseViewport.toFloat() / bitmap.width.toFloat(),
                baseViewport.toFloat() / bitmap.height.toFloat()
            )
            val imageScale = baseScale * zoom.coerceAtLeast(1f)
            val cropSize = (baseViewport / imageScale).toInt().coerceIn(1, minOf(bitmap.width, bitmap.height))
            val centerX = (bitmap.width / 2f) - (offsetX / imageScale)
            val centerY = (bitmap.height / 2f) - (offsetY / imageScale)
            val left = (centerX - cropSize / 2f).toInt().coerceIn(0, bitmap.width - cropSize)
            val top = (centerY - cropSize / 2f).toInt().coerceIn(0, bitmap.height - cropSize)
            val square = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)
            FileOutputStream(destination).use { output ->
                square.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            Uri.fromFile(destination).toString()
        }.getOrNull()
    }

    fun isValidUsername(value: String): Boolean {
        val clean = value.trim().removePrefix("@")
        return clean.length in 3..30 && clean.all { it.isLetterOrDigit() || it == '_' || it == '.' }
    }

    private fun scopedKey(user: FirebaseUser?, key: String): String {
        val owner = user?.uid ?: "guest"
        return "$owner.$key"
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
}
