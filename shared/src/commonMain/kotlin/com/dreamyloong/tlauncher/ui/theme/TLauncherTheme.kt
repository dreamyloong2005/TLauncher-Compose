package com.dreamyloong.tlauncher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dreamyloong.tlauncher.core.theme.LauncherThemeDefinition
import com.dreamyloong.tlauncher.core.theme.ThemeBrightness
import com.dreamyloong.tlauncher.core.theme.builtInThemeFor

private val DayThemeColors = lightColorScheme(
    primary = Color(0xFF0B84FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E9FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF29A9E6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2F3FF),
    onSecondaryContainer = Color(0xFF001F2B),
    tertiary = Color(0xFF5266FF),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE1E3FF),
    onTertiaryContainer = Color(0xFF0B146B),
    background = Color(0xFFF4FAFF),
    onBackground = Color(0xFF111C2B),
    surface = Color(0xFFF7FBFF),
    onSurface = Color(0xFF111C2B),
    surfaceContainer = Color(0xFFE7F2FF),
    surfaceContainerHigh = Color(0xFFDBEBFF),
    onSurfaceVariant = Color(0xFF435467),
)

private val NightThemeColors = darkColorScheme(
    primary = Color(0xFF48B6FF),
    onPrimary = Color(0xFF002340),
    primaryContainer = Color(0xFF00355F),
    onPrimaryContainer = Color(0xFFD3E9FF),
    secondary = Color(0xFF7CE3FF),
    onSecondary = Color(0xFF003545),
    secondaryContainer = Color(0xFF004D63),
    onSecondaryContainer = Color(0xFFC9F2FF),
    tertiary = Color(0xFF92A0FF),
    onTertiary = Color(0xFF16206E),
    tertiaryContainer = Color(0xFF2F3A8D),
    onTertiaryContainer = Color(0xFFE0E3FF),
    background = Color(0xFF08111F),
    onBackground = Color(0xFFE2ECFF),
    surface = Color(0xFF0C1626),
    onSurface = Color(0xFFE2ECFF),
    surfaceContainer = Color(0xFF122033),
    surfaceContainerHigh = Color(0xFF1A2A40),
    onSurfaceVariant = Color(0xFFB5C7E3),
)

@Composable
fun TLauncherTheme(
    theme: LauncherThemeDefinition? = null,
    content: @Composable () -> Unit,
) {
    val resolvedTheme = theme ?: builtInThemeFor(
        brightness = if (rememberSystemDarkTheme()) {
            ThemeBrightness.DARK
        } else {
            ThemeBrightness.LIGHT
        },
    )
    MaterialTheme(
        colorScheme = colorSchemeFor(resolvedTheme),
        content = content,
    )
}

private fun colorSchemeFor(theme: LauncherThemeDefinition): ColorScheme {
    return if (theme.brightness == ThemeBrightness.DARK) {
        NightThemeColors
    } else {
        DayThemeColors
    }
}
