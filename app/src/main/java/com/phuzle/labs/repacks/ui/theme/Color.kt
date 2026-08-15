package com.phuzle.labs.repacks.ui.theme

import androidx.compose.ui.graphics.Color

// Neon core palette — the app's fixed identity regardless of light/dark/AMOLED mode. Deliberately
// NOT using Material You dynamic color: a wallpaper-derived palette would undercut the deliberate
// HUD look this app is going for.
val NeonCyan = Color(0xFF00F0FF)
val NeonMagenta = Color(0xFFFF2E88)
val NeonViolet = Color(0xFFB14EFF)
val NeonAcid = Color(0xFFD4FF3E)
val NeonRed = Color(0xFFFF3B5C)

val VoidBlack = Color(0xFF060611)
val VoidSurface = Color(0xFF0D0E1F)
val VoidSurfaceRaised = Color(0xFF15172E)
val VoidOutline = Color(0xFF2A2D4D)

val AmoledBlack = Color(0xFF000000)
val AmoledSurface = Color(0xFF090909)
val AmoledOutline = Color(0xFF232323)

val DaylightBackground = Color(0xFFEFF1FB)
val DaylightSurface = Color(0xFFFFFFFF)
val DaylightSurfaceRaised = Color(0xFFF3F4FC)
val DaylightOutline = Color(0xFFCBD0EE)

val TextOnVoidPrimary = Color(0xFFE7ECFF)
val TextOnVoidSecondary = Color(0xFF8A90B8)
val TextOnDaylightPrimary = Color(0xFF12132B)
val TextOnDaylightSecondary = Color(0xFF585E82)

/** Deterministic accent per provider id, used for card borders/badges (RepackCard, DetailScreen). */
private val providerAccents = listOf(NeonCyan, NeonMagenta, NeonViolet, NeonAcid)

fun accentForProvider(providerId: String): Color {
    val index = providerId.fold(0) { acc, c -> acc + c.code }
    return providerAccents[index % providerAccents.size]
}
