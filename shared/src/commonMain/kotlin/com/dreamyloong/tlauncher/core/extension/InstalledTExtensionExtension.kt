package com.dreamyloong.tlauncher.core.extension

data class InstalledTExtensionExtension(
    val packageManifest: ExtensionPackageManifest,
    val sourceName: String,
    val runtimeExtension: LauncherExtension,
    override val hostGrants: Set<HostGrant> = emptySet(),
    override val packageResources: ExtensionPackageResources = EmptyExtensionPackageResources,
    private val runtimeHandle: Any? = null,
) : LauncherExtension {
    override val extension = packageManifest.extension
    override val displayName: String = packageManifest.displayName
    override val version: String = packageManifest.version
    override val apiVersion: String = packageManifest.apiVersion
    override val entrypoint: String = packageManifest.entrypoints.entries.firstOrNull()?.value.orEmpty()

    override fun createFeatures(context: ExtensionContext): List<ExtensionFeature> {
        return runtimeExtension.createFeatures(context)
    }
}
