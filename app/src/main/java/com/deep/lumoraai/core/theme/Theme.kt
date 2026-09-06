package com.deep.lumoraai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.TextUtilsCompat
import java.util.Locale as JavaLocale

private val AppColorScheme = darkColorScheme(
    primary = LumoraPrimary,
    onPrimary = LumoraOnPrimary,
    primaryContainer = LumoraPrimaryContainer,
    onPrimaryContainer = LumoraOnPrimaryContainer,
    inversePrimary = LumoraInversePrimary,
    secondary = LumoraSecondary,
    onSecondary = LumoraOnSecondary,
    secondaryContainer = LumoraSecondaryContainer,
    onSecondaryContainer = LumoraOnSecondaryContainer,
    tertiary = LumoraTertiary,
    onTertiary = LumoraOnTertiary,
    tertiaryContainer = LumoraTertiaryContainer,
    onTertiaryContainer = LumoraOnTertiaryContainer,
    background = LumoraBackground,
    onBackground = LumoraOnBackground,
    surface = LumoraSurface,
    onSurface = LumoraOnSurface,
    surfaceVariant = LumoraSurfaceVariant,
    onSurfaceVariant = LumoraOnSurfaceVariant,
    surfaceTint = LumoraSurfaceTint,
    inverseSurface = LumoraInverseSurface,
    inverseOnSurface = LumoraInverseOnSurface,
    outline = LumoraOutline,
    outlineVariant = LumoraOutlineVariant,
    error = LumoraError,
    onError = LumoraOnError,
    errorContainer = LumoraErrorContainer,
    onErrorContainer = LumoraOnErrorContainer
)

@Composable
fun LumoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Derive the layout direction from the applied locale on every render so a
    // language change (e.g. to Arabic) reliably lays out RTL, while every LTR
    // locale stays LTR. The configuration is re-read on recreate, so this
    // follows the currently applied locale.
    val localeTag = Locale.current.language
    val layoutDirection = run {
        val locale = JavaLocale.forLanguageTag(localeTag)
        val dir = TextUtilsCompat.getLayoutDirectionFromLocale(locale)
        if (dir == android.view.View.LAYOUT_DIRECTION_RTL) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
    CompositionLocalProvider(
        LocalSpacing provides LumoraSpacing(),
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = LumoraTypography,
            shapes = LumoraShapes,
            content = content
        )
    }
}

@Composable
fun LumoraAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = LumoraTheme(darkTheme = darkTheme, content = content)