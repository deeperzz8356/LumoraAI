package com.deep.lumoraai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
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

private val LightColors = lightColorScheme(
    primary = Color(0xFF595D73),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE1FB),
    onPrimaryContainer = Color(0xFF161A2D),
    inversePrimary = LumoraPrimary,
    secondary = Color(0xFF2F2EBE),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E0FF),
    onSecondaryContainer = Color(0xFF07006C),
    tertiary = Color(0xFF334282),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = Color(0xFF001354),
    background = Color(0xFFF9F9FB),
    onBackground = Color(0xFF131313),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF131313),
    surfaceVariant = Color(0xFFE5E2E1),
    onSurfaceVariant = Color(0xFF46464C),
    surfaceTint = Color(0xFF595D73),
    inverseSurface = Color(0xFF131313),
    inverseOnSurface = Color(0xFFF9F9FB),
    outline = Color(0xFF919097),
    outlineVariant = Color(0xFFC7C5CD),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun LumoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSpacing provides LumoraSpacing()) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
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