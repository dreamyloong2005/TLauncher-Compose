package com.dreamyloong.tlauncher.core.template

import com.dreamyloong.tlauncher.core.i18n.LocalizedText
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.TemplateDescriptor
import com.dreamyloong.tlauncher.core.model.TemplateTargetFacet
import com.dreamyloong.tlauncher.core.model.LaunchSupportLevel
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.model.RuntimeRequirement
import com.dreamyloong.tlauncher.core.model.TemplateReleaseState
import com.dreamyloong.tlauncher.core.model.TemplateSourceType

data class TemplatePlatformSupport(
    val target: PlatformTarget,
    val supportLevel: LaunchSupportLevel,
)

data class TemplatePlatformFacet(
    val target: PlatformTarget,
    val supportLevel: LaunchSupportLevel,
    val runtimeRequirements: List<RuntimeRequirement> = emptyList(),
    val capabilityKeys: Set<String> = emptySet(),
    val capabilityLabels: Map<String, LocalizedText> = emptyMap(),
    val notes: LocalizedText? = null,
)

data class TemplatePackage(
    val extension: ExtensionManifest,
    val schemaVersion: Int,
    val name: LocalizedText,
    val description: LocalizedText,
    val defaultInstanceDescription: LocalizedText? = null,
    val sourceType: TemplateSourceType,
    val releaseState: TemplateReleaseState,
    val notes: LocalizedText? = null,
    val platforms: List<TemplatePlatformFacet>,
    val devPath: String? = null,
) {
    init {
        require(platforms.isNotEmpty()) { "TemplatePackage platforms must not be empty." }
        require(platforms.map { facet -> facet.target }.toSet() == extension.supportedTargets) {
            "TemplatePackage platforms must match extension.supportedTargets."
        }
    }

    val supportedTargets: Set<PlatformTarget>
        get() = platforms.map { facet -> facet.target }.toSet()

    val runtimeRequirements: List<RuntimeRequirement>
        get() = platforms.flatMap { facet -> facet.runtimeRequirements }.distinct()

    val capabilityKeys: Set<String>
        get() = platforms.flatMap { facet -> facet.capabilityKeys }.toSet()

    val supportedPlatforms: List<TemplatePlatformSupport>
        get() = platforms.map { facet ->
            TemplatePlatformSupport(
                target = facet.target,
                supportLevel = facet.supportLevel,
            )
        }

    fun facetFor(target: PlatformTarget): TemplatePlatformFacet? {
        return platforms.firstOrNull { facet -> facet.target == target }
    }
}

interface TemplatePackageRegistry {
    fun packages(): List<TemplatePackage>
}

fun TemplatePackage.toTemplateDescriptor(): TemplateDescriptor {
    return TemplateDescriptor(
        packageId = ExtensionIdentityId(extension.identityId),
        name = name,
        description = description,
        defaultInstanceDescription = defaultInstanceDescription,
        schemaVersion = schemaVersion,
        sourceType = sourceType,
        releaseState = releaseState,
        notes = notes,
        platforms = platforms.map { facet ->
            TemplateTargetFacet(
                target = facet.target,
                supportLevel = facet.supportLevel,
                runtimeRequirements = facet.runtimeRequirements,
                capabilityKeys = facet.capabilityKeys,
                capabilityLabels = facet.capabilityLabels,
                notes = facet.notes,
            )
        },
    )
}

fun TemplatePackage.toTemplate(target: PlatformTarget): Template? {
    return toTemplateDescriptor().resolve(target)
}
