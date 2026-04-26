package com.dreamyloong.tlauncher.core.model

import com.dreamyloong.tlauncher.core.i18n.LocalizedText

enum class PlatformTarget {
    WINDOWS,
    ANDROID,
    IOS,
    MACOS,
}

enum class LaunchSupportLevel {
    BROWSE_ONLY,
    INSTALLABLE,
    LAUNCHABLE,
}

data class ExtensionIdentityId(val value: String)

data class RuntimeRequirement(
    val engine: String? = null,
    val language: String? = null,
    val version: String? = null,
)

enum class TemplateSourceType {
    OFFICIAL,
    COMMUNITY,
    PLUGIN,
}

enum class TemplateReleaseState {
    DRAFT,
    EXPERIMENTAL,
    READY,
}

data class TemplateTargetFacet(
    val target: PlatformTarget,
    val supportLevel: LaunchSupportLevel,
    val runtimeRequirements: List<RuntimeRequirement> = emptyList(),
    val capabilityKeys: Set<String> = emptySet(),
    val capabilityLabels: Map<String, LocalizedText> = emptyMap(),
    val notes: LocalizedText? = null,
)

data class TemplateDescriptor(
    val packageId: ExtensionIdentityId,
    val name: LocalizedText,
    val description: LocalizedText,
    val defaultInstanceDescription: LocalizedText? = null,
    val schemaVersion: Int,
    val sourceType: TemplateSourceType = TemplateSourceType.OFFICIAL,
    val releaseState: TemplateReleaseState = TemplateReleaseState.READY,
    val notes: LocalizedText? = null,
    val platforms: List<TemplateTargetFacet>,
) {
    init {
        require(platforms.isNotEmpty()) { "TemplateDescriptor platforms must not be empty." }
    }

    val supportedTargets: Set<PlatformTarget>
        get() = platforms.map { facet -> facet.target }.toSet()

    fun facetFor(target: PlatformTarget): TemplateTargetFacet? {
        return platforms.firstOrNull { facet -> facet.target == target }
    }

    fun resolve(target: PlatformTarget): Template? {
        val facet = facetFor(target) ?: return null
        return Template(
            packageId = packageId,
            name = name,
            description = description,
            defaultInstanceDescription = defaultInstanceDescription,
            target = target,
            supportedTargets = supportedTargets,
            runtimeRequirements = facet.runtimeRequirements,
            capabilityKeys = facet.capabilityKeys,
            capabilityLabels = facet.capabilityLabels,
            supportLevel = facet.supportLevel,
            schemaVersion = schemaVersion,
            sourceType = sourceType,
            releaseState = releaseState,
            notes = facet.notes ?: notes,
        )
    }
}

data class Template(
    val packageId: ExtensionIdentityId,
    val name: LocalizedText,
    val description: LocalizedText,
    val defaultInstanceDescription: LocalizedText? = null,
    val target: PlatformTarget,
    val supportedTargets: Set<PlatformTarget>,
    val runtimeRequirements: List<RuntimeRequirement>,
    val capabilityKeys: Set<String>,
    val capabilityLabels: Map<String, LocalizedText> = emptyMap(),
    val supportLevel: LaunchSupportLevel,
    val schemaVersion: Int,
    val sourceType: TemplateSourceType = TemplateSourceType.OFFICIAL,
    val releaseState: TemplateReleaseState = TemplateReleaseState.READY,
    val notes: LocalizedText? = null,
)
