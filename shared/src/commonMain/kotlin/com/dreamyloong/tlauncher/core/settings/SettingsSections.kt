package com.dreamyloong.tlauncher.core.settings

import com.dreamyloong.tlauncher.core.i18n.AppStrings
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.ManageStorageAccessState

enum class SettingsSectionOrigin {
    CORE,
    TEMPLATE,
    PLUGIN,
}

sealed interface SettingsTextRef {
    data class Direct(val value: String) : SettingsTextRef
    data class BundleKey(val key: String) : SettingsTextRef
}

data class SettingsLocalizationBundle(
    val sourceId: String,
    val filePaths: Map<SupportedLanguage, String> = emptyMap(),
    val entries: Map<SupportedLanguage, Map<String, String>>,
) {
    fun resolve(
        language: SupportedLanguage,
        key: String,
    ): String {
        return entries[language]?.get(key)
            ?: entries[SupportedLanguage.EN_US]?.get(key)
            ?: key
    }
}

data class SettingsSectionRegistration(
    val id: String,
    val sourceId: String,
    val origin: SettingsSectionOrigin,
    val title: SettingsTextRef,
    val subtitle: SettingsTextRef? = null,
)

sealed interface SettingsComponentRegistration {
    val componentId: String
    val parentSectionId: String
    val sourceId: String
    val orderHint: Int

    data class Summary(
        override val componentId: String,
        override val parentSectionId: String,
        override val sourceId: String,
        override val orderHint: Int = 0,
        val title: SettingsTextRef,
        val subtitle: SettingsTextRef,
    ) : SettingsComponentRegistration

    data class Value(
        override val componentId: String,
        override val parentSectionId: String,
        override val sourceId: String,
        override val orderHint: Int = 0,
        val label: SettingsTextRef,
        val value: SettingsTextRef,
    ) : SettingsComponentRegistration

    data class Actions(
        override val componentId: String,
        override val parentSectionId: String,
        override val sourceId: String,
        override val orderHint: Int = 0,
        val title: SettingsTextRef? = null,
        val subtitle: SettingsTextRef? = null,
        val options: List<SettingsOptionRegistration>,
    ) : SettingsComponentRegistration
}

data class SettingsOptionRegistration(
    val id: String,
    val label: SettingsTextRef,
    val selected: Boolean = false,
    val onClick: () -> Unit,
)

data class SettingsContributionBundle(
    val sourceId: String,
    val sections: List<SettingsSectionRegistration>,
    val components: List<SettingsComponentRegistration>,
    val localizationBundle: SettingsLocalizationBundle? = null,
)

data class SettingsSection(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val origin: SettingsSectionOrigin,
    val entries: List<SettingsSectionEntry>,
    val order: Int = 0,
)

sealed interface SettingsSectionEntry {
    data class Summary(
        val title: String,
        val subtitle: String,
    ) : SettingsSectionEntry

    data class Value(
        val label: String,
        val value: String,
    ) : SettingsSectionEntry

    data class Actions(
        val title: String? = null,
        val subtitle: String? = null,
        val options: List<SettingsSectionOption>,
    ) : SettingsSectionEntry
}

data class SettingsSectionOption(
    val id: String,
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit,
)

data class SettingsSectionContext(
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val currentTemplate: Template?,
    val allGames: List<GameInstance>,
    val visibleTemplates: List<Template>,
    val extensionPriorityEntries: List<ExtensionPriorityEntry>,
    val strings: AppStrings,
    val manageStorageAccessState: ManageStorageAccessState? = null,
)
