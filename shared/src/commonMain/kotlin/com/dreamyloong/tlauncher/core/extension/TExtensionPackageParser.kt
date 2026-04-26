package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionKind
import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

data class ExtensionPackageManifest(
    val extension: ExtensionManifest,
    val displayName: String,
    val version: String,
    val apiVersion: String,
    val description: String? = null,
    val entrypoints: Map<PlatformTarget, String>,
    val runtimeArtifacts: Map<PlatformTarget, String> = emptyMap(),
    val permissionKeys: Set<HostPermissionKey> = emptySet(),
)

data class ParsedExtensionPackage(
    val manifest: ExtensionPackageManifest,
    val sourceName: String,
)

object TExtensionPackageFormat {
    const val FILE_EXTENSION: String = ".textension"
    const val MANIFEST_PATH: String = "manifest.json"

    fun isTExtensionFileName(fileName: String): Boolean {
        return fileName.endsWith(FILE_EXTENSION, ignoreCase = true)
    }
}

interface ExtensionPackageSource {
    val sourceName: String

    fun exists(path: String): Boolean

    fun list(path: String = ""): List<String>

    fun readUtf8(path: String): String?

    fun readBytes(path: String): ByteArray?
}

class InMemoryExtensionPackageSource(
    override val sourceName: String,
    private val entries: Map<String, String>,
) : ExtensionPackageSource {
    override fun exists(path: String): Boolean {
        return path in entries
    }

    override fun list(path: String): List<String> {
        val prefix = normalizedDirectoryPrefix(path)
        return entries.keys
            .filter { key -> key.startsWith(prefix) }
            .map { key -> key.removePrefix(prefix).substringBefore('/') }
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
    }

    override fun readUtf8(path: String): String? {
        return entries[path]
    }

    override fun readBytes(path: String): ByteArray? {
        return entries[path]?.encodeToByteArray()
    }
}

class DirectoryExtensionPackageSource(
    override val sourceName: String,
    private val fileSystem: FileSystem,
    private val rootPath: Path,
) : ExtensionPackageSource {
    override fun exists(path: String): Boolean {
        return fileSystem.exists(rootPath / path)
    }

    override fun list(path: String): List<String> {
        val resolvedPath = rootPath / path
        if (!fileSystem.exists(resolvedPath)) {
            return emptyList()
        }
        return fileSystem.list(resolvedPath)
            .map { child -> child.name }
            .sorted()
    }

    override fun readUtf8(path: String): String? {
        val resolvedPath = rootPath / path
        if (!fileSystem.exists(resolvedPath)) return null
        return fileSystem.read(resolvedPath) {
            readUtf8()
        }
    }

    override fun readBytes(path: String): ByteArray? {
        val resolvedPath = rootPath / path
        if (!fileSystem.exists(resolvedPath)) return null
        return fileSystem.read(resolvedPath) {
            readByteArray()
        }
    }
}

class ExtensionPackageParseException(
    message: String,
) : IllegalArgumentException(message)

interface ExtensionPackageParser {
    fun parse(source: ExtensionPackageSource): ParsedExtensionPackage

    fun parseManifestText(manifestText: String): ExtensionPackageManifest
}

class JsonTExtensionPackageParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
) : ExtensionPackageParser {
    override fun parse(source: ExtensionPackageSource): ParsedExtensionPackage {
        requireTExtensionName(source.sourceName)
        val manifestText = source.readUtf8(TExtensionPackageFormat.MANIFEST_PATH)
            ?: throw ExtensionPackageParseException(
                "Missing ${TExtensionPackageFormat.MANIFEST_PATH} in ${source.sourceName}",
            )
        val manifest = parseManifestText(manifestText)
        validateRuntimeArtifacts(manifest, source)
        return ParsedExtensionPackage(
            manifest = manifest,
            sourceName = source.sourceName,
        )
    }

    override fun parseManifestText(manifestText: String): ExtensionPackageManifest {
        val document = try {
            json.decodeFromString<ExtensionPackageManifestDocument>(manifestText)
        } catch (error: Throwable) {
            throw ExtensionPackageParseException("Invalid ${TExtensionPackageFormat.MANIFEST_PATH}: ${error.message}")
        }

        val extension = ExtensionManifest(
            id = document.id.trim(),
            kind = document.kind,
            supportedTargets = document.supportedTargets,
            capabilities = document.capabilities.ifEmpty {
                ExtensionCapabilityPolicy.defaultFor(document.kind)
            },
            permissionKeys = document.permissionKeys,
            compatibility = document.compatibility.toDomain(),
        )
        validateManifestDocument(document, extension)
        return ExtensionPackageManifest(
            extension = extension,
            displayName = document.displayName.trim(),
            version = document.version.trim(),
            apiVersion = document.apiVersion.trim(),
            description = document.description?.trim()?.takeIf { it.isNotEmpty() },
            entrypoints = document.entrypoints.mapValues { (_, entrypoint) -> entrypoint.trim() },
            runtimeArtifacts = document.runtimeArtifacts.mapValues { (_, artifactPath) -> artifactPath.trim() },
            permissionKeys = document.permissionKeys,
        )
    }

    private fun requireTExtensionName(sourceName: String) {
        if (!TExtensionPackageFormat.isTExtensionFileName(sourceName)) {
            throw ExtensionPackageParseException(
                "Unsupported extension package name: $sourceName. Expected ${TExtensionPackageFormat.FILE_EXTENSION}.",
            )
        }
    }

    private fun validateManifestDocument(
        document: ExtensionPackageManifestDocument,
        extension: ExtensionManifest,
    ) {
        if (extension.registrationId.isBlank()) {
            throw ExtensionPackageParseException("Extension registration id must not be blank.")
        }
        if (document.displayName.isBlank()) {
            throw ExtensionPackageParseException("Extension displayName must not be blank.")
        }
        if (document.version.isBlank()) {
            throw ExtensionPackageParseException("Extension version must not be blank.")
        }
        if (document.apiVersion.isBlank()) {
            throw ExtensionPackageParseException("Extension apiVersion must not be blank.")
        }
        if (extension.supportedTargets.isEmpty()) {
            throw ExtensionPackageParseException("Extension supportedTargets must not be empty.")
        }
        if (extension.supportedTargets.size != 1) {
            throw ExtensionPackageParseException(
                "A .textension package must target exactly one platform. Split multi-platform extensions into one package per target.",
            )
        }
        if (!extension.hasValidCapabilityDeclaration()) {
            throw ExtensionPackageParseException(
                "Invalid capabilities for ${extension.kind.name.lowercase()} extension: ${extension.invalidCapabilities().joinToString()}",
            )
        }
        if (!extension.hasValidPermissionDeclaration()) {
            val details = buildList {
                val invalidPermissions = extension.invalidPermissionKeys()
                val missingPermissions = extension.missingPermissionKeysForCapabilities()
                if (invalidPermissions.isNotEmpty()) {
                    add("invalid permissionKeys: ${invalidPermissions.joinToString()}")
                }
                if (missingPermissions.isNotEmpty()) {
                    add("missing required permissionKeys: ${missingPermissions.joinToString()}")
                }
            }.joinToString("; ")
            throw ExtensionPackageParseException(
                "Invalid host permissions for ${extension.kind.name.lowercase()} extension: $details",
            )
        }
        if (document.entrypoints.isEmpty()) {
            throw ExtensionPackageParseException("Extension entrypoints must not be empty.")
        }
        if (document.entrypoints.keys != extension.supportedTargets) {
            throw ExtensionPackageParseException(
                "Extension entrypoints must match supportedTargets exactly for a single-target .textension package.",
            )
        }
        if (document.runtimeArtifacts.keys != extension.supportedTargets) {
            throw ExtensionPackageParseException(
                "Extension runtimeArtifacts must match supportedTargets exactly for a single-target .textension package.",
            )
        }
        val declaredTargets = document.entrypoints.keys + document.runtimeArtifacts.keys
        val unsupportedTargets = declaredTargets - extension.supportedTargets
        if (unsupportedTargets.isNotEmpty()) {
            throw ExtensionPackageParseException(
                "Entrypoints or runtime artifacts declared for unsupported targets: ${unsupportedTargets.joinToString()}",
            )
        }
        if (document.kind != ExtensionKind.PLUGIN && document.permissionKeys.isNotEmpty()) {
            throw ExtensionPackageParseException(
                "Only PLUGIN extensions may declare permissionKeys in ${TExtensionPackageFormat.MANIFEST_PATH}.",
            )
        }
        document.entrypoints.forEach { (target, entrypoint) ->
            if (entrypoint.isBlank()) {
                throw ExtensionPackageParseException("Entrypoint for $target must not be blank.")
            }
        }
        document.runtimeArtifacts.forEach { (target, artifactPath) ->
            validateRelativePackagePath(
                artifactPath = artifactPath,
                label = "Runtime artifact path for $target",
            )
        }
        if (extension.targetQualifier != null && extension.targetQualifier !in extension.supportedTargets) {
            throw ExtensionPackageParseException(
                "Extension registration id target suffix ${extension.targetQualifier} must also appear in supportedTargets.",
            )
        }
    }

    private fun validateRuntimeArtifacts(
        manifest: ExtensionPackageManifest,
        source: ExtensionPackageSource,
    ) {
        manifest.runtimeArtifacts.forEach { (_, artifactPath) ->
            if (!source.exists(artifactPath)) {
                throw ExtensionPackageParseException(
                    "Declared runtime artifact is missing from ${source.sourceName}: $artifactPath",
                )
            }
        }
    }

    private fun validateRelativePackagePath(
        artifactPath: String,
        label: String,
    ) {
        val trimmedPath = artifactPath.trim()
        if (trimmedPath.isEmpty()) {
            throw ExtensionPackageParseException("$label must not be blank.")
        }
        if (trimmedPath.startsWith("/") || trimmedPath.startsWith("\\") || ':' in trimmedPath) {
            throw ExtensionPackageParseException("$label must be relative to the .textension package root.")
        }
        val normalizedSegments = trimmedPath.split('/', '\\')
        if (normalizedSegments.any { segment -> segment == ".." }) {
            throw ExtensionPackageParseException("$label must not escape the .textension package root.")
        }
    }
}

object DefaultExtensionPackageParser : ExtensionPackageParser by JsonTExtensionPackageParser()

private fun normalizedDirectoryPrefix(path: String): String {
    val trimmed = path.trim().trim('/')
    return if (trimmed.isBlank()) "" else "$trimmed/"
}

@Serializable
private data class ExtensionPackageManifestDocument(
    val id: String,
    val kind: ExtensionKind,
    val displayName: String,
    val version: String,
    val apiVersion: String,
    val description: String? = null,
    val supportedTargets: Set<PlatformTarget>,
    val capabilities: Set<ExtensionCapability> = emptySet(),
    val compatibility: ExtensionCompatibilityDocument,
    val entrypoints: Map<PlatformTarget, String>,
    val runtimeArtifacts: Map<PlatformTarget, String> = emptyMap(),
    val permissionKeys: Set<HostPermissionKey> = emptySet(),
)

@Serializable
private data class ExtensionCompatibilityDocument(
    val packageFormatVersion: Int,
    val sdkApiVersion: Int? = null,
    val minSdkApiVersion: Int? = null,
    val targetSdkApiVersion: Int? = null,
) {
    fun toDomain(): ExtensionCompatibility {
        val hasSdkApiVersionShorthand = sdkApiVersion != null
        val hasSdkApiVersionRange = minSdkApiVersion != null && targetSdkApiVersion != null
        if (!hasSdkApiVersionShorthand && !hasSdkApiVersionRange) {
            throw ExtensionPackageParseException(
                "Extension compatibility must declare minSdkApiVersion and targetSdkApiVersion, or sdkApiVersion shorthand.",
            )
        }
        val resolvedTargetSdkApiVersion = targetSdkApiVersion ?: sdkApiVersion!!
        val resolvedMinSdkApiVersion = minSdkApiVersion ?: sdkApiVersion!!
        return ExtensionCompatibility(
            packageFormatVersion = packageFormatVersion,
            sdkApiVersion = sdkApiVersion ?: resolvedTargetSdkApiVersion,
            minSdkApiVersion = resolvedMinSdkApiVersion,
            targetSdkApiVersion = resolvedTargetSdkApiVersion,
        )
    }
}
