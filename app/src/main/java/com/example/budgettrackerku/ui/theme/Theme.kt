package com.example.budgettrackerku.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = GradientEnd,
    tertiary = GradientStart,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = White,
    surfaceVariant = Color(0xFF3A3A3A),
    outline = Color(0xFF5A5A5A)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = GradientEnd,
    tertiary = GradientStart,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Black,
    onSurface = Black,
    surfaceVariant = LightGray,
    outline = BorderColor
)

@Composable
fun BudgetTrackerKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // The design shows a solid blue background for the header area including status bar
            window.statusBarColor = colorScheme.primary.toArgb()
            // Status bar icons should be light to contrast with the blue background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
