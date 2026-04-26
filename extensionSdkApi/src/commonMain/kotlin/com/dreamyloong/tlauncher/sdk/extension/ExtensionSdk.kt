package com.dreamyloong.tlauncher.sdk.extension

import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices
import com.dreamyloong.tlauncher.sdk.model.ExtensionKind
import com.dreamyloong.tlauncher.sdk.model.ExtensionManifest

enum class ExtensionCapability {
    DEFINE_TEMPLATE_METADATA,
    DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS,
    PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS,
    PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS,

    DEFINE_THEME_METADATA,
    DEFINE_THEME_TOKENS,
    DEFINE_THEME_SCENE,

    PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
    PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
    MUTATE_PAGE_TREE,
    INTERCEPT_ACTIONS,
    INTERCEPT_GAME_LAUNCH,
    INTERCEPT_TEMPLATE_LAUNCH_PREPARATION,
    READ_GAME_LIBRARY,
    WRITE_GAME_LIBRARY,
    OPEN_EXTERNAL_URLS,
    NETWORK_ACCESS,
    FILESYSTEM_READ,
    FILESYSTEM_WRITE,
}

object ExtensionCapabilityPolicy {
    private val templateCapabilities = setOf(
        ExtensionCapability.DEFINE_TEMPLATE_METADATA,
        ExtensionCapability.DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS,
        ExtensionCapability.PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS,
        ExtensionCapability.PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS,
    )

    private val themeCapabilities = setOf(
        ExtensionCapability.DEFINE_THEME_METADATA,
        ExtensionCapability.DEFINE_THEME_TOKENS,
        ExtensionCapability.DEFINE_THEME_SCENE,
    )

    private val pluginCapabilities = setOf(
        ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
        ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
        ExtensionCapability.MUTATE_PAGE_TREE,
        ExtensionCapability.INTERCEPT_ACTIONS,
        ExtensionCapability.INTERCEPT_GAME_LAUNCH,
        ExtensionCapability.INTERCEPT_TEMPLATE_LAUNCH_PREPARATION,
        ExtensionCapability.READ_GAME_LIBRARY,
        ExtensionCapability.WRITE_GAME_LIBRARY,
        ExtensionCapability.OPEN_EXTERNAL_URLS,
        ExtensionCapability.NETWORK_ACCESS,
        ExtensionCapability.FILESYSTEM_READ,
        ExtensionCapability.FILESYSTEM_WRITE,
    )

    fun allowedFor(kind: ExtensionKind): Set<ExtensionCapability> {
        return when (kind) {
            ExtensionKind.TEMPLATE -> templateCapabilities
            ExtensionKind.THEME -> themeCapabilities
            ExtensionKind.PLUGIN -> pluginCapabilities
        }
    }

    fun defaultFor(kind: ExtensionKind): Set<ExtensionCapability> {
        return when (kind) {
            ExtensionKind.TEMPLATE -> templateCapabilities
            ExtensionKind.THEME -> themeCapabilities
            ExtensionKind.PLUGIN -> setOf(
                ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
                ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
            )
        }
    }

    fun invalidFor(
        kind: ExtensionKind,
        declared: Set<ExtensionCapability>,
    ): Set<ExtensionCapability> {
        return declared - allowedFor(kind)
    }
}

enum class HostPermissionKey {
    READ_GAME_LIBRARY,
    WRITE_GAME_LIBRARY,
    OPEN_EXTERNAL_URLS,
    NETWORK_ACCESS,
    FILESYSTEM_READ,
    FILESYSTEM_WRITE,
}

enum class HostGrantState {
    GRANTED,
    DENIED,
}

data class HostGrant(
    val permissionKey: HostPermissionKey,
    val state: HostGrantState = HostGrantState.GRANTED,
    val reason: String? = null,
) {
    val isGranted: Boolean
        get() = state == HostGrantState.GRANTED
}

object HostPermissionPolicy {
    private val pluginPermissions = setOf(
        HostPermissionKey.READ_GAME_LIBRARY,
        HostPermissionKey.WRITE_GAME_LIBRARY,
        HostPermissionKey.OPEN_EXTERNAL_URLS,
        HostPermissionKey.NETWORK_ACCESS,
        HostPermissionKey.FILESYSTEM_READ,
        HostPermissionKey.FILESYSTEM_WRITE,
    )

    private val capabilityPermissionRequirements = mapOf(
        ExtensionCapability.READ_GAME_LIBRARY to HostPermissionKey.READ_GAME_LIBRARY,
        ExtensionCapability.WRITE_GAME_LIBRARY to HostPermissionKey.WRITE_GAME_LIBRARY,
        ExtensionCapability.OPEN_EXTERNAL_URLS to HostPermissionKey.OPEN_EXTERNAL_URLS,
        ExtensionCapability.NETWORK_ACCESS to HostPermissionKey.NETWORK_ACCESS,
        ExtensionCapability.FILESYSTEM_READ to HostPermissionKey.FILESYSTEM_READ,
        ExtensionCapability.FILESYSTEM_WRITE to HostPermissionKey.FILESYSTEM_WRITE,
    )

    fun allowedFor(kind: ExtensionKind): Set<HostPermissionKey> {
        return when (kind) {
            ExtensionKind.TEMPLATE,
            ExtensionKind.THEME,
            -> emptySet()

            ExtensionKind.PLUGIN -> pluginPermissions
        }
    }

    fun invalidFor(
        kind: ExtensionKind,
        declared: Set<HostPermissionKey>,
    ): Set<HostPermissionKey> {
        return declared - allowedFor(kind)
    }

    fun requiredFor(capabilities: Set<ExtensionCapability>): Set<HostPermissionKey> {
        return capabilities.mapNotNull(capabilityPermissionRequirements::get).toSet()
    }

    fun missingFor(
        capabilities: Set<ExtensionCapability>,
        declared: Set<HostPermissionKey>,
    ): Set<HostPermissionKey> {
        return requiredFor(capabilities) - declared
    }
}

data class ExtensionCompatibility(
    val packageFormatVersion: Int = ExtensionSdkContract.PACKAGE_FORMAT_VERSION,
    val sdkApiVersion: Int = ExtensionSdkContract.SDK_API_VERSION,
    val minSdkApiVersion: Int = sdkApiVersion,
    val targetSdkApiVersion: Int = sdkApiVersion,
)

data class HostSdkDescriptor(
    val packageFormatVersion: Int,
    val sdkApiVersion: Int,
    val minSupportedSdkApiVersion: Int = ExtensionSdkContract.MIN_SUPPORTED_SDK_API_VERSION,
)

object ExtensionSdkContract {
    const val PACKAGE_FORMAT_VERSION: Int = 1
    const val MIN_SUPPORTED_SDK_API_VERSION: Int = 1
    const val SDK_API_VERSION: Int = 1
    const val SDK_VERSION: String = "1.0.0"

    fun hostDescriptor(): HostSdkDescriptor {
        return HostSdkDescriptor(
            packageFormatVersion = PACKAGE_FORMAT_VERSION,
            sdkApiVersion = SDK_API_VERSION,
            minSupportedSdkApiVersion = MIN_SUPPORTED_SDK_API_VERSION,
        )
    }
}

enum class ExtensionCompatibilityIssue {
    PACKAGE_FORMAT_VERSION_MISMATCH,
    SDK_API_VERSION_MISMATCH,
    SDK_API_VERSION_RANGE_INVALID,
    MIN_SDK_API_VERSION_TOO_NEW,
    TARGET_SDK_API_VERSION_TOO_OLD,
}

sealed interface ExtensionCompatibilityResult {
    val issues: List<ExtensionCompatibilityIssue>
    val isCompatible: Boolean

    data class Compatible(
        override val issues: List<ExtensionCompatibilityIssue> = emptyList(),
    ) : ExtensionCompatibilityResult {
        override val isCompatible: Boolean = true
    }

    data class Incompatible(
        override val issues: List<ExtensionCompatibilityIssue>,
    ) : ExtensionCompatibilityResult {
        override val isCompatible: Boolean = false
    }
}

interface ExtensionCompatibilityChecker {
    fun check(
        manifest: ExtensionManifest,
        host: HostSdkDescriptor,
    ): ExtensionCompatibilityResult
}

object DefaultExtensionCompatibilityChecker : ExtensionCompatibilityChecker {
    override fun check(
        manifest: ExtensionManifest,
        host: HostSdkDescriptor,
    ): ExtensionCompatibilityResult {
        val compatibility = manifest.compatibility
        val issues = buildList {
            if (compatibility.packageFormatVersion != host.packageFormatVersion) {
                add(ExtensionCompatibilityIssue.PACKAGE_FORMAT_VERSION_MISMATCH)
            }
            if (
                compatibility.sdkApiVersion < 1 ||
                compatibility.minSdkApiVersion < 1 ||
                compatibility.targetSdkApiVersion < 1 ||
                compatibility.minSdkApiVersion > compatibility.targetSdkApiVersion
            ) {
                add(ExtensionCompatibilityIssue.SDK_API_VERSION_RANGE_INVALID)
            }
            if (compatibility.minSdkApiVersion > host.sdkApiVersion) {
                add(ExtensionCompatibilityIssue.MIN_SDK_API_VERSION_TOO_NEW)
            }
            if (compatibility.targetSdkApiVersion < host.minSupportedSdkApiVersion) {
                add(ExtensionCompatibilityIssue.TARGET_SDK_API_VERSION_TOO_OLD)
            }
        }
        return if (issues.isEmpty()) {
            ExtensionCompatibilityResult.Compatible()
        } else {
            ExtensionCompatibilityResult.Incompatible(issues)
        }
    }
}

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

interface ExtensionEntrypoint {
    fun createExtension(): LauncherExtension
}
