package com.dreamyloong.tlauncher.core.model

import com.dreamyloong.tlauncher.core.extension.ExtensionCapability
import com.dreamyloong.tlauncher.core.extension.ExtensionCapabilityPolicy
import com.dreamyloong.tlauncher.core.extension.ExtensionCompatibility
import com.dreamyloong.tlauncher.core.extension.ExtensionCompatibilityChecker
import com.dreamyloong.tlauncher.core.extension.ExtensionCompatibilityResult
import com.dreamyloong.tlauncher.core.extension.DefaultExtensionCompatibilityChecker
import com.dreamyloong.tlauncher.core.extension.HostSdkDescriptor
import com.dreamyloong.tlauncher.core.extension.HostGrant
import com.dreamyloong.tlauncher.core.extension.HostPermissionKey
import com.dreamyloong.tlauncher.core.extension.HostPermissionPolicy

enum class ExtensionKind {
    TEMPLATE,
    THEME,
    PLUGIN,
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
            kind = kind,
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

data class ExtensionPermissionReview(
    val extension: ExtensionManifest,
    val displayName: String,
    val sourceName: String,
    val currentGrants: Set<HostGrant> = emptySet(),
)

data class ExtensionPackageScanProblem(
    val sourceName: String,
    val message: String,
)

interface ExtensionPriorityStore {
    fun prioritizedEntries(extensions: List<ExtensionDescriptor>): List<ExtensionPriorityEntry>
    fun increasePriority(identityId: String, extensions: List<ExtensionDescriptor>)
    fun decreasePriority(identityId: String, extensions: List<ExtensionDescriptor>)
}
