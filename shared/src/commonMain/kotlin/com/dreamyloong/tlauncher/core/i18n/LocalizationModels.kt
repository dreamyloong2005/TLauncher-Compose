package com.dreamyloong.tlauncher.core.i18n

enum class SupportedLanguage {
    ZH_CN,
    EN_US,
}

sealed interface LanguagePreference {
    data object FollowSystem : LanguagePreference

    data class Fixed(val language: SupportedLanguage) : LanguagePreference
}

data class LocalizedText(
    val zhCn: String,
    val enUs: String,
) {
    fun resolve(language: SupportedLanguage): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> zhCn
            SupportedLanguage.EN_US -> enUs
        }
    }
}

interface LanguageSettingsStore {
    fun currentPreference(): LanguagePreference
    fun setCurrentPreference(preference: LanguagePreference)
}

interface LanguageResolver {
    fun resolve(
        preference: LanguagePreference,
        systemTag: String,
    ): SupportedLanguage
}

class DefaultLanguageResolver : LanguageResolver {
    override fun resolve(
        preference: LanguagePreference,
        systemTag: String,
    ): SupportedLanguage {
        return when (preference) {
            LanguagePreference.FollowSystem -> languageFromTag(systemTag)
            is LanguagePreference.Fixed -> preference.language
        }
    }
}

fun languageFromTag(tag: String): SupportedLanguage {
    val lowered = tag.lowercase()
    return if (lowered.startsWith("zh")) {
        SupportedLanguage.ZH_CN
    } else {
        SupportedLanguage.EN_US
    }
}

expect fun platformLanguageTag(): String

