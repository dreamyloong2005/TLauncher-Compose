package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.PlatformTarget

data class LoadedExtensionRuntime(
    val extension: LauncherExtension,
    val hostGrants: Set<HostGrant>,
    val packageResources: ExtensionPackageResources,
)

class ExtensionRuntimeLoadException(
    message: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

expect object ExtensionRuntimeLoader {
    fun load(
        parsedPackage: ParsedExtensionPackage,
        source: ExtensionPackageSource,
        target: PlatformTarget,
    ): LoadedExtensionRuntime
}
