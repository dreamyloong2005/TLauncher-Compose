package com.dreamyloong.tlauncher.launch

import android.content.Intent
import android.os.Bundle
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest

internal data class AndroidGameLaunchPayload(
    val gameDisplayName: String,
    val templatePackageId: ExtensionIdentityId,
    val projectDirectory: String,
    val packFileName: String,
    val launchContextJson: String,
    val launchContextFileName: String,
    val nativeLibraryResourceDirectory: String,
    val dynamicJarResourcePaths: List<String>,
    val runtimeBridgeClassName: String?,
    val hostProjectKey: String?,
    val classLoaderBackedNativeLibraryNames: Set<String>,
    val nativeLibraryLoadOrder: List<String>,
    val hostNativeLibraryExcludes: Set<String>,
) {
    fun toIntent(intent: Intent): Intent {
        return intent.apply {
            putExtra(EXTRA_GAME_DISPLAY_NAME, gameDisplayName)
            putExtra(EXTRA_TEMPLATE_PACKAGE_ID, templatePackageId.value)
            putExtra(EXTRA_PROJECT_DIRECTORY, projectDirectory)
            putExtra(EXTRA_PACK_FILE_NAME, packFileName)
            putExtra(EXTRA_LAUNCH_CONTEXT_JSON, launchContextJson)
            putExtra(EXTRA_LAUNCH_CONTEXT_FILE_NAME, launchContextFileName)
            putExtra(EXTRA_NATIVE_LIBRARY_RESOURCE_DIRECTORY, nativeLibraryResourceDirectory)
            putStringArrayListExtra(EXTRA_DYNAMIC_JAR_RESOURCE_PATHS, ArrayList(dynamicJarResourcePaths))
            putExtra(EXTRA_RUNTIME_BRIDGE_CLASS_NAME, runtimeBridgeClassName)
            putExtra(EXTRA_HOST_PROJECT_KEY, hostProjectKey)
            putStringArrayListExtra(
                EXTRA_CLASS_LOADER_BACKED_NATIVE_LIBRARY_NAMES,
                ArrayList(classLoaderBackedNativeLibraryNames),
            )
            putStringArrayListExtra(EXTRA_NATIVE_LIBRARY_LOAD_ORDER, ArrayList(nativeLibraryLoadOrder))
            putStringArrayListExtra(EXTRA_HOST_NATIVE_LIBRARY_EXCLUDES, ArrayList(hostNativeLibraryExcludes))
        }
    }

    fun toFragmentArguments(): Bundle {
        return Bundle().apply {
            putString(EXTRA_GAME_DISPLAY_NAME, gameDisplayName)
            putString(EXTRA_TEMPLATE_PACKAGE_ID, templatePackageId.value)
            putString(EXTRA_PROJECT_DIRECTORY, projectDirectory)
            putString(EXTRA_PACK_FILE_NAME, packFileName)
            putString(EXTRA_LAUNCH_CONTEXT_JSON, launchContextJson)
            putString(EXTRA_LAUNCH_CONTEXT_FILE_NAME, launchContextFileName)
            putString(EXTRA_NATIVE_LIBRARY_RESOURCE_DIRECTORY, nativeLibraryResourceDirectory)
            putStringArrayList(EXTRA_DYNAMIC_JAR_RESOURCE_PATHS, ArrayList(dynamicJarResourcePaths))
            putString(EXTRA_RUNTIME_BRIDGE_CLASS_NAME, runtimeBridgeClassName)
            putString(EXTRA_HOST_PROJECT_KEY, hostProjectKey)
            putStringArrayList(
                EXTRA_CLASS_LOADER_BACKED_NATIVE_LIBRARY_NAMES,
                ArrayList(classLoaderBackedNativeLibraryNames),
            )
            putStringArrayList(EXTRA_NATIVE_LIBRARY_LOAD_ORDER, ArrayList(nativeLibraryLoadOrder))
            putStringArrayList(EXTRA_HOST_NATIVE_LIBRARY_EXCLUDES, ArrayList(hostNativeLibraryExcludes))
        }
    }

    companion object {
        private const val EXTRA_GAME_DISPLAY_NAME = "com.dreamyloong.tlauncher.launch.GAME_DISPLAY_NAME"
        private const val EXTRA_TEMPLATE_PACKAGE_ID = "com.dreamyloong.tlauncher.launch.TEMPLATE_PACKAGE_ID"
        private const val EXTRA_PROJECT_DIRECTORY = "com.dreamyloong.tlauncher.launch.PROJECT_DIRECTORY"
        private const val EXTRA_PACK_FILE_NAME = "com.dreamyloong.tlauncher.launch.PACK_FILE_NAME"
        private const val EXTRA_LAUNCH_CONTEXT_JSON = "com.dreamyloong.tlauncher.launch.LAUNCH_CONTEXT_JSON"
        private const val EXTRA_LAUNCH_CONTEXT_FILE_NAME = "com.dreamyloong.tlauncher.launch.LAUNCH_CONTEXT_FILE_NAME"
        private const val EXTRA_NATIVE_LIBRARY_RESOURCE_DIRECTORY =
            "com.dreamyloong.tlauncher.launch.NATIVE_LIBRARY_RESOURCE_DIRECTORY"
        private const val EXTRA_DYNAMIC_JAR_RESOURCE_PATHS =
            "com.dreamyloong.tlauncher.launch.DYNAMIC_JAR_RESOURCE_PATHS"
        private const val EXTRA_RUNTIME_BRIDGE_CLASS_NAME =
            "com.dreamyloong.tlauncher.launch.RUNTIME_BRIDGE_CLASS_NAME"
        private const val EXTRA_HOST_PROJECT_KEY =
            "com.dreamyloong.tlauncher.launch.HOST_PROJECT_KEY"
        private const val EXTRA_CLASS_LOADER_BACKED_NATIVE_LIBRARY_NAMES =
            "com.dreamyloong.tlauncher.launch.CLASS_LOADER_BACKED_NATIVE_LIBRARY_NAMES"
        private const val EXTRA_NATIVE_LIBRARY_LOAD_ORDER =
            "com.dreamyloong.tlauncher.launch.NATIVE_LIBRARY_LOAD_ORDER"
        private const val EXTRA_HOST_NATIVE_LIBRARY_EXCLUDES =
            "com.dreamyloong.tlauncher.launch.HOST_NATIVE_LIBRARY_EXCLUDES"

        fun fromRequest(request: GameLaunchRequest.AndroidRuntime): AndroidGameLaunchPayload {
            return AndroidGameLaunchPayload(
                gameDisplayName = request.gameDisplayName,
                templatePackageId = request.templatePackageId,
                projectDirectory = request.projectDirectory,
                packFileName = request.packFileName,
                launchContextJson = request.launchContextJson,
                launchContextFileName = request.launchContextFileName,
                nativeLibraryResourceDirectory = request.nativeLibraryResourceDirectory,
                dynamicJarResourcePaths = request.dynamicJarResourcePaths,
                runtimeBridgeClassName = request.runtimeBridgeClassName,
                hostProjectKey = request.hostProjectKey,
                classLoaderBackedNativeLibraryNames = request.classLoaderBackedNativeLibraryNames,
                nativeLibraryLoadOrder = request.nativeLibraryLoadOrder,
                hostNativeLibraryExcludes = request.hostNativeLibraryExcludes,
            )
        }

        fun fromIntent(intent: Intent): AndroidGameLaunchPayload {
            return fromArguments(intent.extras ?: Bundle.EMPTY)
        }

        fun fromArguments(arguments: Bundle): AndroidGameLaunchPayload {
            return AndroidGameLaunchPayload(
                gameDisplayName = arguments.getString(EXTRA_GAME_DISPLAY_NAME).orEmpty(),
                templatePackageId = ExtensionIdentityId(arguments.getString(EXTRA_TEMPLATE_PACKAGE_ID).orEmpty()),
                projectDirectory = arguments.getString(EXTRA_PROJECT_DIRECTORY).orEmpty(),
                packFileName = arguments.getString(EXTRA_PACK_FILE_NAME).orEmpty(),
                launchContextJson = arguments.getString(EXTRA_LAUNCH_CONTEXT_JSON).orEmpty(),
                launchContextFileName = arguments.getString(EXTRA_LAUNCH_CONTEXT_FILE_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: "tlauncher-launch-context.json",
                nativeLibraryResourceDirectory = arguments.getString(EXTRA_NATIVE_LIBRARY_RESOURCE_DIRECTORY).orEmpty(),
                dynamicJarResourcePaths = arguments.getStringArrayList(EXTRA_DYNAMIC_JAR_RESOURCE_PATHS).orEmpty(),
                runtimeBridgeClassName = arguments.getString(EXTRA_RUNTIME_BRIDGE_CLASS_NAME)
                    ?.takeIf { it.isNotBlank() },
                hostProjectKey = arguments.getString(EXTRA_HOST_PROJECT_KEY)
                    ?.takeIf { it.isNotBlank() },
                classLoaderBackedNativeLibraryNames = arguments
                    .getStringArrayList(EXTRA_CLASS_LOADER_BACKED_NATIVE_LIBRARY_NAMES)
                    .orEmpty()
                    .toSet(),
                nativeLibraryLoadOrder = arguments.getStringArrayList(EXTRA_NATIVE_LIBRARY_LOAD_ORDER).orEmpty(),
                hostNativeLibraryExcludes = arguments.getStringArrayList(EXTRA_HOST_NATIVE_LIBRARY_EXCLUDES)
                    .orEmpty()
                    .toSet(),
            )
        }
    }
}
