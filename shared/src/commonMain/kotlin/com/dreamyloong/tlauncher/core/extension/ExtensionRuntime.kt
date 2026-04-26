package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices

interface ExtensionContext {
    val apiVersion: String
    val hostGrants: Set<HostGrant>
    val packageResources: ExtensionPackageResources
    val stateStore: ExtensionStateStore
    val hostPaths: ExtensionHostPaths
    val hostServices: ExtensionHostServices

    fun withHostGrants(grants: Set<HostGrant>): ExtensionContext

    fun withPackageResources(resources: ExtensionPackageResources): ExtensionContext

    fun withStateStore(store: ExtensionStateStore): ExtensionContext

    fun withHostPaths(paths: ExtensionHostPaths): ExtensionContext

    fun withHostServices(services: ExtensionHostServices): ExtensionContext
}

interface ExtensionFeature

data class ExtensionHostPaths(
    val appFilesDirectoryPath: String? = null,
    val launcherStorageDirectoryPath: String? = null,
    val packageName: String? = null,
)

interface ExtensionStateStore {
    fun read(key: String): String?

    fun write(key: String, value: String?)
}

object EmptyExtensionStateStore : ExtensionStateStore {
    override fun read(key: String): String? = null

    override fun write(key: String, value: String?) = Unit
}

interface ExtensionPackageResources {
    fun exists(path: String): Boolean

    fun list(path: String = ""): List<String>

    fun readUtf8(path: String): String?

    fun readBytes(path: String): ByteArray?
}

object EmptyExtensionPackageResources : ExtensionPackageResources {
    override fun exists(path: String): Boolean = false

    override fun list(path: String): List<String> = emptyList()

    override fun readUtf8(path: String): String? = null

    override fun readBytes(path: String): ByteArray? = null
}

interface LauncherExtension {
    val extension: ExtensionManifest
    val displayName: String
    val version: String
    val apiVersion: String
    val entrypoint: String
    val hostGrants: Set<HostGrant>
        get() = emptySet()
    val packageResources: ExtensionPackageResources
        get() = EmptyExtensionPackageResources

    fun createFeatures(context: ExtensionContext): List<ExtensionFeature>
}

data class RegisteredExtensionFeature(
    val owner: LauncherExtension,
    val feature: ExtensionFeature,
)

interface ExtensionRegistry {
    fun installedExtensions(): List<LauncherExtension>
    fun registeredFeatures(): List<RegisteredExtensionFeature>
    fun extensionFailureSnapshot(): Map<String, String> = emptyMap()
    fun reportExtensionFailure(
        owner: LauncherExtension,
        stage: String,
        throwable: Throwable,
    ) = Unit
    fun clearExtensionFailure(
        owner: LauncherExtension,
        stage: String,
    ) = Unit

    fun features(): List<ExtensionFeature> {
        return registeredFeatures().map { it.feature }
    }
}
