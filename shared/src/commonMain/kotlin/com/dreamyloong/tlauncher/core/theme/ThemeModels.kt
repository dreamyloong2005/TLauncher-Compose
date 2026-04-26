package com.dreamyloong.tlauncher.core.theme

import com.dreamyloong.tlauncher.core.i18n.LocalizedText
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget

sealed interface ThemePreference {
    data object FollowSystem : ThemePreference

    data class Fixed(val themeId: ExtensionIdentityId) : ThemePreference
}

enum class ThemeBrightness {
    LIGHT,
    DARK,
}

enum class ThemeLauncherIcon {
    DEFAULT,
    NIGHT,
}

data class ThemeSceneSpec(
    val kind: String,
    val supportsAnimation: Boolean,
)

data class LauncherThemeDefinition(
    val id: ExtensionIdentityId,
    val name: LocalizedText,
    val description: LocalizedText,
    val brightness: ThemeBrightness,
    val scene: ThemeSceneSpec,
    val supportedTargets: Set<PlatformTarget> = PlatformTarget.entries.toSet(),
    val launcherIcon: ThemeLauncherIcon = ThemeLauncherIcon.DEFAULT,
)
