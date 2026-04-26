package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource

interface InstalledExtensionPackageStore {
    val isSupported: Boolean

    fun installPackageBytes(
        sourceName: String,
        packageBytes: ByteArray,
    ): ExtensionPackageSource

    fun installPackage(source: ExtensionPackageSource): ExtensionPackageSource

    fun uninstallPackage(sourceName: String): Boolean

    fun listInstalledPackages(): List<ExtensionPackageSource>
}

@Composable
expect fun rememberInstalledExtensionPackageStore(): InstalledExtensionPackageStore
