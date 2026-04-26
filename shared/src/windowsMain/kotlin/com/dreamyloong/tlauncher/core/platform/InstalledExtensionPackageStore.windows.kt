package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource
import com.dreamyloong.tlauncher.core.extension.TExtensionPackageFormat
import com.dreamyloong.tlauncher.core.extension.ZipExtensionPackageSource
import java.io.File

private class WindowsInstalledExtensionPackageStore : InstalledExtensionPackageStore {
    private val packageDirectory: File
        get() = File(launcherStorageDirectoryPath(), "extensions/packages")

    override val isSupported: Boolean = true

    override fun installPackageBytes(
        sourceName: String,
        packageBytes: ByteArray,
    ): ExtensionPackageSource {
        packageDirectory.mkdirs()
        val destination = File(packageDirectory, sanitizePackageName(sourceName))
        destination.writeBytes(packageBytes)
        return ZipExtensionPackageSource(destination)
    }

    override fun installPackage(source: ExtensionPackageSource): ExtensionPackageSource {
        require(source is ZipExtensionPackageSource) {
            "Windows installed package store only supports .textension zip sources."
        }
        return installPackageBytes(
            sourceName = source.sourceName,
            packageBytes = source.file.readBytes(),
        )
    }

    override fun uninstallPackage(sourceName: String): Boolean {
        val targetFile = File(packageDirectory, sanitizePackageName(sourceName))
        return targetFile.isFile && targetFile.delete()
    }

    override fun listInstalledPackages(): List<ExtensionPackageSource> {
        val directory = packageDirectory
        if (!directory.isDirectory) {
            return emptyList()
        }
        return directory.listFiles()
            .orEmpty()
            .filter { file -> file.isFile && TExtensionPackageFormat.isTExtensionFileName(file.name) }
            .sortedBy { file -> file.name.lowercase() }
            .map(::ZipExtensionPackageSource)
    }

    private fun sanitizePackageName(sourceName: String): String {
        val normalized = sourceName.trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "extension${TExtensionPackageFormat.FILE_EXTENSION}" }
        return if (TExtensionPackageFormat.isTExtensionFileName(normalized)) {
            normalized
        } else {
            normalized + TExtensionPackageFormat.FILE_EXTENSION
        }
    }
}

@Composable
actual fun rememberInstalledExtensionPackageStore(): InstalledExtensionPackageStore {
    return WindowsInstalledExtensionPackageStore()
}
