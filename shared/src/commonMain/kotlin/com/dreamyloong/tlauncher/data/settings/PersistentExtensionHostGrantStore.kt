package com.dreamyloong.tlauncher.data.settings

import com.dreamyloong.tlauncher.core.extension.HostGrant
import com.dreamyloong.tlauncher.core.extension.HostGrantState
import com.dreamyloong.tlauncher.core.extension.HostPermissionKey
import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository
import com.dreamyloong.tlauncher.data.persistence.PersistedExtensionHostGrantState

class PersistentExtensionHostGrantStore(
    private val stateRepository: LauncherStateRepository,
) {
    fun grantsFor(identityId: String): Set<HostGrant> {
        return stateRepository.read().extensionHostGrants
            .firstOrNull { persisted -> persisted.sourceId == identityId }
            ?.toDomain()
            ?.second
            .orEmpty()
    }

    fun syncGrants(
        extensionManifest: ExtensionManifest,
        requestedPermissionKeys: Set<HostPermissionKey>,
        suggestedGrants: Set<HostGrant>,
    ): Set<HostGrant> {
        val nextGrants = mergeRequestedPermissions(
            requestedPermissionKeys = requestedPermissionKeys,
            persistedGrants = grantsFor(extensionManifest.identityId),
            suggestedGrants = suggestedGrants,
        )
        stateRepository.update { state ->
            val nextEntries = state.extensionHostGrants
                .filterNot { persisted -> persisted.sourceId == extensionManifest.identityId }
                .toMutableList()
            if (nextGrants.isNotEmpty()) {
                nextEntries += PersistedExtensionHostGrantState.fromDomain(
                    sourceId = extensionManifest.identityId,
                    grants = nextGrants,
                )
            }
            state.copy(
                extensionHostGrants = nextEntries.sortedBy { persisted -> persisted.sourceId },
            )
        }
        return nextGrants
    }

    fun syncKnownExtensions(identityIds: Collection<String>) {
        val knownIds = identityIds.toSet()
        stateRepository.update { state ->
            state.copy(
                extensionHostGrants = state.extensionHostGrants
                    .filter { persisted -> persisted.sourceId in knownIds }
                    .sortedBy { persisted -> persisted.sourceId },
            )
        }
    }

    fun setGrantDecision(
        extensionManifest: ExtensionManifest,
        requestedPermissionKeys: Set<HostPermissionKey>,
        state: HostGrantState,
        reason: String? = null,
    ): Set<HostGrant> {
        val nextGrants = requestedPermissionKeys.map { permissionKey ->
            HostGrant(
                permissionKey = permissionKey,
                state = state,
                reason = reason,
            )
        }.toSet()
        stateRepository.update { launcherState ->
            val nextEntries = launcherState.extensionHostGrants
                .filterNot { persisted -> persisted.sourceId == extensionManifest.identityId }
                .toMutableList()
            if (nextGrants.isNotEmpty()) {
                nextEntries += PersistedExtensionHostGrantState.fromDomain(
                    sourceId = extensionManifest.identityId,
                    grants = nextGrants,
                )
            }
            launcherState.copy(
                extensionHostGrants = nextEntries.sortedBy { persisted -> persisted.sourceId },
            )
        }
        return nextGrants
    }

    fun setSingleGrantDecision(
        extensionManifest: ExtensionManifest,
        permissionKey: HostPermissionKey,
        state: HostGrantState,
        reason: String? = null,
    ): Set<HostGrant> {
        val requestedPermissionKeys = extensionManifest.permissionKeys
        val nextGrants = requestedPermissionKeys.map { requestedPermissionKey ->
            if (requestedPermissionKey == permissionKey) {
                HostGrant(
                    permissionKey = requestedPermissionKey,
                    state = state,
                    reason = reason,
                )
            } else {
                grantsFor(extensionManifest.identityId).firstOrNull { grant ->
                    grant.permissionKey == requestedPermissionKey
                } ?: HostGrant(
                    permissionKey = requestedPermissionKey,
                    state = HostGrantState.DENIED,
                    reason = "Permission review pending.",
                )
            }
        }.toSet()
        stateRepository.update { launcherState ->
            val nextEntries = launcherState.extensionHostGrants
                .filterNot { persisted -> persisted.sourceId == extensionManifest.identityId }
                .toMutableList()
            if (nextGrants.isNotEmpty()) {
                nextEntries += PersistedExtensionHostGrantState.fromDomain(
                    sourceId = extensionManifest.identityId,
                    grants = nextGrants,
                )
            }
            launcherState.copy(
                extensionHostGrants = nextEntries.sortedBy { persisted -> persisted.sourceId },
            )
        }
        return nextGrants
    }
}

private fun mergeRequestedPermissions(
    requestedPermissionKeys: Set<HostPermissionKey>,
    persistedGrants: Set<HostGrant>,
    suggestedGrants: Set<HostGrant>,
): Set<HostGrant> {
    if (requestedPermissionKeys.isEmpty()) {
        return emptySet()
    }
    val persistedByPermission = persistedGrants.associateBy { grant -> grant.permissionKey }
    val suggestedByPermission = suggestedGrants.associateBy { grant -> grant.permissionKey }
    return requestedPermissionKeys.mapNotNull { permissionKey ->
        persistedByPermission[permissionKey] ?: suggestedByPermission[permissionKey]
    }.toSet()
}
