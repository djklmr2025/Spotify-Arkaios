package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanLight,
    onPrimary = Color(0xFF08080C),
    primaryContainer = CyanDark,
    onPrimaryContainer = Color(0xFFE0F7FA),
    secondary = BlueAccent,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = IndigoDeep,
    onSecondaryContainer = CyanLight,
    tertiary = ArkaiosGold,
    onTertiary = Color(0xFF08080C),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = ErrorColor
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Arkaios-Tify Elegant Dark Theme
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}


