package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource

private class UnsupportedInstalledExtensionPackageStore : InstalledExtensionPackageStore {
    override val isSupported: Boolean = false

    override fun installPackageBytes(
        sourceName: String,
        packageBytes: ByteArray,
    ): ExtensionPackageSource {
        error("Installed extension package storage is not supported on iOS yet.")
    }

    override fun installPackage(source: ExtensionPackageSource): ExtensionPackageSource {
        error("Installed extension package storage is not supported on iOS yet.")
    }

    override fun uninstallPackage(sourceName: String): Boolean {
        error("Installed extension package storage is not supported on iOS yet.")
    }

    override fun listInstalledPackages(): List<ExtensionPackageSource> = emptyList()
}

@Composable
actual fun rememberInstalledExtensionPackageStore(): InstalledExtensionPackageStore {
    return UnsupportedInstalledExtensionPackageStore()
}
