package com.dreamyloong.tlauncher.sdk.platform

import com.dreamyloong.tlauncher.sdk.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.sdk.model.GameInstance
import com.dreamyloong.tlauncher.sdk.model.GameInstanceId
import com.dreamyloong.tlauncher.sdk.model.PlatformTarget
import com.dreamyloong.tlauncher.sdk.model.Template

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

data class GameLaunchContext(
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val currentTemplate: Template?,
)

data class DirectoryPickerState(
    val isSupported: Boolean,
    val pickDirectory: (initialPath: String?, onPicked: (String?) -> Unit) -> Unit,
)

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
) {
    private val fingerprint = bytes.contentHashCode()

    override fun equals(other: Any?): Boolean {
        return other is PickedFile &&
            name == other.name &&
            fingerprint == other.fingerprint &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + fingerprint
    }
}

data class FilePickerState(
    val isSupported: Boolean,
    val pickFile: (acceptedMimeTypes: List<String>, onPicked: (PickedFile?) -> Unit) -> Unit,
)

data class ManageStorageAccessState(
    val isSupported: Boolean,
    val isGranted: Boolean,
    val requestAccess: () -> Unit,
)

sealed interface LauncherIconSource {
    data object Default : LauncherIconSource

    data object Night : LauncherIconSource

    class Asset(
        val path: String,
        val bytes: ByteArray,
    ) : LauncherIconSource {
        private val fingerprint = bytes.contentHashCode()

        override fun equals(other: Any?): Boolean {
            return other is Asset &&
                path == other.path &&
                fingerprint == other.fingerprint &&
                bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return 31 * path.hashCode() + fingerprint
        }

        override fun toString(): String {
            return "LauncherIconSource.Asset(path=$path, bytes=${bytes.size})"
        }
    }
}

object LauncherIconController {
    fun setIcon(icon: LauncherIconSource) = Unit

    fun setIconAndCloseLauncher(icon: LauncherIconSource) = Unit

    fun resetIcon() = Unit
}

interface AndroidExtensionHostController {
    fun requestCloseHostedRuntime()

    fun closeHostedRuntimeAndReturnToLauncher()

    fun restartHostedRuntime()

    fun restartLauncherFromHostedRuntime()
}
