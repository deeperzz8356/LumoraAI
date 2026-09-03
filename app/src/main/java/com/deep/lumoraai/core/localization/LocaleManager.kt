package com.deep.lumoraai.core.localization

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    val supportedCodes = setOf("en", "vi", "es", "fr", "de", "it", "pt", "tr", "ar", "hi", "ko", "zh")

    fun normalize(code: String?): String =
        code?.lowercase(Locale.ROOT)?.takeIf { it in supportedCodes } ?: "en"

    fun apply(context: Context, code: String): Context {
        val locale = Locale.forLanguageTag(normalize(code))
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
