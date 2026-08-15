package com.phuzle.labs.repacks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.phuzle.labs.repacks.data.prefs.ThemeMode

// Fixed neon-on-void palette — deliberately not Material You dynamic color (see Color.kt).
private val VoidColors = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidBlack,
    secondary = NeonMagenta,
    onSecondary = VoidBlack,
    tertiary = NeonViolet,
    onTertiary = VoidBlack,
    error = NeonRed,
    onError = VoidBlack,
    background = VoidBlack,
    onBackground = TextOnVoidPrimary,
    surface = VoidSurface,
    onSurface = TextOnVoidPrimary,
    surfaceVariant = VoidSurfaceRaised,
    onSurfaceVariant = TextOnVoidSecondary,
    outline = VoidOutline,
)

private val AmoledColors = VoidColors.copy(
    background = AmoledBlack,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurface,
    outline = AmoledOutline,
)

private val DaylightColors = lightColorScheme(
    primary = Color(0xFF0086A8), // legible cyan-teal against a light ground
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFC81E68),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7A2FD1),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFD1223D),
    onError = Color(0xFFFFFFFF),
    background = DaylightBackground,
    onBackground = TextOnDaylightPrimary,
    surface = DaylightSurface,
    onSurface = TextOnDaylightPrimary,
    surfaceVariant = DaylightSurfaceRaised,
    onSurfaceVariant = TextOnDaylightSecondary,
    outline = DaylightOutline,
)

@Composable
fun RepacksTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK_AMOLED -> true
    }
    val colorScheme = when {
        themeMode == ThemeMode.DARK_AMOLED -> AmoledColors
        useDark -> VoidColors
        else -> DaylightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepacksTypography,
        shapes = RepacksShapes,
        content = content,
    )
}
