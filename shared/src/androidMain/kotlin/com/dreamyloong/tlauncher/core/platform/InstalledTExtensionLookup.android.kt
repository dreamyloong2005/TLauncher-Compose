package com.dreamyloong.tlauncher.core.platform

import com.dreamyloong.tlauncher.core.extension.DefaultExtensionPackageParser
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageResources
import com.dreamyloong.tlauncher.core.extension.PackageBackedExtensionResources
import com.dreamyloong.tlauncher.core.extension.TExtensionPackageFormat
import com.dreamyloong.tlauncher.core.extension.ZipExtensionPackageSource
import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import java.io.File

data class InstalledTExtensionPackageLookup(
    val source: ZipExtensionPackageSource,
    val manifest: ExtensionManifest,
)

fun findInstalledTExtensionPackage(
    identityId: String,
    target: PlatformTarget,
): InstalledTExtensionPackageLookup? {
    val packageDirectory = File(launcherStorageDirectoryPath(), "extensions/packages")
    if (!packageDirectory.isDirectory) {
        return null
    }
    val parser = DefaultExtensionPackageParser
    val candidates = packageDirectory.listFiles()
        .orEmpty()
        .filter { file -> file.isFile && TExtensionPackageFormat.isTExtensionFileName(file.name) }
        .mapNotNull { file ->
            val source = ZipExtensionPackageSource(file)
            val manifest = runCatching { parser.parse(source).manifest.extension }.getOrNull()
                ?: return@mapNotNull null
            if (manifest.identityId != identityId) {
                return@mapNotNull null
            }
            InstalledTExtensionPackageLookup(
                source = source,
                manifest = manifest,
            )
        }
    return candidates.maxWithOrNull(
        compareBy<InstalledTExtensionPackageLookup>(
            { extensionTargetSelectionScore(target, it.manifest) },
            { it.source.sourceName.lowercase() },
        ),
    )
}

fun installedTExtensionResources(
    identityId: String,
    target: PlatformTarget,
): ExtensionPackageResources? {
    return findInstalledTExtensionPackage(identityId, target)
        ?.let { lookup -> PackageBackedExtensionResources(lookup.source) }
}

private fun extensionTargetSelectionScore(
    target: PlatformTarget,
    extension: ExtensionManifest,
): Int {
    return when {
        extension.targetQualifier == target -> 4
        extension.targetQualifier == null && target in extension.supportedTargets -> 3
        target in extension.supportedTargets -> 2
        extension.targetQualifier == null -> 1
        else -> 0
    }
}
