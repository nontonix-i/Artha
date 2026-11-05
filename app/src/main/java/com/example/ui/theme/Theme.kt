package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = PureWhite,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = CobaltDark,
    secondary = CobaltDark,
    onSecondary = PureWhite,
    secondaryContainer = CobaltLight,
    onSecondaryContainer = CobaltDark,
    tertiary = ProfitGreen,
    onTertiary = PureWhite,
    tertiaryContainer = ProfitGreenLight,
    onTertiaryContainer = ProfitGreen,
    error = DebtRed,
    onError = PureWhite,
    errorContainer = DebtRedLight,
    onErrorContainer = DebtRed,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = PureWhite,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = CobaltPrimary,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = CobaltLight,
    secondary = CobaltLight,
    onSecondary = TextPrimary,
    background = Color(0xFF0F172A),
    onBackground = PureWhite,
    surface = Color(0xFF1E293B),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = DebtRed,
    onError = PureWhite,
    outline = Color(0xFF334155)
)

@Composable
fun ArthaStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ArthaStockTheme(darkTheme = darkTheme, content = content)
}
