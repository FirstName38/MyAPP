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
    LumaWallpaper("Lavender", Brush.linearGradient(listOf(Color(0xFF6D5BA6), Color(0xFFE9D5FF)))),
    LumaWallpaper("Creamy", Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color(0xFFE7D8C9)))),
    LumaWallpaper("Rose", Brush.linearGradient(listOf(Color(0xFF9F5264), Color(0xFFF3D5DC)))),
    LumaWallpaper("Sage", Brush.linearGradient(listOf(Color(0xFF56745B), Color(0xFFDCE8D8)))),
    LumaWallpaper("Moonlight", Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFFCBD5E1))))
)

fun wallpaperByName(name: String): LumaWallpaper = LumaWallpapers.find { it.name == name } ?: LumaWallpapers.first()

// Sound categories are deliberately separated so each timer phase can have its own audio identity.
val LumaSounds = listOf("Silence", "Rain", "LoFi", "WhiteNoise", "Forest", "Soft", "DeepFocus")
val LumaBreakSounds = listOf("Silence", "Chime", "Soft", "Rain", "Forest")
val LumaAlarmSounds = listOf("Chime", "Soft", "DeepFocus", "Rain")
