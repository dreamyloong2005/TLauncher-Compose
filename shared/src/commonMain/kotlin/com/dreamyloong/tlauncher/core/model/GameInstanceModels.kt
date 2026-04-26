package com.dreamyloong.tlauncher.core.model

data class GameInstanceId(val value: String)

data class GameInstance(
    val id: GameInstanceId,
    val templatePackageId: ExtensionIdentityId,
    val displayName: String,
    val description: String = "",
    val currentVersion: String? = null,
    val installPath: String? = null,
)
