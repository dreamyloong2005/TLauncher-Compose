package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionKind

enum class HostPermissionKey {
    READ_GAME_LIBRARY,
    WRITE_GAME_LIBRARY,
    OPEN_EXTERNAL_URLS,
    NETWORK_ACCESS,
    FILESYSTEM_READ,
    FILESYSTEM_WRITE,
}

enum class HostGrantState {
    GRANTED,
    DENIED,
}

data class HostGrant(
    val permissionKey: HostPermissionKey,
    val state: HostGrantState = HostGrantState.GRANTED,
    val reason: String? = null,
) {
    val isGranted: Boolean
        get() = state == HostGrantState.GRANTED
}

object HostPermissionPolicy {
    private val pluginPermissions = setOf(
        HostPermissionKey.READ_GAME_LIBRARY,
        HostPermissionKey.WRITE_GAME_LIBRARY,
        HostPermissionKey.OPEN_EXTERNAL_URLS,
        HostPermissionKey.NETWORK_ACCESS,
        HostPermissionKey.FILESYSTEM_READ,
        HostPermissionKey.FILESYSTEM_WRITE,
    )

    private val capabilityPermissionRequirements = mapOf(
        ExtensionCapability.READ_GAME_LIBRARY to HostPermissionKey.READ_GAME_LIBRARY,
        ExtensionCapability.WRITE_GAME_LIBRARY to HostPermissionKey.WRITE_GAME_LIBRARY,
        ExtensionCapability.OPEN_EXTERNAL_URLS to HostPermissionKey.OPEN_EXTERNAL_URLS,
        ExtensionCapability.NETWORK_ACCESS to HostPermissionKey.NETWORK_ACCESS,
        ExtensionCapability.FILESYSTEM_READ to HostPermissionKey.FILESYSTEM_READ,
        ExtensionCapability.FILESYSTEM_WRITE to HostPermissionKey.FILESYSTEM_WRITE,
    )

    fun allowedFor(kind: ExtensionKind): Set<HostPermissionKey> {
        return when (kind) {
            ExtensionKind.TEMPLATE,
            ExtensionKind.THEME,
            -> emptySet()

            ExtensionKind.PLUGIN -> pluginPermissions
        }
    }

    fun invalidFor(
        kind: ExtensionKind,
        declared: Set<HostPermissionKey>,
    ): Set<HostPermissionKey> {
        return declared - allowedFor(kind)
    }

    fun requiredFor(capabilities: Set<ExtensionCapability>): Set<HostPermissionKey> {
        return capabilities.mapNotNull(capabilityPermissionRequirements::get).toSet()
    }

    fun missingFor(
        kind: ExtensionKind,
        capabilities: Set<ExtensionCapability>,
        declared: Set<HostPermissionKey>,
    ): Set<HostPermissionKey> {
        return requiredFor(capabilities) - declared
    }
}
