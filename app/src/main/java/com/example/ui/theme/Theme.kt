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

private val DarkColorScheme =
  darkColorScheme(
    primary = GeoPrimary,
    secondary = GeoPresentBg,
    tertiary = GeoAbsentBg,
    background = Color(0xFF12131A),
    surface = Color(0xFF1C1D24),
    onPrimary = Color.White,
    onSecondary = GeoPresentText,
    onTertiary = GeoAbsentText,
    onBackground = Color(0xFFE2E2E9),
    onSurface = Color(0xFFE2E2E9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GeoPrimary,
    secondary = GeoPresentBg,
    tertiary = GeoAbsentBg,
    background = GeoBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = GeoPresentText,
    onTertiary = GeoAbsentText,
    onBackground = GeoText,
    onSurface = GeoText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to show custom theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
