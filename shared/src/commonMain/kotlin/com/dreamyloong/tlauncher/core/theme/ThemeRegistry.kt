package com.dreamyloong.tlauncher.core.theme

import com.dreamyloong.tlauncher.core.i18n.LocalizedText
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget

object BuiltinThemeIds {
    val DAY = ExtensionIdentityId("theme.dreamyloong.day")
    val NIGHT = ExtensionIdentityId("theme.dreamyloong.night")
    val all = setOf(DAY, NIGHT)
}

interface ThemeRegistry {
    fun allThemes(): List<LauncherThemeDefinition>

    fun findTheme(themeId: ExtensionIdentityId): LauncherThemeDefinition? {
        return allThemes().firstOrNull { it.id == themeId }
    }
}

interface ThemeSettingsStore {
    fun currentPreference(): ThemePreference
    fun setCurrentPreference(preference: ThemePreference)
}

interface ThemeResolver {
    fun resolve(
        preference: ThemePreference,
        systemDark: Boolean,
        availableThemes: List<LauncherThemeDefinition>,
    ): LauncherThemeDefinition
}

class DefaultThemeResolver : ThemeResolver {
    override fun resolve(
        preference: ThemePreference,
        systemDark: Boolean,
        availableThemes: List<LauncherThemeDefinition>,
    ): LauncherThemeDefinition {
        val followSystemTheme = builtInThemeFor(
            brightness = if (systemDark) {
                ThemeBrightness.DARK
            } else {
                ThemeBrightness.LIGHT
            },
        )

        return when (preference) {
            ThemePreference.FollowSystem -> followSystemTheme

            is ThemePreference.Fixed -> {
                availableThemes.firstOrNull { it.id == preference.themeId } ?: followSystemTheme
            }
        }
    }
}

fun builtInThemeDefinitions(
    target: PlatformTarget? = null,
): List<LauncherThemeDefinition> {
    return listOf(
        builtInThemeFor(ThemeBrightness.LIGHT, target),
        builtInThemeFor(ThemeBrightness.DARK, target),
    )
}

fun builtInThemeFor(
    brightness: ThemeBrightness,
    target: PlatformTarget? = null,
): LauncherThemeDefinition {
    val supportedTargets = target?.let(::setOf) ?: PlatformTarget.entries.toSet()
    return when (brightness) {
        ThemeBrightness.LIGHT -> LauncherThemeDefinition(
            id = BuiltinThemeIds.DAY,
            name = LocalizedText("白天", "Day"),
            description = LocalizedText(
                "启动器内置的浅色主题。",
                "The launcher's built-in light theme.",
            ),
            brightness = ThemeBrightness.LIGHT,
            launcherIcon = ThemeLauncherIcon.DEFAULT,
            scene = ThemeSceneSpec(
                kind = "daylight-scene",
                supportsAnimation = false,
            ),
            supportedTargets = supportedTargets,
        )

        ThemeBrightness.DARK -> LauncherThemeDefinition(
            id = BuiltinThemeIds.NIGHT,
            name = LocalizedText("黑夜", "Night"),
            description = LocalizedText(
                "启动器内置的深色主题。",
                "The launcher's built-in dark theme.",
            ),
            brightness = ThemeBrightness.DARK,
            launcherIcon = ThemeLauncherIcon.NIGHT,
            scene = ThemeSceneSpec(
                kind = "night-scene",
                supportsAnimation = false,
            ),
            supportedTargets = supportedTargets,
        )
    }
}
