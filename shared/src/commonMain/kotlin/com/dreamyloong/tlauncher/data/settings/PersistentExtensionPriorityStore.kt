package com.dreamyloong.tlauncher.data.settings

import com.dreamyloong.tlauncher.core.model.ExtensionDescriptor
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityStore
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository

class PersistentExtensionPriorityStore(
    private val stateRepository: LauncherStateRepository,
) : ExtensionPriorityStore {
    override fun prioritizedEntries(extensions: List<ExtensionDescriptor>): List<ExtensionPriorityEntry> {
        val persistedOrder = stateRepository.read().extensionPrioritySourceIds
        val syncedOrder = syncManualOrder(persistedOrder, extensions)
        if (syncedOrder != persistedOrder) {
            stateRepository.update { state ->
                state.copy(extensionPrioritySourceIds = syncedOrder)
            }
        }
        return prioritizedEntriesFromOrder(syncedOrder, extensions)
    }

    override fun increasePriority(
        identityId: String,
        extensions: List<ExtensionDescriptor>,
    ) {
        stateRepository.update { state ->
            val order = syncManualOrder(state.extensionPrioritySourceIds, extensions).toMutableList()
            val extensionLookup = extensions.associateBy { it.extension.identityId }
            val index = order.indexOf(identityId)
            if (
                index > 0 &&
                extensionLookup[identityId]?.priorityPinnedToBottom != true &&
                extensionLookup[order[index - 1]]?.priorityPinnedToBottom != true
            ) {
                val current = order[index]
                order[index] = order[index - 1]
                order[index - 1] = current
            }
            state.copy(extensionPrioritySourceIds = order)
        }
    }

    override fun decreasePriority(
        identityId: String,
        extensions: List<ExtensionDescriptor>,
    ) {
        stateRepository.update { state ->
            val order = syncManualOrder(state.extensionPrioritySourceIds, extensions).toMutableList()
            val extensionLookup = extensions.associateBy { it.extension.identityId }
            val index = order.indexOf(identityId)
            val movableLastIndex = order.indexOfLast { id -> extensionLookup[id]?.priorityPinnedToBottom != true }
            if (
                index >= 0 &&
                index < movableLastIndex &&
                extensionLookup[identityId]?.priorityPinnedToBottom != true &&
                extensionLookup[order[index + 1]]?.priorityPinnedToBottom != true
            ) {
                val current = order[index]
                order[index] = order[index + 1]
                order[index + 1] = current
            }
            state.copy(extensionPrioritySourceIds = order)
        }
    }
}

private fun prioritizedEntriesFromOrder(
    order: List<String>,
    extensions: List<ExtensionDescriptor>,
): List<ExtensionPriorityEntry> {
    return order.mapNotNull { identityId ->
        extensions.firstOrNull { it.extension.identityId == identityId }
    }.mapIndexed { index, descriptor ->
        ExtensionPriorityEntry(
            descriptor = descriptor,
            priority = index,
        )
    }
}

private fun syncManualOrder(
    manualOrder: List<String>,
    extensions: List<ExtensionDescriptor>,
): List<String> {
    val identityIds = extensions.map { it.extension.identityId }.distinct()
    val extensionLookup = extensions.associateBy { it.extension.identityId }
    val normalizedOrder = manualOrder.filter { it in identityIds }.toMutableList()
    identityIds.forEach { identityId ->
        if (identityId !in normalizedOrder) {
            normalizedOrder += identityId
        }
    }
    val movableIds = normalizedOrder.filter { id -> extensionLookup[id]?.priorityPinnedToBottom != true }
    val pinnedIds = normalizedOrder.filter { id -> extensionLookup[id]?.priorityPinnedToBottom == true }
    return movableIds + pinnedIds
}
