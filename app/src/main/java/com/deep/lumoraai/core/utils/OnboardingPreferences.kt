package com.deep.lumoraai.core.utils

import android.content.Context

object OnboardingPreferences {
    private const val PREFS = "lumora_onboarding"
    private const val KEY_COMPLETED = "completed"
    private const val KEY_PROFILE_HINT_SEEN = "profile_hint_seen"

    fun isCompleted(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETED, false)

    fun markCompleted(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_COMPLETED, true)
            .apply()
    }

    fun isProfileHintSeen(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_PROFILE_HINT_SEEN, false)

    fun markProfileHintSeen(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PROFILE_HINT_SEEN, true)
            .apply()
    }

    fun reset(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_COMPLETED)
            .remove(KEY_PROFILE_HINT_SEEN)
            .apply()
    }
}
