package com.dreamyloong.tlauncher.sdk.theme

import com.dreamyloong.tlauncher.sdk.i18n.LocalizedText
import com.dreamyloong.tlauncher.sdk.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.sdk.model.ExtensionManifest
import com.dreamyloong.tlauncher.sdk.model.PlatformTarget

typealias ThemeProviderExtension = com.dreamyloong.tlauncher.sdk.plugin.ThemeProviderExtension

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

data class ThemeColorTokens(
    val primaryHex: String,
    val secondaryHex: String,
    val tertiaryHex: String,
    val backgroundHex: String,
    val surfaceHex: String,
)

data class ThemeSceneManifest(
    val kind: String,
    val supportsAnimation: Boolean,
    val previewAsset: String? = null,
)

data class ThemePlatformFacet(
    val target: PlatformTarget,
    val brightness: ThemeBrightness,
    val tokens: ThemeColorTokens,
    val scene: ThemeSceneManifest,
    val launcherIcon: ThemeLauncherIcon = ThemeLauncherIcon.DEFAULT,
)

data class ThemePackage(
    val extension: ExtensionManifest,
    val schemaVersion: Int,
    val name: LocalizedText,
    val author: String,
    val version: String,
    val description: LocalizedText,
    val platforms: List<ThemePlatformFacet>,
) {
    init {
        require(platforms.isNotEmpty()) { "ThemePackage platforms must not be empty." }
        require(platforms.map { facet -> facet.target }.toSet() == extension.supportedTargets) {
            "ThemePackage platforms must match extension.supportedTargets."
        }
    }

    val supportedTargets: Set<PlatformTarget>
        get() = platforms.map { facet -> facet.target }.toSet()

    val defaultBrightness: ThemeBrightness
        get() = platforms.first().brightness

    val tokens: ThemeColorTokens
        get() = platforms.first().tokens

    val scene: ThemeSceneManifest
        get() = platforms.first().scene

    fun facetFor(target: PlatformTarget): ThemePlatformFacet? {
        return platforms.firstOrNull { facet -> facet.target == target }
    }
}

interface ThemePackageRegistry {
    fun packages(): List<ThemePackage>
}

interface ThemePackageParser {
    fun parse(
        manifestText: String,
        tokensText: String,
        sceneText: String,
    ): ThemePackage
}

fun ThemePackage.toThemeDefinition(target: PlatformTarget? = null): LauncherThemeDefinition {
    val selectedFacet = target?.let(::facetFor) ?: platforms.first()
    return LauncherThemeDefinition(
        id = ExtensionIdentityId(extension.identityId),
        name = name,
        description = description,
        brightness = selectedFacet.brightness,
        launcherIcon = selectedFacet.launcherIcon,
        scene = ThemeSceneSpec(
            kind = selectedFacet.scene.kind,
            supportsAnimation = selectedFacet.scene.supportsAnimation,
        ),
        supportedTargets = supportedTargets,
    )
}
