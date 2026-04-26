package com.dreamyloong.tlauncher.core.i18n

import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon

actual val AppStrings.homeStoragePermissionWarningTitle: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "储存访问"
        SupportedLanguage.EN_US -> "Storage Access"
    }

actual val AppStrings.settingsManageStoragePermission: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "储存访问"
        SupportedLanguage.EN_US -> "Storage Access"
    }

actual val AppStrings.settingsManageStorageGranted: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "当前平台不需要额外储存权限。"
        SupportedLanguage.EN_US -> "This platform does not require extra storage access."
    }

actual val AppStrings.settingsManageStorageRequired: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "当前平台不需要额外储存权限。"
        SupportedLanguage.EN_US -> "This platform does not require extra storage access."
    }

actual val AppStrings.settingsManageStorageUnsupported: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "当前平台不需要额外储存权限。"
        SupportedLanguage.EN_US -> "This platform does not require extra storage access."
    }

actual val AppStrings.settingsManageStorageActionGrant: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "打开设置"
        SupportedLanguage.EN_US -> "Open Settings"
    }

actual val AppStrings.settingsManageStorageActionOpen: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "打开设置"
        SupportedLanguage.EN_US -> "Open Settings"
    }

actual fun AppStrings.homeStoragePermissionWarningSubtitle(templateName: String): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "$templateName 在当前平台不需要额外储存授权。"
        SupportedLanguage.EN_US -> "$templateName does not need extra storage authorization on this platform."
    }
}

actual fun AppStrings.launcherIconName(icon: ThemeLauncherIcon): String {
    return when (icon) {
        ThemeLauncherIcon.DEFAULT -> if (language == SupportedLanguage.ZH_CN) "默认图标" else "Default icon"
        ThemeLauncherIcon.NIGHT -> if (language == SupportedLanguage.ZH_CN) "黑夜图标" else "Night icon"
    }
}

actual fun AppStrings.settingsLauncherIconTitle(): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "启动器图标"
        SupportedLanguage.EN_US -> "Launcher Icon"
    }
}

actual fun AppStrings.settingsLauncherIconFollowTheme(): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "跟随主题自动选择"
        SupportedLanguage.EN_US -> "Follow Theme"
    }
}

actual fun AppStrings.settingsLauncherIconSubtitle(
    currentIconName: String,
    themeIconName: String,
): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "当前图标：$currentIconName。跟随主题时会使用：$themeIconName。"
        SupportedLanguage.EN_US -> "Current icon: $currentIconName. Following the theme uses: $themeIconName."
    }
}

actual fun AppStrings.settingsLauncherIconSaveAndClose(): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "应用图标"
        SupportedLanguage.EN_US -> "Apply Icon"
    }
}
