package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PremiumFintechLightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = TextInverse,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueHover,
    secondary = BrandBlue,
    onSecondary = TextInverse,
    secondaryContainer = SurfaceSubtle,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandNavyLight,
    onTertiary = TextInverse,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force the intentional Premium Light Fintech Theme across all presentation devices
    MaterialTheme(
        colorScheme = PremiumFintechLightColorScheme,
        typography = Typography,
        content = content
    )
}
