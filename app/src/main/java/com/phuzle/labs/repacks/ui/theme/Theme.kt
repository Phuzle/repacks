package com.phuzle.labs.repacks.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.phuzle.labs.repacks.data.prefs.ThemeMode

private val LightColors = lightColorScheme(
    primary = RepacksPrimary,
    secondary = RepacksSecondary,
    tertiary = RepacksTertiary,
    background = LightBackground,
    surface = LightSurface,
)

private val DarkColors = darkColorScheme(
    primary = RepacksPrimary,
    secondary = RepacksSecondary,
    tertiary = RepacksTertiary,
    background = DarkBackground,
    surface = DarkSurface,
)

private val AmoledColors = DarkColors.copy(
    background = AmoledBackground,
    surface = AmoledSurface,
)

@Composable
fun RepacksTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK_AMOLED -> true
    }
    val context = LocalContext.current
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        themeMode == ThemeMode.DARK_AMOLED -> AmoledColors
        dynamicColorSupported && useDark -> dynamicDarkColorScheme(context)
        dynamicColorSupported && !useDark -> dynamicLightColorScheme(context)
        useDark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepacksTypography,
        content = content,
    )
}
