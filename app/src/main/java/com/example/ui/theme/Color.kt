package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Identity — Obsidian, Midnight, Electric Cyan & Silver
val ObsidianVoid = Color(0xFF04060D)
val MidnightBlack = Color(0xFF090D18)
val DeepSpaceSurface = Color(0xFF0E1525)
val ElevatedGlassSurface = Color(0xFF131C30)
val SubtleSlateSurface = Color(0xFF1B263F)

val ElectricCyan = Color(0xFF22D3EE)
val SoftCyanGlow = Color(0xFF38BDF8)
val IceCyan = Color(0xFFA5F3FC)
val DeepCyanContainer = Color(0xFF083344)

val SilverPrimary = Color(0xFFE2E8F0)
val SilverSecondary = Color(0xFF94A3B8)
val MutedSlateText = Color(0xFF64748B)

val GlassBorder = Color(0x2894A3B8)
val CyanGlassBorder = Color(0x4D22D3EE)

val StatusSuccess = Color(0xFF34D399)
val StatusWarning = Color(0xFFFBBF24)
val StatusError = Color(0xFFF87171)

fun parseColorHexOrDefault(hex: String, fallback: Color = ElectricCyan): Color {
  return try {
    val clean = hex.trim().removePrefix("#")
    val parsed = when (clean.length) {
      6 -> 0xFF000000L or clean.toLong(16)
      8 -> clean.toLong(16)
      else -> return fallback
    }
    Color(parsed)
  } catch (_: Exception) {
    fallback
  }
}
