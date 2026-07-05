package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Default Dark Theme: Deep Blue-Green Night Sky
private val DarkDeenColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = Gold,
    onTertiary = DarkGreen,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

// 2. Light Theme: Soft Nature Green
private val LightDeenColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = SurfaceLight,
    onPrimaryContainer = TextDark,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = Gold,
    onTertiary = DarkGreen,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    error = ErrorRed,
    onError = Color.White
)

// 3. Alternate Full Green Theme
private val GreenDeenColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkGreen,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = LightGold,
    onTertiary = DarkGreen,
    background = DarkGreen,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun DeenPathTheme(
    themeName: String = "DARK", // "DARK", "LIGHT", "GREEN"
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "LIGHT" -> LightDeenColorScheme
        "GREEN" -> GreenDeenColorScheme
        else -> DarkDeenColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    themeName: String = "DARK",
    content: @Composable () -> Unit
) {
    DeenPathTheme(themeName = themeName, content = content)
}
