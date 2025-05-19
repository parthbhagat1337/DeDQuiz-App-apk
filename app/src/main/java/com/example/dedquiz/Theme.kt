package com.example.dedquiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🔹 Primary Cyberpunk-Inspired Colors
val TechBlue = Color(0xFF00F3FF)       // Neon Blue - Main accent color
val PurpleCTO = Color(0xFF8E44AD)       // Deep Purple - Used for shapes/borders
val Cyan = Color(0xFF00FFFF)            // Bright Cyan - For subtitles, text highlights
val RedAlert = Color(0xFFFF3B30)        // Warning/Alert red
val GreenPulse = Color(0xFF39FF14)      // Success green
val WhiteGlitch = Color(0xFFF0F0F0)     // Bright white for readable text

// 🔹 Background & Surface Colors
val AlmostBlack = Color(0xFF080808)     // Very dark black background
val SemiTransparentBlack = Color(0x80000000)  // 50% transparent black
val DarkGraySurface = Color(0xFF1A1A1A) // Slightly lighter than black for cards/surfaces

// 🎨 Material Color Scheme using Cyberpunk Palette
private val DarkColorScheme = darkColorScheme(
    primary = TechBlue,
    onPrimary = Color.Black,
    primaryContainer = PurpleCTO,
    onPrimaryContainer = Color.White,

    background = AlmostBlack,
    onBackground = WhiteGlitch,

    surface = DarkGraySurface,
    onSurface = WhiteGlitch,
    surfaceVariant = Color(0xFF2C3137),
    onSurfaceVariant = Color.White,

    outline = TechBlue.copy(alpha = 0.5f),

    error = RedAlert,
    onError = Color.White
)

// 🌈 Main App Theme Composable
@Composable
fun DeDQuizTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}