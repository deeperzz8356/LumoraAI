package com.deep.lumoraai.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColors = darkColorScheme(
    primary = LumoraPrimary,
    secondary = LumoraSecondary,
    tertiary = LumoraAccent,
    background = LumoraBackground,
    surface = LumoraSurface,
    surfaceVariant = LumoraSurfaceHigh,
    error = LumoraError,
    onPrimary = LumoraBackground,
    onSecondary = LumoraBackground,
    onBackground = LumoraTextPrimary,
    onSurface = LumoraTextPrimary,
    onSurfaceVariant = LumoraTextSecondary,
    outline = LumoraOutline
)

private val LightColors = lightColorScheme(
    primary = LumoraPrimary,
    secondary = LumoraSecondary,
    tertiary = LumoraAccent,
    background = LumoraLightBackground,
    surface = LumoraLightSurface,
    surfaceVariant = LumoraLightSurfaceVariant,
    error = LumoraError,
    onPrimary = LumoraBackground,
    onSecondary = LumoraBackground,
    onBackground = LumoraLightText,
    onSurface = LumoraLightText,
    onSurfaceVariant = LumoraLightTextSecondary,
    outline = LumoraLightOutline
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