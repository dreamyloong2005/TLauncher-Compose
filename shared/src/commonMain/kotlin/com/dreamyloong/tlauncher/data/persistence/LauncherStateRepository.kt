package com.dreamyloong.tlauncher.data.persistence

import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.extension.HostGrant
import com.dreamyloong.tlauncher.core.extension.HostGrantState
import com.dreamyloong.tlauncher.core.extension.HostPermissionKey
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.platform.launcherFileSystem
import com.dreamyloong.tlauncher.core.platform.launcherStorageDirectoryPath
import com.dreamyloong.tlauncher.core.theme.ThemePreference
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

@OptIn(ExperimentalSerializationApi::class)
private val launcherStateJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
    explicitNulls = false
}

@Serializable
internal data class PersistedLauncherState(
    val schemaVersion: Int = 1,
    val language: PersistedLanguagePreference = PersistedLanguagePreference(),
    val theme: PersistedThemePreference = PersistedThemePreference(),
    val library: PersistedLibraryState = PersistedLibraryState(),
    val extensionPrioritySourceIds: List<String> = emptyList(),
    val disabledExtensionSourceIds: List<String> = emptyList(),
    val extensionHostGrants: List<PersistedExtensionHostGrantState> = emptyList(),
    val extensionStates: List<PersistedExtensionStateEntry> = emptyList(),
)

@Serializable
internal data class PersistedLanguagePreference(
    val mode: String = "follow_system",
    val language: String? = null,
) {
    fun toDomain(): LanguagePreference {
        return if (mode == "fixed" && language != null) {
            LanguagePreference.Fixed(language.toSupportedLanguage())
        } else {
            LanguagePreference.FollowSystem
        }
    }

    companion object {
        fun fromDomain(preference: LanguagePreference): PersistedLanguagePreference {
            return when (preference) {
                LanguagePreference.FollowSystem -> PersistedLanguagePreference()
                is LanguagePreference.Fixed -> PersistedLanguagePreference(
                    mode = "fixed",
                    language = preference.language.name,
                )
            }
        }
    }
}

@Serializable
internal data class PersistedThemePreference(
    val mode: String = "follow_system",
    val themeId: String? = null,
) {
    fun toDomain(): ThemePreference {
        return if (mode == "fixed" && themeId != null) {
            ThemePreference.Fixed(ExtensionIdentityId(themeId))
        } else {
            ThemePreference.FollowSystem
        }
    }

    companion object {
        fun fromDomain(preference: ThemePreference): PersistedThemePreference {
            return when (preference) {
                ThemePreference.FollowSystem -> PersistedThemePreference()
                is ThemePreference.Fixed -> PersistedThemePreference(
                    mode = "fixed",
                    themeId = preference.themeId.value,
                )
            }
        }
    }
}

@Serializable
internal data class PersistedLibraryState(
    val instances: List<PersistedGameInstance> = emptyList(),
    val currentInstanceId: String? = null,
)

@Serializable
internal data class PersistedGameInstance(
    val id: String,
    val templatePackageId: String? = null,
    val displayName: String,
    val description: String? = null,
    val currentVersion: String? = null,
    val installPath: String? = null,
) {
    fun toDomain(): GameInstance {
        return GameInstance(
            id = GameInstanceId(id),
            templatePackageId = ExtensionIdentityId(templatePackageId.orEmpty()),
            displayName = displayName,
            description = description.orEmpty(),
            currentVersion = currentVersion,
            installPath = installPath,
        )
    }

    companion object {
        fun fromDomain(instance: GameInstance): PersistedGameInstance {
            return PersistedGameInstance(
                id = instance.id.value,
                templatePackageId = instance.templatePackageId.value,
                displayName = instance.displayName,
                description = instance.description,
                currentVersion = instance.currentVersion,
                installPath = instance.installPath,
            )
        }
    }
}

@Serializable
internal data class PersistedExtensionHostGrantState(
    val sourceId: String,
    val grants: List<PersistedHostGrant> = emptyList(),
) {
    fun toDomain(): Pair<String, Set<HostGrant>> {
        return sourceId to grants.map(PersistedHostGrant::toDomain).toSet()
    }

    companion object {
        fun fromDomain(
            sourceId: String,
            grants: Set<HostGrant>,
        ): PersistedExtensionHostGrantState {
            return PersistedExtensionHostGrantState(
                sourceId = sourceId,
                grants = grants.map(PersistedHostGrant::fromDomain).sortedBy { it.permissionKey },
            )
        }
    }
}

@Serializable
internal data class PersistedExtensionStateEntry(
    val sourceId: String,
    val values: Map<String, String> = emptyMap(),
)

@Serializable
internal data class PersistedHostGrant(
    val permissionKey: String,
    val state: String = HostGrantState.GRANTED.name,
    val reason: String? = null,
) {
    fun toDomain(): HostGrant {
        return HostGrant(
            permissionKey = permissionKey.toHostPermissionKey(),
            state = state.toHostGrantState(),
            reason = reason,
        )
    }

    companion object {
        fun fromDomain(grant: HostGrant): PersistedHostGrant {
            return PersistedHostGrant(
                permissionKey = grant.permissionKey.name,
                state = grant.state.name,
                reason = grant.reason,
            )
        }
    }
}

class LauncherStateRepository(
    private val fileSystem: FileSystem = launcherFileSystem(),
    private val filePath: Path = "${launcherStorageDirectoryPath()}/launcher-state.json".toPath(),
) {
    private var cachedState: PersistedLauncherState? = null

    internal fun read(): PersistedLauncherState {
        cachedState?.let { return it }
        val loaded = loadState()
        cachedState = loaded
        return loaded
    }

    internal fun update(transform: (PersistedLauncherState) -> PersistedLauncherState): PersistedLauncherState {
        val nextState = transform(read())
        persist(nextState)
        cachedState = nextState
        return nextState
    }

    internal fun readExtensionState(sourceId: String): Map<String, String> {
        return read().extensionStates
            .firstOrNull { entry -> entry.sourceId == sourceId }
            ?.values
            .orEmpty()
    }

    internal fun updateExtensionState(
        sourceId: String,
        transform: (Map<String, String>) -> Map<String, String>,
    ) {
        update { state ->
            val nextValues = transform(
                state.extensionStates
                    .firstOrNull { entry -> entry.sourceId == sourceId }
                    ?.values
                    .orEmpty(),
            )
            val filteredEntries = state.extensionStates.filterNot { entry -> entry.sourceId == sourceId }
            val nextEntries = if (nextValues.isEmpty()) {
                filteredEntries
            } else {
                filteredEntries + PersistedExtensionStateEntry(
                    sourceId = sourceId,
                    values = nextValues.sortedByKey(),
                )
            }
            state.copy(extensionStates = nextEntries.sortedBy { entry -> entry.sourceId })
        }
    }

    private fun loadState(): PersistedLauncherState {
        if (!fileSystem.exists(filePath)) {
            return PersistedLauncherState()
        }
        return runCatching {
            launcherStateJson.decodeFromString<PersistedLauncherState>(
                fileSystem.read(filePath) { readUtf8() },
            )
        }.getOrElse {
            PersistedLauncherState()
        }
    }

    private fun persist(state: PersistedLauncherState) {
        filePath.parent?.let(fileSystem::createDirectories)
        fileSystem.write(filePath) {
            writeUtf8(launcherStateJson.encodeToString(state))
        }
    }
}

private fun Map<String, String>.sortedByKey(): Map<String, String> {
    return entries
        .sortedBy { entry -> entry.key }
        .associate { entry -> entry.key to entry.value }
}

private fun String.toSupportedLanguage(): SupportedLanguage {
    return runCatching { SupportedLanguage.valueOf(this) }.getOrDefault(SupportedLanguage.EN_US)
}

private fun String.toHostPermissionKey(): HostPermissionKey {
    return runCatching { HostPermissionKey.valueOf(this) }.getOrDefault(HostPermissionKey.NETWORK_ACCESS)
}

private fun String.toHostGrantState(): HostGrantState {
    return runCatching { HostGrantState.valueOf(this) }.getOrDefault(HostGrantState.DENIED)
}
