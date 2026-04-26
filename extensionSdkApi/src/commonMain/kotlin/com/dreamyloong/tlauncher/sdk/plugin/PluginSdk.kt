package com.dreamyloong.tlauncher.sdk.plugin

import com.dreamyloong.tlauncher.sdk.action.LauncherAction
import com.dreamyloong.tlauncher.sdk.action.LauncherActionContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionFeature
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension
import com.dreamyloong.tlauncher.sdk.page.PageContext
import com.dreamyloong.tlauncher.sdk.page.PageContributionBundle
import com.dreamyloong.tlauncher.sdk.page.ResolvedPage
import com.dreamyloong.tlauncher.sdk.platform.GameLaunchContext
import com.dreamyloong.tlauncher.sdk.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.sdk.settings.SettingsContributionBundle
import com.dreamyloong.tlauncher.sdk.settings.SettingsSection
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionContext
import com.dreamyloong.tlauncher.sdk.template.TemplateLaunchPreparationContext
import com.dreamyloong.tlauncher.sdk.template.TemplatePackage
import com.dreamyloong.tlauncher.sdk.theme.ThemePackage

data class PluginId(val value: String)

enum class PluginPermission {
    READ_GAMES,
    WRITE_GAMES,
    READ_TEMPLATES,
    NETWORK_ACCESS,
    FILESYSTEM_READ,
    FILESYSTEM_WRITE,
    UI_EXTENSION,
}

interface ProgrammableExtension : LauncherExtension {
    val permissions: Set<PluginPermission>
}

interface TemplateProviderExtension : ExtensionFeature {
    fun provideTemplatePackages(): List<TemplatePackage>
}

interface ThemeProviderExtension : ExtensionFeature {
    fun provideThemePackages(): List<ThemePackage>
}

interface SettingsSectionProviderExtension : ExtensionFeature {
    fun provideSettings(
        context: SettingsSectionContext,
    ): SettingsContributionBundle
}

interface SettingsSectionMutatorExtension : ExtensionFeature {
    fun mutateSettingsSections(
        context: SettingsSectionContext,
        sections: List<SettingsSection>,
    ): List<SettingsSection>
}

interface PageContributionProviderExtension : ExtensionFeature {
    fun providePageContributions(
        context: PageContext,
    ): List<PageContributionBundle>
}

interface PageTreeMutatorExtension : ExtensionFeature {
    fun mutatePage(
        context: PageContext,
        page: ResolvedPage,
    ): ResolvedPage
}

interface ActionInterceptorExtension : ExtensionFeature {
    fun interceptAction(
        action: LauncherAction,
        context: LauncherActionContext,
        next: (LauncherAction) -> Unit,
    )
}

interface LaunchInterceptorExtension : ExtensionFeature {
    fun interceptLaunch(
        request: GameLaunchRequest,
        context: GameLaunchContext,
        next: (GameLaunchRequest) -> Unit,
    )
}

interface TemplateLaunchPreparationInterceptorExtension : ExtensionFeature {
    fun interceptPreparedLaunchRequest(
        request: GameLaunchRequest?,
        context: TemplateLaunchPreparationContext,
        next: (GameLaunchRequest?) -> Unit,
    )
}
