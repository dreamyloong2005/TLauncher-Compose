package com.dreamyloong.tlauncher.sdk.i18n

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

data class AppStrings(
    val language: SupportedLanguage,
    val commonBack: String,
)
