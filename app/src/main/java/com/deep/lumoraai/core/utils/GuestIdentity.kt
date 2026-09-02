package com.deep.lumoraai.core.utils

import android.content.Context
import android.provider.Settings
import com.google.firebase.auth.FirebaseUser
import java.security.MessageDigest
import java.util.Locale

object GuestIdentity {
    private const val PREFS = "lumora_guest_identity"
    private const val KEY_TRIAL_STARTED = "trial_started"
    private const val KEY_TRIAL_EXHAUSTED = "trial_exhausted"

    fun displayName(context: Context, user: FirebaseUser?): String {
        return when {
            user == null -> guestName(context)
            !user.isAnonymous -> user.displayName
                ?: user.email?.substringBefore("@")?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                ?: "Lumora Creator"
            else -> guestName(context)
        }
    }

    fun subtitle(context: Context, user: FirebaseUser?): String {
        return when {
            user == null -> "@${guestHandle(context)}"
            !user.isAnonymous -> user.email ?: "@${displayName(context, user).lowercase(Locale.getDefault()).replace(" ", "_")}"
            else -> "@${guestHandle(context)}"
        }
    }

    fun markTrialStarted(context: Context) {
        prefs(context).edit().putBoolean(KEY_TRIAL_STARTED, true).apply()
    }

    fun markTrialExhausted(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_TRIAL_STARTED, true)
            .putBoolean(KEY_TRIAL_EXHAUSTED, true)
            .apply()
    }

    fun isTrialExhausted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TRIAL_EXHAUSTED, false)

    private fun guestName(context: Context): String = "Guest-${deviceToken(context)}"

    private fun guestHandle(context: Context): String = "guest_${deviceToken(context).lowercase(Locale.US)}"

    private fun deviceToken(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "lumora-device"
        return sha256(androidId).take(6).uppercase(Locale.US)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
