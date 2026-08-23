package com.luma.focus.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class LumaWallpaper(val name: String, val brush: Brush)

val LumaWallpapers = listOf(
    LumaWallpaper("Nebula", Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF09090B)))),
    LumaWallpaper("Ocean", Brush.linearGradient(listOf(Color(0xFF075985), Color(0xFF020617)))),
    LumaWallpaper("Sunset", Brush.linearGradient(listOf(Color(0xFFC2410C), Color(0xFF3B0764)))),
    LumaWallpaper("Midnight", Brush.linearGradient(listOf(Color(0xFF172554), Color(0xFF020617)))),
    LumaWallpaper("Forest", Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF052E16)))),
    LumaWallpaper("Aurora", Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF312E81), Color(0xFF111827)))),
    LumaWallpaper("Dream", Brush.linearGradient(listOf(Color(0xFF9D174D), Color(0xFF312E81)))),
    LumaWallpaper("Paper", Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF111827))))
)

fun wallpaperByName(name: String): LumaWallpaper =
    LumaWallpapers.find { it.name == name } ?: LumaWallpapers.first()

// Audio files are bundled in res/raw and looped during focus sessions.
val LumaSounds = listOf("Silence", "Rain", "LoFi", "WhiteNoise", "Forest", "Chime", "Soft", "DeepFocus")
val LumaAlarmSounds = listOf("Chime", "Soft", "DeepFocus", "Rain")
