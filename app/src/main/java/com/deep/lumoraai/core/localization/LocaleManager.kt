package com.deep.lumoraai.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {
    val supportedCodes = setOf("en", "vi", "es", "fr", "de", "it", "pt", "tr", "ar", "hi", "ko", "zh")

    fun normalize(code: String?): String =
        code?.lowercase(Locale.ROOT)?.takeIf { it in supportedCodes } ?: "en"

    /**
     * Wraps a base context with the configuration for [code]. Used from
     * Activity.attachBaseContext so the very first frame renders in the right
     * language even before the AppCompat delegate applies.
     */
    fun apply(context: Context, code: String): Context {
        val locale = Locale.forLanguageTag(normalize(code))
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    /**
     * Applies [code] as the app-wide locale using the AndroidX per-app locale
     * API. This reliably updates every activity (and survives process restarts
     * via the storage the framework/AppCompat manages), which the manual
     * Configuration + recreate() approach did not do consistently.
     */
    fun applyAppLocale(code: String) {
        val normalized = normalize(code)
        val locales = LocaleListCompat.forLanguageTags(normalized)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** The currently applied app locale tag, or the persisted default fallback. */
    fun currentAppLocale(fallback: String): String {
        val applied = AppCompatDelegate.getApplicationLocales()
        if (applied.isEmpty) return normalize(fallback)
        return normalize(applied[0]?.language)
    }
}
