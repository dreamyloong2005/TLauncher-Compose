package com.dreamyloong.tlauncher.data.library

import com.dreamyloong.tlauncher.core.library.GameLibraryService
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.TemplateDescriptor
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.template.CompatibilityEvaluator
import com.dreamyloong.tlauncher.core.template.TemplateRegistry
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository
import com.dreamyloong.tlauncher.data.persistence.PersistedGameInstance

class PersistentGameLibraryService(
    private val templateRegistry: TemplateRegistry,
    private val compatibilityEvaluator: CompatibilityEvaluator,
    private val stateRepository: LauncherStateRepository,
) : GameLibraryService {
    private fun templates(): List<TemplateDescriptor> = templateRegistry.allTemplates()

    override fun compatibleTemplates(target: PlatformTarget): List<Template> {
        return compatibilityEvaluator.compatibleTemplates(templates(), target)
    }

    override fun instances(): List<GameInstance> {
        return stateRepository.read().library.instances.map(PersistedGameInstance::toDomain)
    }

    override fun currentInstance(): GameInstance? {
        val currentInstanceId = stateRepository.read().library.currentInstanceId ?: return null
        return getInstance(GameInstanceId(currentInstanceId))
    }

    override fun getInstance(instanceId: GameInstanceId): GameInstance? {
        return instances().firstOrNull { it.id == instanceId }
    }

    override fun setCurrentInstance(instanceId: GameInstanceId): Boolean {
        if (instances().none { it.id == instanceId }) {
            return false
        }
        stateRepository.update { state ->
            state.copy(
                library = state.library.copy(currentInstanceId = instanceId.value),
            )
        }
        return true
    }

    override fun createInstance(templatePackageId: ExtensionIdentityId, displayName: String, description: String): GameInstance? {
        if (templates().none { it.packageId == templatePackageId }) {
            return null
        }

        val normalizedName = displayName.trim()
        if (normalizedName.isEmpty()) {
            return null
        }

        val existingInstances = instances()
        val instance = GameInstance(
            id = nextInstanceId(templatePackageId, existingInstances),
            templatePackageId = templatePackageId,
            displayName = normalizedName,
            description = description.trim(),
        )
        stateRepository.update { state ->
            state.copy(
                library = state.library.copy(
                    instances = state.library.instances + PersistedGameInstance.fromDomain(instance),
                    currentInstanceId = state.library.currentInstanceId ?: instance.id.value,
                ),
            )
        }
        return instance
    }

    override fun updateInstanceDetails(
        instanceId: GameInstanceId,
        displayName: String,
        description: String,
    ): Boolean {
        val normalizedName = displayName.trim()
        if (normalizedName.isEmpty()) {
            return false
        }
        val normalizedDescription = description.trim()
        val state = stateRepository.read()
        if (state.library.instances.none { it.id == instanceId.value }) {
            return false
        }
        stateRepository.update { currentState ->
            currentState.copy(
                library = currentState.library.copy(
                    instances = currentState.library.instances.map { instance ->
                        if (instance.id == instanceId.value) {
                            instance.copy(
                                displayName = normalizedName,
                                description = normalizedDescription,
                            )
                        } else {
                            instance
                        }
                    },
                ),
            )
        }
        return true
    }

    override fun deleteInstance(instanceId: GameInstanceId): Boolean {
        val state = stateRepository.read()
        if (state.library.instances.none { it.id == instanceId.value }) {
            return false
        }
        stateRepository.update { currentState ->
            val updatedInstances = currentState.library.instances.filterNot { it.id == instanceId.value }
            val nextCurrentId = if (currentState.library.currentInstanceId == instanceId.value) {
                updatedInstances.firstOrNull()?.id
            } else {
                currentState.library.currentInstanceId
            }
            currentState.copy(
                library = currentState.library.copy(
                    instances = updatedInstances,
                    currentInstanceId = nextCurrentId,
                ),
            )
        }
        return true
    }

    override fun canCreate(templatePackageId: ExtensionIdentityId): Boolean {
        return templates().any { it.packageId == templatePackageId }
    }
}

private fun nextInstanceId(
    templatePackageId: ExtensionIdentityId,
    existingInstances: List<GameInstance>,
): GameInstanceId {
    val prefix = templatePackageId.value.replace(Regex("[^A-Za-z0-9._-]"), "_")
    var index = 1
    while (existingInstances.any { instance -> instance.id.value == "$prefix-$index" }) {
        index += 1
    }
    return GameInstanceId("$prefix-$index")
}
