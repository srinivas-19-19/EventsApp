package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OpsPrimaryIndigoLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = OpsAccentTeal,
    onSecondary = Color.White,
    background = OpsDarkBackground,
    onBackground = OpsDarkTextPrimary,
    surface = OpsDarkSurface,
    onSurface = OpsDarkTextPrimary,
    surfaceVariant = OpsDarkSurfaceElevated,
    onSurfaceVariant = OpsDarkTextSecondary,
    outline = OpsDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = OpsPrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = OpsAccentTeal,
    onSecondary = Color.White,
    background = OpsLightBackground,
    onBackground = OpsLightTextPrimary,
    surface = OpsLightSurface,
    onSurface = OpsLightTextPrimary,
    surfaceVariant = OpsLightSurfaceElevated,
    onSurfaceVariant = OpsLightTextSecondary,
    outline = OpsLightBorder
)

@Composable
fun CampusEventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
