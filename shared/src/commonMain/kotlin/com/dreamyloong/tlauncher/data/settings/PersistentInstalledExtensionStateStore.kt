package com.dreamyloong.tlauncher.data.settings

import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository

class PersistentInstalledExtensionStateStore(
    private val stateRepository: LauncherStateRepository,
) {
    fun isEnabled(identityId: String): Boolean {
        return identityId !in stateRepository.read().disabledExtensionSourceIds
    }

    fun syncKnownExtensions(identityIds: Collection<String>) {
        val knownIds = identityIds.toSet()
        stateRepository.update { state ->
            state.copy(
                disabledExtensionSourceIds = state.disabledExtensionSourceIds
                    .filter { identityId -> identityId in knownIds }
                    .sorted(),
            )
        }
    }

    fun setEnabled(
        identityId: String,
        enabled: Boolean,
    ) {
        stateRepository.update { state ->
            val nextDisabled = state.disabledExtensionSourceIds.toMutableSet()
            if (enabled) {
                nextDisabled.remove(identityId)
            } else {
                nextDisabled.add(identityId)
            }
            state.copy(
                disabledExtensionSourceIds = nextDisabled.sorted(),
            )
        }
    }
}
