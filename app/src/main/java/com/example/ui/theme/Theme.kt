package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAccentTheme = compositionLocalOf { AccentTheme.WHITE }

fun createClockColorScheme(accent: AccentTheme) = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color.Black,
    primaryContainer = accent.primaryMuted,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE4E4E7),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFD4D4D8),
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    error = Color.White,
    onError = Color.Black
)

@Composable
fun ClockTheme(
    accent: AccentTheme = AccentTheme.WHITE,
    content: @Composable () -> Unit
) {
    val colorScheme = createClockColorScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = DarkBg.toArgb()
                it.navigationBarColor = DarkBg.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    CompositionLocalProvider(LocalAccentTheme provides accent) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
