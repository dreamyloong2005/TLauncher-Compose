package com.dreamyloong.tlauncher.data.plugin

import com.dreamyloong.tlauncher.core.extension.ExtensionStateStore
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository

class PersistentExtensionStateStores(
    private val stateRepository: LauncherStateRepository,
) {
    fun storeFor(sourceId: String): ExtensionStateStore {
        return PersistentExtensionStateStore(
            sourceId = sourceId,
            stateRepository = stateRepository,
        )
    }
}

private class PersistentExtensionStateStore(
    private val sourceId: String,
    private val stateRepository: LauncherStateRepository,
) : ExtensionStateStore {
    override fun read(key: String): String? {
        return stateRepository.readExtensionState(sourceId)[key]
    }

    override fun write(key: String, value: String?) {
        stateRepository.updateExtensionState(sourceId) { values ->
            if (value == null) {
                values - key
            } else {
                values + (key to value)
            }
        }
    }
}
