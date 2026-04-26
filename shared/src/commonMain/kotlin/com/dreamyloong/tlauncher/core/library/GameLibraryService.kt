package com.dreamyloong.tlauncher.core.library

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget

interface GameLibraryService {
    fun compatibleTemplates(target: PlatformTarget): List<Template>
    fun instances(): List<GameInstance>
    fun currentInstance(): GameInstance?
    fun getInstance(instanceId: GameInstanceId): GameInstance?
    fun setCurrentInstance(instanceId: GameInstanceId): Boolean
    fun createInstance(templatePackageId: ExtensionIdentityId, displayName: String, description: String): GameInstance?
    fun updateInstanceDetails(instanceId: GameInstanceId, displayName: String, description: String): Boolean
    fun deleteInstance(instanceId: GameInstanceId): Boolean
    fun canCreate(templatePackageId: ExtensionIdentityId): Boolean
}
