package com.phuzle.labs.repacks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Neon core palette — the app's fixed identity regardless of light/dark/AMOLED mode. Deliberately
// NOT using Material You dynamic color: a wallpaper-derived palette would undercut the deliberate
// HUD look this app is going for.
val NeonCyan = Color(0xFF00F0FF)
val NeonMagenta = Color(0xFFFF2E88)
val NeonViolet = Color(0xFFB14EFF)
val NeonAcid = Color(0xFFD4FF3E)
val NeonRed = Color(0xFFFF3B5C)

// Light-theme counterparts of the neon palette above. The raw neon values (esp. acid yellow-green)
// are tuned for contrast against near-black surfaces and become nearly unreadable on a white/light
// background — these are darkened/saturated so text and borders drawn in "the accent color" stay
// legible in Light theme. Values match the roles already used by DaylightColors in Theme.kt.
val NeonCyanOnLight = Color(0xFF0086A8)
val NeonMagentaOnLight = Color(0xFFC81E68)
val NeonVioletOnLight = Color(0xFF7A2FD1)
val NeonAcidOnLight = Color(0xFF6E8C00)

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
private val darkProviderAccents = listOf(NeonCyan, NeonMagenta, NeonViolet, NeonAcid)
private val lightProviderAccents = listOf(NeonCyanOnLight, NeonMagentaOnLight, NeonVioletOnLight, NeonAcidOnLight)

/** True when the current color scheme's background is light (i.e. Light theme, not
 * Void/AMOLED dark) — used to pick a readable accent variant instead of assuming dark mode. */
@Composable
fun isLightSurface(): Boolean = MaterialTheme.colorScheme.background.luminance() > 0.5f

/** Picks [light] or [dark] based on the current theme, so a single "accent color" constant can
 * stay legible whether it's drawn on the void-black or daylight background. */
@Composable
fun themedAccent(dark: Color, light: Color): Color = if (isLightSurface()) light else dark

@Composable
fun accentForProvider(providerId: String): Color {
    val palette = if (isLightSurface()) lightProviderAccents else darkProviderAccents
    val index = providerId.fold(0) { acc, c -> acc + c.code }
    return palette[index % palette.size]
}
