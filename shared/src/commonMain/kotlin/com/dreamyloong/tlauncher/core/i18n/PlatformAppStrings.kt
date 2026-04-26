package com.dreamyloong.tlauncher.core.i18n

import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon

expect val AppStrings.homeStoragePermissionWarningTitle: String

expect val AppStrings.settingsManageStoragePermission: String

expect val AppStrings.settingsManageStorageGranted: String

expect val AppStrings.settingsManageStorageRequired: String

expect val AppStrings.settingsManageStorageUnsupported: String

expect val AppStrings.settingsManageStorageActionGrant: String

expect val AppStrings.settingsManageStorageActionOpen: String

expect fun AppStrings.homeStoragePermissionWarningSubtitle(templateName: String): String

expect fun AppStrings.launcherIconName(icon: ThemeLauncherIcon): String

expect fun AppStrings.settingsLauncherIconTitle(): String

expect fun AppStrings.settingsLauncherIconFollowTheme(): String

expect fun AppStrings.settingsLauncherIconSubtitle(
    currentIconName: String,
    themeIconName: String,
): String

expect fun AppStrings.settingsLauncherIconSaveAndClose(): String
