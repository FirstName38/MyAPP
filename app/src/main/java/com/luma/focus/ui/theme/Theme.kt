package com.luma.focus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LumaTheme(
    accentColor: Color = FocusAccent,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = accentColor,
        secondary = accentColor,
        background = LumaBackground,
        surface = LumaSurface,
        surfaceVariant = LumaSurfaceElevated,
        onBackground = LumaTextPrimary,
        onSurface = LumaTextPrimary,
        onPrimary = Color(0xFF0D0D14)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumaTypography,
        content = content
    )
}
