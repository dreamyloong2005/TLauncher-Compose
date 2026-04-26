package com.dreamyloong.tlauncher.core.plugin

import com.dreamyloong.tlauncher.core.action.LauncherAction
import com.dreamyloong.tlauncher.core.action.LauncherActionContext
import com.dreamyloong.tlauncher.core.extension.ExtensionContext
import com.dreamyloong.tlauncher.core.extension.ExtensionFeature
import com.dreamyloong.tlauncher.core.extension.LauncherExtension
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.ResolvedPage
import com.dreamyloong.tlauncher.core.platform.GameLaunchContext
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.settings.SettingsContributionBundle
import com.dreamyloong.tlauncher.core.settings.SettingsSectionContext
import com.dreamyloong.tlauncher.core.settings.SettingsSection
import com.dreamyloong.tlauncher.core.template.TemplatePackage
import com.dreamyloong.tlauncher.core.template.TemplateLaunchPreparationContext
import com.dreamyloong.tlauncher.core.theme.ThemePackage

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
