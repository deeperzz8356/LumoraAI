package com.deep.lumoraai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
    CompositionLocalProvider(LocalSpacing provides LumoraSpacing()) {
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