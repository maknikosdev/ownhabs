package com.ownhabs.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = OhCyan,
    secondary = OhGreen,
    background = OhDark,
    surface = OhDarkSurface,
    onPrimary = OhWhite,
    onBackground = OhWhite,
    onSurface = OhWhite
)

private val LightColors = lightColorScheme(
    primary = OhCyan,
    secondary = OhGreen,
    background = OhWhite,
    surface = Color(0xFFF5F7FA),
    onPrimary = OhWhite,
    onBackground = OhDark,
    onSurface = OhDark
)

@Composable
fun OwnHabsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OwnHabsTypography,
        content = content
    )
}
