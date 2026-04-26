package com.dreamyloong.tlauncher.sdk.model

import com.dreamyloong.tlauncher.sdk.extension.DefaultExtensionCompatibilityChecker
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCapability
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCapabilityPolicy
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibility
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibilityChecker
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibilityResult
import com.dreamyloong.tlauncher.sdk.extension.HostGrant
import com.dreamyloong.tlauncher.sdk.extension.HostPermissionKey
import com.dreamyloong.tlauncher.sdk.extension.HostPermissionPolicy
import com.dreamyloong.tlauncher.sdk.extension.HostSdkDescriptor
import com.dreamyloong.tlauncher.sdk.i18n.LocalizedText

enum class ExtensionKind {
    TEMPLATE,
    THEME,
    PLUGIN,
}

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

data class GameInstanceId(val value: String)

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

private val extensionIdSegmentPattern = Regex("^[a-z0-9][a-z0-9_-]*$")

data class ResolvedExtensionIdentity(
    val registrationId: String,
    val identityId: String,
    val kind: ExtensionKind,
    val targetQualifier: PlatformTarget? = null,
)

object ExtensionIdentity {
    fun resolve(
        registrationId: String,
        kind: ExtensionKind,
    ): ResolvedExtensionIdentity {
        val normalizedId = registrationId.trim()
        require(normalizedId.isNotEmpty()) { "Extension registration id must not be blank." }

        val segments = normalizedId.split('.')
        require(segments.size >= 3) {
            "Extension registration id must follow kind.author.name[.target]: $normalizedId"
        }
        require(segments.none(String::isBlank)) {
            "Extension registration id contains blank segments: $normalizedId"
        }
        require(segments.all { segment -> extensionIdSegmentPattern.matches(segment) }) {
            "Extension registration id contains invalid segments: $normalizedId"
        }

        val expectedKindSegment = kind.name.lowercase()
        require(segments.first() == expectedKindSegment) {
            "Extension registration id $normalizedId must start with $expectedKindSegment."
        }

        val targetQualifier = PlatformTarget.entries.firstOrNull { target ->
            target.name.lowercase() == segments.last()
        }
        val identitySegments = if (targetQualifier != null) segments.dropLast(1) else segments
        require(identitySegments.size >= 3) {
            "Extension identity id must contain kind.author.name: $normalizedId"
        }

        return ResolvedExtensionIdentity(
            registrationId = normalizedId,
            identityId = identitySegments.joinToString("."),
            kind = kind,
            targetQualifier = targetQualifier,
        )
    }
}

data class ExtensionManifest(
    val id: String,
    val kind: ExtensionKind,
    val supportedTargets: Set<PlatformTarget>,
    val capabilities: Set<ExtensionCapability> = ExtensionCapabilityPolicy.defaultFor(kind),
    val permissionKeys: Set<HostPermissionKey> = emptySet(),
    val compatibility: ExtensionCompatibility = ExtensionCompatibility(),
) {
    private val resolvedIdentity = ExtensionIdentity.resolve(
        registrationId = id,
        kind = kind,
    )

    val registrationId: String
        get() = resolvedIdentity.registrationId

    val identityId: String
        get() = resolvedIdentity.identityId

    val targetQualifier: PlatformTarget?
        get() = resolvedIdentity.targetQualifier

    fun allowedCapabilities(): Set<ExtensionCapability> {
        return ExtensionCapabilityPolicy.allowedFor(kind)
    }

    fun invalidCapabilities(): Set<ExtensionCapability> {
        return ExtensionCapabilityPolicy.invalidFor(kind, capabilities)
    }

    fun hasValidCapabilityDeclaration(): Boolean {
        return invalidCapabilities().isEmpty()
    }

    fun allowedPermissionKeys(): Set<HostPermissionKey> {
        return HostPermissionPolicy.allowedFor(kind)
    }

    fun invalidPermissionKeys(): Set<HostPermissionKey> {
        return HostPermissionPolicy.invalidFor(kind, permissionKeys)
    }

    fun missingPermissionKeysForCapabilities(): Set<HostPermissionKey> {
        return HostPermissionPolicy.missingFor(
            capabilities = capabilities,
            declared = permissionKeys,
        )
    }

    fun hasValidPermissionDeclaration(): Boolean {
        return invalidPermissionKeys().isEmpty() && missingPermissionKeysForCapabilities().isEmpty()
    }

    fun compatibilityAgainst(
        host: HostSdkDescriptor,
        checker: ExtensionCompatibilityChecker = DefaultExtensionCompatibilityChecker,
    ): ExtensionCompatibilityResult {
        return checker.check(this, host)
    }
}

data class ExtensionDescriptor(
    val extension: ExtensionManifest,
    val displayName: String,
    val enabled: Boolean,
    val compatibility: ExtensionCompatibilityResult,
    val priorityPinnedToBottom: Boolean = false,
    val hostGrants: Set<HostGrant> = emptySet(),
    val userEnabled: Boolean = true,
    val sourceName: String? = null,
    val packageVersion: String? = null,
    val apiVersion: String? = null,
    val packageDescription: String? = null,
    val runtimeLoaded: Boolean = false,
    val runtimeLoadError: String? = null,
)

data class ExtensionPriorityEntry(
    val descriptor: ExtensionDescriptor,
    val priority: Int,
)

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

data class GameInstance(
    val id: GameInstanceId,
    val templatePackageId: ExtensionIdentityId,
    val displayName: String,
    val description: String = "",
    val currentVersion: String? = null,
    val installPath: String? = null,
)
