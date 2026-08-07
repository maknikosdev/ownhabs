package com.habitpulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = HpTeal,
    secondary = HpGreen,
    background = HpDark,
    surface = HpDarkSurface,
    onPrimary = HpWhite,
    onBackground = HpWhite,
    onSurface = HpWhite
)

private val LightColors = lightColorScheme(
    primary = HpTeal,
    secondary = HpGreen,
    background = HpWhite,
    surface = Color(0xFFF5F7FA),
    onPrimary = HpWhite,
    onBackground = HpDark,
    onSurface = HpDark
)

@Composable
fun HabitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = HabitPulseTypography,
        content = content
    )
}
