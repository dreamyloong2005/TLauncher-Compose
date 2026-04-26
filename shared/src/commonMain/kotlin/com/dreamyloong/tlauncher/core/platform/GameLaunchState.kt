package com.dreamyloong.tlauncher.core.platform

import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.GameInstanceId

data class GameLaunchState(
    val isSupported: Boolean,
    val syncLaunchContext: (GameLaunchRequest?) -> Unit = {},
    val launch: (GameLaunchRequest) -> Unit,
)

sealed interface GameLaunchRequest {
    data class AndroidRuntime(
        val gameInstanceId: GameInstanceId,
        val gameDisplayName: String,
        val templatePackageId: ExtensionIdentityId,
        val projectDirectory: String,
        val packFileName: String,
        val launchContextJson: String,
        val launchContextFileName: String = "tlauncher-launch-context.json",
        val nativeLibraryResourceDirectory: String,
        val dynamicJarResourcePaths: List<String> = emptyList(),
        val runtimeBridgeClassName: String? = null,
        val hostProjectKey: String? = null,
        val classLoaderBackedNativeLibraryNames: Set<String> = emptySet(),
        val nativeLibraryLoadOrder: List<String> = emptyList(),
        val hostNativeLibraryExcludes: Set<String> = emptySet(),
    ) : GameLaunchRequest
}
