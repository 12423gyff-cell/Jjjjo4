package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    textSizeMultiplier: Float = 1.0f,
    accentColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Get selected accent color based on user choice
    // 0=Gold (Sunny Veranda), 1=Dark Blue, 2=Light Blue, 3=Red, 4=Green, 5=Gray
    val selectedAccentColor = when (accentColorIndex) {
        1 -> Color(0xFF1E40AF) // Dark Blue (Premium Royal Dark Blue)
        2 -> Color(0xFF60A5FA) // Light Blue (Sky Blue)
        3 -> Color(0xFFDC2626) // Red (Elegant Red)
        4 -> Color(0xFF16A34A) // Green (Beautiful Islamic Green)
        5 -> Color(0xFF6B7280) // Gray (Sleek Slate/Steel Gray)
        else -> Color(0xFFFFDE95) // Gold (Default - Sunny Veranda)
    }

    val colorScheme = baseColorScheme.copy(
        secondary = selectedAccentColor,
        secondaryContainer = selectedAccentColor.copy(alpha = 0.2f)
    )

    val scaledTypography = Typography.copy(
        displayLarge = Typography.displayLarge.copy(fontSize = Typography.displayLarge.fontSize * textSizeMultiplier),
        displayMedium = Typography.displayMedium.copy(fontSize = Typography.displayMedium.fontSize * textSizeMultiplier),
        displaySmall = Typography.displaySmall.copy(fontSize = Typography.displaySmall.fontSize * textSizeMultiplier),
        headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * textSizeMultiplier),
        headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * textSizeMultiplier),
        headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * textSizeMultiplier),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * textSizeMultiplier),
        titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * textSizeMultiplier),
        titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * textSizeMultiplier),
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * textSizeMultiplier),
        bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * textSizeMultiplier),
        bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * textSizeMultiplier),
        labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * textSizeMultiplier),
        labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * textSizeMultiplier),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * textSizeMultiplier)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
