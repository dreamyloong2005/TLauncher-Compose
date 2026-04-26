package com.dreamyloong.tlauncher.core.i18n

import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon

actual val AppStrings.homeStoragePermissionWarningTitle: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "需要管理储存权限"
        SupportedLanguage.EN_US -> "Manage Storage Access Required"
    }

actual val AppStrings.settingsManageStoragePermission: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "管理储存权限"
        SupportedLanguage.EN_US -> "Manage Storage Access"
    }

actual val AppStrings.settingsManageStorageGranted: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "已开启，可以访问外部目录并完成文件检查。"
        SupportedLanguage.EN_US -> "Granted. External directories and file checks are available."
    }

actual val AppStrings.settingsManageStorageRequired: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "未开启。部分外部目录访问和文件检查需要这项权限。"
        SupportedLanguage.EN_US -> "Not granted yet. Some external directory access and file checks depend on it."
    }

actual val AppStrings.settingsManageStorageUnsupported: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "当前 Android 版本不需要这项额外授权。"
        SupportedLanguage.EN_US -> "This Android version does not require the extra access flow."
    }

actual val AppStrings.settingsManageStorageActionGrant: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "获取管理储存权限"
        SupportedLanguage.EN_US -> "Grant Manage Storage Access"
    }

actual val AppStrings.settingsManageStorageActionOpen: String
    get() = when (language) {
        SupportedLanguage.ZH_CN -> "前往权限设置"
        SupportedLanguage.EN_US -> "Open Permission Settings"
    }

actual fun AppStrings.homeStoragePermissionWarningSubtitle(templateName: String): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "还没有授予管理储存权限，$templateName 的目录访问和文件检查暂时无法完成，点击立即前往授权。"
        SupportedLanguage.EN_US -> "Manage storage access is not granted yet, so $templateName cannot finish directory access and file checks. Tap to grant it now."
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
        SupportedLanguage.ZH_CN -> "桌面图标"
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
        SupportedLanguage.ZH_CN -> "当前将保存为：$currentIconName。跟随主题时会使用：$themeIconName。"
        SupportedLanguage.EN_US -> "Will save as: $currentIconName. Following the theme uses: $themeIconName."
    }
}

actual fun AppStrings.settingsLauncherIconSaveAndClose(): String {
    return when (language) {
        SupportedLanguage.ZH_CN -> "保存并关闭启动器"
        SupportedLanguage.EN_US -> "Save and Close Launcher"
    }
}
