package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepForest,
    onPrimary = PureWhite,
    primaryContainer = SurfaceVariantColor,
    onPrimaryContainer = PrimaryText,
    secondary = Charcoal,
    onSecondary = PureWhite,
    background = WarmIvory,
    onBackground = PrimaryText,
    surface = SurfaceColor,
    onSurface = PrimaryText,
    surfaceVariant = SurfaceVariantColor,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor,
    outlineVariant = BorderColor
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WarmIvory.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
