package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.action.ExtensionPriorityDirection
import com.dreamyloong.tlauncher.core.action.LauncherAction
import com.dreamyloong.tlauncher.core.action.LauncherActionContext
import com.dreamyloong.tlauncher.core.action.LauncherActionDispatcher
import com.dreamyloong.tlauncher.core.i18n.AppStrings
import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.i18n.LocalizedText
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.model.ExtensionDescriptor
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.ExtensionKind
import com.dreamyloong.tlauncher.core.model.ExtensionManifest
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.model.LaunchSupportLevel
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.model.RuntimeRequirement
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.TemplateDescriptor
import com.dreamyloong.tlauncher.core.model.TemplateReleaseState
import com.dreamyloong.tlauncher.core.model.TemplateSourceType
import com.dreamyloong.tlauncher.core.model.TemplateTargetFacet
import com.dreamyloong.tlauncher.core.page.PageActionRegistration
import com.dreamyloong.tlauncher.core.page.PageActionStyle
import com.dreamyloong.tlauncher.core.page.PageChoiceOptionRegistration
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageDisplayPolicy
import com.dreamyloong.tlauncher.core.page.PageFooterLayoutRegistration
import com.dreamyloong.tlauncher.core.page.PageLocalizationBundle
import com.dreamyloong.tlauncher.core.page.PageNodePlacement
import com.dreamyloong.tlauncher.core.page.PageNodeRegistration
import com.dreamyloong.tlauncher.core.page.PageProgressRegistration
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageValueItemRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.page.PageWidgetTone
import com.dreamyloong.tlauncher.core.page.ResolvedPage
import com.dreamyloong.tlauncher.core.page.ResolvedPageAction
import com.dreamyloong.tlauncher.core.page.ResolvedPageChoiceOption
import com.dreamyloong.tlauncher.core.page.ResolvedPageNode
import com.dreamyloong.tlauncher.core.page.ResolvedPageProgress
import com.dreamyloong.tlauncher.core.page.ResolvedPageValueItem
import com.dreamyloong.tlauncher.core.page.ResolvedPageWidget
import com.dreamyloong.tlauncher.core.platform.DirectoryPickerState
import com.dreamyloong.tlauncher.core.platform.FilePickerState
import com.dreamyloong.tlauncher.core.platform.GameLaunchContext
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.platform.GameLaunchState
import com.dreamyloong.tlauncher.core.platform.ManageStorageAccessState
import com.dreamyloong.tlauncher.core.platform.PickedFile
import com.dreamyloong.tlauncher.core.plugin.ActionInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.LaunchInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.PageContributionProviderExtension
import com.dreamyloong.tlauncher.core.plugin.PageTreeMutatorExtension
import com.dreamyloong.tlauncher.core.plugin.SettingsSectionMutatorExtension
import com.dreamyloong.tlauncher.core.plugin.SettingsSectionProviderExtension
import com.dreamyloong.tlauncher.core.plugin.TemplateLaunchPreparationInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.TemplateProviderExtension
import com.dreamyloong.tlauncher.core.plugin.ThemeProviderExtension
import com.dreamyloong.tlauncher.core.settings.SettingsComponentRegistration
import com.dreamyloong.tlauncher.core.settings.SettingsContributionBundle
import com.dreamyloong.tlauncher.core.settings.SettingsLocalizationBundle
import com.dreamyloong.tlauncher.core.settings.SettingsOptionRegistration
import com.dreamyloong.tlauncher.core.settings.SettingsSection
import com.dreamyloong.tlauncher.core.settings.SettingsSectionContext
import com.dreamyloong.tlauncher.core.settings.SettingsSectionEntry
import com.dreamyloong.tlauncher.core.settings.SettingsSectionOption
import com.dreamyloong.tlauncher.core.settings.SettingsSectionOrigin
import com.dreamyloong.tlauncher.core.settings.SettingsSectionRegistration
import com.dreamyloong.tlauncher.core.settings.SettingsTextRef
import com.dreamyloong.tlauncher.core.template.TemplateFileCheckResult
import com.dreamyloong.tlauncher.core.template.TemplateLaunchPreparationContext
import com.dreamyloong.tlauncher.core.template.TemplatePackage
import com.dreamyloong.tlauncher.core.template.TemplatePlatformFacet
import com.dreamyloong.tlauncher.core.theme.LauncherThemeDefinition
import com.dreamyloong.tlauncher.core.theme.ThemeBrightness
import com.dreamyloong.tlauncher.core.theme.ThemeColorTokens
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import com.dreamyloong.tlauncher.core.theme.ThemePackage
import com.dreamyloong.tlauncher.core.theme.ThemePlatformFacet
import com.dreamyloong.tlauncher.core.theme.ThemePreference
import com.dreamyloong.tlauncher.core.theme.ThemeSceneManifest
import com.dreamyloong.tlauncher.core.theme.ThemeSceneSpec
import com.dreamyloong.tlauncher.sdk.account.LauncherAccount as SdkLauncherAccount
import com.dreamyloong.tlauncher.sdk.account.LauncherAccountProvider as SdkLauncherAccountProvider
import com.dreamyloong.tlauncher.sdk.account.SteamAccountLoginMode as SdkSteamAccountLoginMode
import com.dreamyloong.tlauncher.sdk.action.ExtensionPriorityDirection as SdkExtensionPriorityDirection
import com.dreamyloong.tlauncher.sdk.action.LauncherAction as SdkLauncherAction
import com.dreamyloong.tlauncher.sdk.action.LauncherActionContext as SdkLauncherActionContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCapability as SdkExtensionCapability
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibility as SdkExtensionCompatibility
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibilityIssue as SdkExtensionCompatibilityIssue
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibilityResult as SdkExtensionCompatibilityResult
import com.dreamyloong.tlauncher.sdk.extension.ExtensionContext as SdkExtensionContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionFeature as SdkExtensionFeature
import com.dreamyloong.tlauncher.sdk.extension.ExtensionHostPaths as SdkExtensionHostPaths
import com.dreamyloong.tlauncher.sdk.extension.ExtensionPackageResources as SdkExtensionPackageResources
import com.dreamyloong.tlauncher.sdk.extension.ExtensionStateStore as SdkExtensionStateStore
import com.dreamyloong.tlauncher.sdk.extension.HostGrant as SdkHostGrant
import com.dreamyloong.tlauncher.sdk.extension.HostGrantState as SdkHostGrantState
import com.dreamyloong.tlauncher.sdk.extension.HostPermissionKey as SdkHostPermissionKey
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension as SdkLauncherExtension
import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices as SdkExtensionHostServices
import com.dreamyloong.tlauncher.sdk.i18n.AppStrings as SdkAppStrings
import com.dreamyloong.tlauncher.sdk.i18n.LanguagePreference as SdkLanguagePreference
import com.dreamyloong.tlauncher.sdk.i18n.LocalizedText as SdkLocalizedText
import com.dreamyloong.tlauncher.sdk.i18n.SupportedLanguage as SdkSupportedLanguage
import com.dreamyloong.tlauncher.sdk.model.ExtensionDescriptor as SdkExtensionDescriptor
import com.dreamyloong.tlauncher.sdk.model.ExtensionIdentityId as SdkExtensionIdentityId
import com.dreamyloong.tlauncher.sdk.model.ExtensionKind as SdkExtensionKind
import com.dreamyloong.tlauncher.sdk.model.ExtensionManifest as SdkExtensionManifest
import com.dreamyloong.tlauncher.sdk.model.ExtensionPriorityEntry as SdkExtensionPriorityEntry
import com.dreamyloong.tlauncher.sdk.model.GameInstance as SdkGameInstance
import com.dreamyloong.tlauncher.sdk.model.GameInstanceId as SdkGameInstanceId
import com.dreamyloong.tlauncher.sdk.model.LaunchSupportLevel as SdkLaunchSupportLevel
import com.dreamyloong.tlauncher.sdk.model.PlatformTarget as SdkPlatformTarget
import com.dreamyloong.tlauncher.sdk.model.RuntimeRequirement as SdkRuntimeRequirement
import com.dreamyloong.tlauncher.sdk.model.Template as SdkTemplate
import com.dreamyloong.tlauncher.sdk.model.TemplateDescriptor as SdkTemplateDescriptor
import com.dreamyloong.tlauncher.sdk.model.TemplateReleaseState as SdkTemplateReleaseState
import com.dreamyloong.tlauncher.sdk.model.TemplateSourceType as SdkTemplateSourceType
import com.dreamyloong.tlauncher.sdk.model.TemplateTargetFacet as SdkTemplateTargetFacet
import com.dreamyloong.tlauncher.sdk.page.PageActionRegistration as SdkPageActionRegistration
import com.dreamyloong.tlauncher.sdk.page.PageActionStyle as SdkPageActionStyle
import com.dreamyloong.tlauncher.sdk.page.PageChoiceOptionRegistration as SdkPageChoiceOptionRegistration
import com.dreamyloong.tlauncher.sdk.page.PageContext as SdkPageContext
import com.dreamyloong.tlauncher.sdk.page.PageContributionBundle as SdkPageContributionBundle
import com.dreamyloong.tlauncher.sdk.page.PageDisplayPolicy as SdkPageDisplayPolicy
import com.dreamyloong.tlauncher.sdk.page.PageFooterLayoutRegistration as SdkPageFooterLayoutRegistration
import com.dreamyloong.tlauncher.sdk.page.PageLocalizationBundle as SdkPageLocalizationBundle
import com.dreamyloong.tlauncher.sdk.page.PageNodePlacement as SdkPageNodePlacement
import com.dreamyloong.tlauncher.sdk.page.PageNodeRegistration as SdkPageNodeRegistration
import com.dreamyloong.tlauncher.sdk.page.PageProgressRegistration as SdkPageProgressRegistration
import com.dreamyloong.tlauncher.sdk.page.PageRegistration as SdkPageRegistration
import com.dreamyloong.tlauncher.sdk.page.PageSectionRegistration as SdkPageSectionRegistration
import com.dreamyloong.tlauncher.sdk.page.PageTextRef as SdkPageTextRef
import com.dreamyloong.tlauncher.sdk.page.PageValueItemRegistration as SdkPageValueItemRegistration
import com.dreamyloong.tlauncher.sdk.page.PageWidgetRegistration as SdkPageWidgetRegistration
import com.dreamyloong.tlauncher.sdk.page.PageWidgetRegistrationModel as SdkPageWidgetRegistrationModel
import com.dreamyloong.tlauncher.sdk.page.PageWidgetTone as SdkPageWidgetTone
import com.dreamyloong.tlauncher.sdk.page.ResolvedPage as SdkResolvedPage
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageAction as SdkResolvedPageAction
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageChoiceOption as SdkResolvedPageChoiceOption
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageNode as SdkResolvedPageNode
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageProgress as SdkResolvedPageProgress
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageValueItem as SdkResolvedPageValueItem
import com.dreamyloong.tlauncher.sdk.page.ResolvedPageWidget as SdkResolvedPageWidget
import com.dreamyloong.tlauncher.sdk.platform.DirectoryPickerState as SdkDirectoryPickerState
import com.dreamyloong.tlauncher.sdk.platform.FilePickerState as SdkFilePickerState
import com.dreamyloong.tlauncher.sdk.platform.GameLaunchContext as SdkGameLaunchContext
import com.dreamyloong.tlauncher.sdk.platform.GameLaunchRequest as SdkGameLaunchRequest
import com.dreamyloong.tlauncher.sdk.platform.GameLaunchState as SdkGameLaunchState
import com.dreamyloong.tlauncher.sdk.platform.ManageStorageAccessState as SdkManageStorageAccessState
import com.dreamyloong.tlauncher.sdk.platform.PickedFile as SdkPickedFile
import com.dreamyloong.tlauncher.sdk.plugin.ActionInterceptorExtension as SdkActionInterceptorExtension
import com.dreamyloong.tlauncher.sdk.plugin.LaunchInterceptorExtension as SdkLaunchInterceptorExtension
import com.dreamyloong.tlauncher.sdk.plugin.PageContributionProviderExtension as SdkPageContributionProviderExtension
import com.dreamyloong.tlauncher.sdk.plugin.PageTreeMutatorExtension as SdkPageTreeMutatorExtension
import com.dreamyloong.tlauncher.sdk.plugin.SettingsSectionMutatorExtension as SdkSettingsSectionMutatorExtension
import com.dreamyloong.tlauncher.sdk.plugin.SettingsSectionProviderExtension as SdkSettingsSectionProviderExtension
import com.dreamyloong.tlauncher.sdk.plugin.TemplateLaunchPreparationInterceptorExtension as SdkTemplateLaunchPreparationInterceptorExtension
import com.dreamyloong.tlauncher.sdk.plugin.TemplateProviderExtension as SdkTemplateProviderExtension
import com.dreamyloong.tlauncher.sdk.plugin.ThemeProviderExtension as SdkThemeProviderExtension
import com.dreamyloong.tlauncher.sdk.settings.SettingsComponentRegistration as SdkSettingsComponentRegistration
import com.dreamyloong.tlauncher.sdk.settings.SettingsContributionBundle as SdkSettingsContributionBundle
import com.dreamyloong.tlauncher.sdk.settings.SettingsLocalizationBundle as SdkSettingsLocalizationBundle
import com.dreamyloong.tlauncher.sdk.settings.SettingsOptionRegistration as SdkSettingsOptionRegistration
import com.dreamyloong.tlauncher.sdk.settings.SettingsSection as SdkSettingsSection
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionContext as SdkSettingsSectionContext
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionEntry as SdkSettingsSectionEntry
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionOption as SdkSettingsSectionOption
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionOrigin as SdkSettingsSectionOrigin
import com.dreamyloong.tlauncher.sdk.settings.SettingsSectionRegistration as SdkSettingsSectionRegistration
import com.dreamyloong.tlauncher.sdk.settings.SettingsTextRef as SdkSettingsTextRef
import com.dreamyloong.tlauncher.sdk.template.TemplateLaunchPreparationContext as SdkTemplateLaunchPreparationContext
import com.dreamyloong.tlauncher.sdk.template.TemplatePackage as SdkTemplatePackage
import com.dreamyloong.tlauncher.sdk.template.TemplatePlatformFacet as SdkTemplatePlatformFacet
import com.dreamyloong.tlauncher.sdk.theme.LauncherThemeDefinition as SdkLauncherThemeDefinition
import com.dreamyloong.tlauncher.sdk.theme.ThemeBrightness as SdkThemeBrightness
import com.dreamyloong.tlauncher.sdk.theme.ThemeColorTokens as SdkThemeColorTokens
import com.dreamyloong.tlauncher.sdk.theme.ThemeLauncherIcon as SdkThemeLauncherIcon
import com.dreamyloong.tlauncher.sdk.theme.ThemePackage as SdkThemePackage
import com.dreamyloong.tlauncher.sdk.theme.ThemePlatformFacet as SdkThemePlatformFacet
import com.dreamyloong.tlauncher.sdk.theme.ThemePreference as SdkThemePreference
import com.dreamyloong.tlauncher.sdk.theme.ThemeSceneManifest as SdkThemeSceneManifest
import com.dreamyloong.tlauncher.sdk.theme.ThemeSceneSpec as SdkThemeSceneSpec

internal fun SdkLauncherExtension.toCoreLauncherExtension(): LauncherExtension {
    return SdkLauncherExtensionAdapter(this)
}

private class SdkLauncherExtensionAdapter(
    private val delegate: SdkLauncherExtension,
) : LauncherExtension {
    override val extension: ExtensionManifest
        get() = delegate.extension.toCore()
    override val displayName: String
        get() = delegate.displayName
    override val version: String
        get() = delegate.version
    override val apiVersion: String
        get() = delegate.apiVersion
    override val entrypoint: String
        get() = delegate.entrypoint
    override val hostGrants: Set<HostGrant>
        get() = delegate.hostGrants.mapToSet { it.toCore() }
    override val packageResources: ExtensionPackageResources
        get() = delegate.packageResources.toCore()

    override fun createFeatures(context: ExtensionContext): List<ExtensionFeature> {
        return delegate.createFeatures(context.toSdk()).map { feature -> feature.toCoreFeature() }
    }
}

private fun SdkExtensionFeature.toCoreFeature(): ExtensionFeature {
    return when (this) {
        is SdkTemplateProviderExtension -> object : TemplateProviderExtension {
            override fun provideTemplatePackages(): List<TemplatePackage> {
                return this@toCoreFeature.provideTemplatePackages().map { it.toCore() }
            }
        }

        is SdkThemeProviderExtension -> object : ThemeProviderExtension {
            override fun provideThemePackages(): List<ThemePackage> {
                return this@toCoreFeature.provideThemePackages().map { it.toCore() }
            }
        }

        is SdkSettingsSectionProviderExtension -> object : SettingsSectionProviderExtension {
            override fun provideSettings(context: SettingsSectionContext): SettingsContributionBundle {
                return this@toCoreFeature.provideSettings(context.toSdk()).toCore()
            }
        }

        is SdkSettingsSectionMutatorExtension -> object : SettingsSectionMutatorExtension {
            override fun mutateSettingsSections(
                context: SettingsSectionContext,
                sections: List<SettingsSection>,
            ): List<SettingsSection> {
                return this@toCoreFeature
                    .mutateSettingsSections(context.toSdk(), sections.map { it.toSdk() })
                    .map { it.toCore() }
            }
        }

        is SdkPageContributionProviderExtension -> object : PageContributionProviderExtension {
            override fun providePageContributions(context: PageContext): List<PageContributionBundle> {
                return this@toCoreFeature.providePageContributions(context.toSdk()).map { it.toCore() }
            }
        }

        is SdkPageTreeMutatorExtension -> object : PageTreeMutatorExtension {
            override fun mutatePage(
                context: PageContext,
                page: ResolvedPage,
            ): ResolvedPage {
                return this@toCoreFeature.mutatePage(context.toSdk(), page.toSdk()).toCore()
            }
        }

        is SdkActionInterceptorExtension -> object : ActionInterceptorExtension {
            override fun interceptAction(
                action: LauncherAction,
                context: LauncherActionContext,
                next: (LauncherAction) -> Unit,
            ) {
                this@toCoreFeature.interceptAction(action.toSdk(), context.toSdk()) { nextAction ->
                    next(nextAction.toCore())
                }
            }
        }

        is SdkLaunchInterceptorExtension -> object : LaunchInterceptorExtension {
            override fun interceptLaunch(
                request: GameLaunchRequest,
                context: GameLaunchContext,
                next: (GameLaunchRequest) -> Unit,
            ) {
                this@toCoreFeature.interceptLaunch(request.toSdk(), context.toSdk()) { nextRequest ->
                    next(nextRequest.toCore())
                }
            }
        }

        is SdkTemplateLaunchPreparationInterceptorExtension -> object : TemplateLaunchPreparationInterceptorExtension {
            override fun interceptPreparedLaunchRequest(
                request: GameLaunchRequest?,
                context: TemplateLaunchPreparationContext,
                next: (GameLaunchRequest?) -> Unit,
            ) {
                this@toCoreFeature.interceptPreparedLaunchRequest(
                    request = request?.toSdk(),
                    context = context.toSdk(),
                ) { nextRequest ->
                    next(nextRequest?.toCore())
                }
            }
        }

        else -> object : ExtensionFeature {}
    }
}

private fun ExtensionContext.toSdk(): SdkExtensionContext {
    return CoreToSdkExtensionContext(this)
}

private class CoreToSdkExtensionContext(
    private val delegate: ExtensionContext,
) : SdkExtensionContext {
    override val apiVersion: String
        get() = delegate.apiVersion
    override val hostGrants: Set<SdkHostGrant>
        get() = delegate.hostGrants.mapToSet { it.toSdk() }
    override val packageResources: SdkExtensionPackageResources
        get() = delegate.packageResources.toSdk()
    override val stateStore: SdkExtensionStateStore
        get() = delegate.stateStore.toSdk()
    override val hostPaths: SdkExtensionHostPaths
        get() = delegate.hostPaths.toSdk()
    override val hostServices: SdkExtensionHostServices
        get() = delegate.hostServices

    override fun withHostGrants(grants: Set<SdkHostGrant>): SdkExtensionContext {
        return delegate.withHostGrants(grants.mapToSet { it.toCore() }).toSdk()
    }

    override fun withPackageResources(resources: SdkExtensionPackageResources): SdkExtensionContext {
        return delegate.withPackageResources(resources.toCore()).toSdk()
    }

    override fun withStateStore(store: SdkExtensionStateStore): SdkExtensionContext {
        return delegate.withStateStore(store.toCore()).toSdk()
    }

    override fun withHostPaths(paths: SdkExtensionHostPaths): SdkExtensionContext {
        return delegate.withHostPaths(paths.toCore()).toSdk()
    }

    override fun withHostServices(services: SdkExtensionHostServices): SdkExtensionContext {
        return delegate.withHostServices(services).toSdk()
    }
}

private fun ExtensionPackageResources.toSdk(): SdkExtensionPackageResources {
    val delegate = this
    return object : SdkExtensionPackageResources {
        override fun exists(path: String): Boolean = delegate.exists(path)
        override fun list(path: String): List<String> = delegate.list(path)
        override fun readUtf8(path: String): String? = delegate.readUtf8(path)
        override fun readBytes(path: String): ByteArray? = delegate.readBytes(path)
    }
}

private fun SdkExtensionPackageResources.toCore(): ExtensionPackageResources {
    val delegate = this
    return object : ExtensionPackageResources {
        override fun exists(path: String): Boolean = delegate.exists(path)
        override fun list(path: String): List<String> = delegate.list(path)
        override fun readUtf8(path: String): String? = delegate.readUtf8(path)
        override fun readBytes(path: String): ByteArray? = delegate.readBytes(path)
    }
}

private fun ExtensionStateStore.toSdk(): SdkExtensionStateStore {
    val delegate = this
    return object : SdkExtensionStateStore {
        override fun read(key: String): String? = delegate.read(key)
        override fun write(key: String, value: String?) = delegate.write(key, value)
    }
}

private fun SdkExtensionStateStore.toCore(): ExtensionStateStore {
    val delegate = this
    return object : ExtensionStateStore {
        override fun read(key: String): String? = delegate.read(key)
        override fun write(key: String, value: String?) = delegate.write(key, value)
    }
}

private fun ExtensionHostPaths.toSdk(): SdkExtensionHostPaths {
    return SdkExtensionHostPaths(
        appFilesDirectoryPath = appFilesDirectoryPath,
        launcherStorageDirectoryPath = launcherStorageDirectoryPath,
        packageName = packageName,
    )
}

private fun SdkExtensionHostPaths.toCore(): ExtensionHostPaths {
    return ExtensionHostPaths(
        appFilesDirectoryPath = appFilesDirectoryPath,
        launcherStorageDirectoryPath = launcherStorageDirectoryPath,
        packageName = packageName,
    )
}

internal fun SdkExtensionManifest.toCore(): ExtensionManifest {
    return ExtensionManifest(
        id = id,
        kind = kind.toCore(),
        supportedTargets = supportedTargets.mapToSet { it.toCore() },
        capabilities = capabilities.mapToSet { it.toCore() },
        permissionKeys = permissionKeys.mapToSet { it.toCore() },
        compatibility = compatibility.toCore(),
    )
}

private fun ExtensionManifest.toSdk(): SdkExtensionManifest {
    return SdkExtensionManifest(
        id = id,
        kind = kind.toSdk(),
        supportedTargets = supportedTargets.mapToSet { it.toSdk() },
        capabilities = capabilities.mapToSet { it.toSdk() },
        permissionKeys = permissionKeys.mapToSet { it.toSdk() },
        compatibility = compatibility.toSdk(),
    )
}

private fun SdkExtensionDescriptor.toCore(): ExtensionDescriptor {
    return ExtensionDescriptor(
        extension = extension.toCore(),
        displayName = displayName,
        enabled = enabled,
        compatibility = compatibility.toCore(),
        priorityPinnedToBottom = priorityPinnedToBottom,
        hostGrants = hostGrants.mapToSet { it.toCore() },
        userEnabled = userEnabled,
        sourceName = sourceName,
        packageVersion = packageVersion,
        apiVersion = apiVersion,
        packageDescription = packageDescription,
        runtimeLoaded = runtimeLoaded,
        runtimeLoadError = runtimeLoadError,
    )
}

private fun ExtensionDescriptor.toSdk(): SdkExtensionDescriptor {
    return SdkExtensionDescriptor(
        extension = extension.toSdk(),
        displayName = displayName,
        enabled = enabled,
        compatibility = compatibility.toSdk(),
        priorityPinnedToBottom = priorityPinnedToBottom,
        hostGrants = hostGrants.mapToSet { it.toSdk() },
        userEnabled = userEnabled,
        sourceName = sourceName,
        packageVersion = packageVersion,
        apiVersion = apiVersion,
        packageDescription = packageDescription,
        runtimeLoaded = runtimeLoaded,
        runtimeLoadError = runtimeLoadError,
    )
}

private fun SdkExtensionPriorityEntry.toCore(): ExtensionPriorityEntry {
    return ExtensionPriorityEntry(
        descriptor = descriptor.toCore(),
        priority = priority,
    )
}

private fun ExtensionPriorityEntry.toSdk(): SdkExtensionPriorityEntry {
    return SdkExtensionPriorityEntry(
        descriptor = descriptor.toSdk(),
        priority = priority,
    )
}

private fun SdkTemplatePackage.toCore(): TemplatePackage {
    return TemplatePackage(
        extension = extension.toCore(),
        schemaVersion = schemaVersion,
        name = name.toCore(),
        description = description.toCore(),
        defaultInstanceDescription = defaultInstanceDescription?.toCore(),
        sourceType = sourceType.toCore(),
        releaseState = releaseState.toCore(),
        notes = notes?.toCore(),
        platforms = platforms.map { it.toCore() },
        devPath = devPath,
    )
}

private fun SdkTemplatePlatformFacet.toCore(): TemplatePlatformFacet {
    return TemplatePlatformFacet(
        target = target.toCore(),
        supportLevel = supportLevel.toCore(),
        runtimeRequirements = runtimeRequirements.map { it.toCore() },
        capabilityKeys = capabilityKeys,
        capabilityLabels = capabilityLabels.mapValues { (_, value) -> value.toCore() },
        notes = notes?.toCore(),
    )
}

private fun SdkThemePackage.toCore(): ThemePackage {
    return ThemePackage(
        extension = extension.toCore(),
        schemaVersion = schemaVersion,
        name = name.toCore(),
        author = author,
        version = version,
        description = description.toCore(),
        platforms = platforms.map { it.toCore() },
    )
}

private fun SdkThemePlatformFacet.toCore(): ThemePlatformFacet {
    return ThemePlatformFacet(
        target = target.toCore(),
        brightness = brightness.toCore(),
        tokens = tokens.toCore(),
        scene = scene.toCore(),
        launcherIcon = launcherIcon.toCore(),
    )
}

private fun SdkThemeColorTokens.toCore(): ThemeColorTokens {
    return ThemeColorTokens(
        primaryHex = primaryHex,
        secondaryHex = secondaryHex,
        tertiaryHex = tertiaryHex,
        backgroundHex = backgroundHex,
        surfaceHex = surfaceHex,
    )
}

private fun SdkThemeSceneManifest.toCore(): ThemeSceneManifest {
    return ThemeSceneManifest(
        kind = kind,
        supportsAnimation = supportsAnimation,
        previewAsset = previewAsset,
    )
}

private fun PageContext.toSdk(): SdkPageContext {
    return SdkPageContext(
        target = target.toSdk(),
        currentGame = currentGame?.toSdk(),
        currentTemplate = currentTemplate?.toSdk(),
        allGames = allGames.map { it.toSdk() },
        visibleTemplates = visibleTemplates.map { it.toSdk() },
        extensionPriorityEntries = extensionPriorityEntries.map { it.toSdk() },
        accounts = accounts.map { it.toSdk() },
        strings = strings.toSdk(),
        manageStorageAccessState = manageStorageAccessState?.toSdk(),
        directoryPickerState = directoryPickerState?.toSdk(),
        filePickerState = filePickerState?.toSdk(),
        gameLaunchState = gameLaunchState?.toSdk(),
        interceptPreparedLaunchRequest = { request, context ->
            interceptPreparedLaunchRequest(request?.toCore(), context.toCore())?.toSdk()
        },
        actionDispatcher = com.dreamyloong.tlauncher.sdk.action.LauncherActionDispatcher { action ->
            actionDispatcher.dispatch(action.toCore())
        },
        navigateToPage = navigateToPage,
        navigateBack = navigateBack,
        requestUiRefresh = requestUiRefresh,
    )
}

private fun SdkPageContributionBundle.toCore(): PageContributionBundle {
    return PageContributionBundle(
        sourceId = sourceId,
        page = page?.toCore(),
        nodes = nodes.map { it.toCore() },
        localizationBundle = localizationBundle?.toCore(),
    )
}

private fun SdkPageRegistration.toCore(): PageRegistration {
    return PageRegistration(
        id = id,
        sourceId = sourceId,
        title = title.toCore(),
        subtitle = subtitle?.toCore(),
        actionLabel = actionLabel?.toCore(),
        action = action,
        supportedTargets = supportedTargets.mapToSet { it.toCore() },
    )
}

private fun SdkPageNodeRegistration.toCore(): PageNodeRegistration {
    return when (this) {
        is SdkPageSectionRegistration -> PageSectionRegistration(
            nodeId = nodeId,
            pageId = pageId,
            parentNodeId = parentNodeId,
            sourceId = sourceId,
            orderHint = orderHint,
            supportedTargets = supportedTargets.mapToSet { it.toCore() },
            displayPolicy = displayPolicy.toCore(),
            placement = placement.toCore(),
            title = title?.toCore(),
            subtitle = subtitle?.toCore(),
        )

        is SdkPageWidgetRegistration -> PageWidgetRegistration(
            nodeId = nodeId,
            pageId = pageId,
            parentNodeId = parentNodeId,
            sourceId = sourceId,
            orderHint = orderHint,
            supportedTargets = supportedTargets.mapToSet { it.toCore() },
            displayPolicy = displayPolicy.toCore(),
            placement = placement.toCore(),
            footerLayout = footerLayout?.toCore(),
            widget = widget.toCore(),
        )
    }
}

private fun SdkPageDisplayPolicy.toCore(): PageDisplayPolicy {
    return PageDisplayPolicy(
        visibleWhen = { context -> visibleWhen(context.toSdk()) },
    )
}

private fun SdkPageTextRef.toCore(): PageTextRef {
    return when (this) {
        is SdkPageTextRef.Direct -> PageTextRef.Direct(value)
        is SdkPageTextRef.BundleKey -> PageTextRef.BundleKey(key)
    }
}

private fun PageTextRef.toSdk(): SdkPageTextRef {
    return when (this) {
        is PageTextRef.Direct -> SdkPageTextRef.Direct(value)
        is PageTextRef.BundleKey -> SdkPageTextRef.BundleKey(key)
    }
}

private fun SdkPageLocalizationBundle.toCore(): PageLocalizationBundle {
    return PageLocalizationBundle(
        sourceId = sourceId,
        filePaths = filePaths.mapKeys { (key, _) -> key.toCore() },
        entries = entries.mapKeys { (key, _) -> key.toCore() },
    )
}

private fun SdkPageWidgetRegistrationModel.toCore(): PageWidgetRegistrationModel {
    return when (this) {
        is SdkPageWidgetRegistrationModel.SummaryCard -> PageWidgetRegistrationModel.SummaryCard(
            title = title.toCore(),
            subtitle = subtitle.toCore(),
            tone = tone.toCore(),
            onClick = onClick,
        )

        is SdkPageWidgetRegistrationModel.ValueCard -> PageWidgetRegistrationModel.ValueCard(
            rows = rows.map { it.toCore() },
        )

        is SdkPageWidgetRegistrationModel.DetailCard -> PageWidgetRegistrationModel.DetailCard(
            title = title.toCore(),
            subtitle = subtitle?.toCore(),
            rows = rows.map { it.toCore() },
            actions = actions.map { it.toCore() },
            tone = tone.toCore(),
            enabled = enabled,
            onClick = onClick,
        )

        is SdkPageWidgetRegistrationModel.ProgressCard -> PageWidgetRegistrationModel.ProgressCard(
            title = title.toCore(),
            subtitle = subtitle?.toCore(),
            progress = progress.toCore(),
            tone = tone.toCore(),
        )

        is SdkPageWidgetRegistrationModel.ChoiceCard -> PageWidgetRegistrationModel.ChoiceCard(
            title = title.toCore(),
            subtitle = subtitle?.toCore(),
            options = options.map { it.toCore() },
            actions = actions.map { it.toCore() },
        )

        is SdkPageWidgetRegistrationModel.ToggleCard -> PageWidgetRegistrationModel.ToggleCard(
            title = title.toCore(),
            subtitle = subtitle?.toCore(),
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )

        is SdkPageWidgetRegistrationModel.ButtonStack -> PageWidgetRegistrationModel.ButtonStack(
            actions = actions.map { it.toCore() },
        )

        is SdkPageWidgetRegistrationModel.LaunchBar -> PageWidgetRegistrationModel.LaunchBar(
            title = title?.toCore(),
            subtitle = subtitle?.toCore(),
            primaryAction = primaryAction.toCore(),
            secondaryActions = secondaryActions.map { it.toCore() },
            tone = tone.toCore(),
        )

        is SdkPageWidgetRegistrationModel.AutoRefresh -> PageWidgetRegistrationModel.AutoRefresh(
            intervalMillis = intervalMillis,
            onRefresh = onRefresh,
        )

        is SdkPageWidgetRegistrationModel.TextInputCard -> PageWidgetRegistrationModel.TextInputCard(
            title = title.toCore(),
            value = value.toCore(),
            placeholder = placeholder?.toCore(),
            supportingText = supportingText?.toCore(),
            enabled = enabled,
            singleLine = singleLine,
            password = password,
            onValueChange = onValueChange,
        )

        is SdkPageWidgetRegistrationModel.DirectoryInputCard -> PageWidgetRegistrationModel.DirectoryInputCard(
            title = title.toCore(),
            value = value.toCore(),
            placeholder = placeholder?.toCore(),
            supportingText = supportingText?.toCore(),
            enabled = enabled,
            pickButtonLabel = pickButtonLabel?.toCore(),
            onValueChange = onValueChange,
            onPickDirectory = onPickDirectory,
        )
    }
}

private fun SdkPageValueItemRegistration.toCore(): PageValueItemRegistration {
    return PageValueItemRegistration(
        label = label.toCore(),
        value = value.toCore(),
    )
}

private fun SdkPageChoiceOptionRegistration.toCore(): PageChoiceOptionRegistration {
    return PageChoiceOptionRegistration(
        id = id,
        label = label.toCore(),
        selected = selected,
        enabled = enabled,
        onClick = onClick,
    )
}

private fun SdkPageActionRegistration.toCore(): PageActionRegistration {
    return PageActionRegistration(
        id = id,
        label = label.toCore(),
        compactLabel = compactLabel?.toCore(),
        style = style.toCore(),
        enabled = enabled,
        onClick = onClick,
    )
}

private fun SdkPageProgressRegistration.toCore(): PageProgressRegistration {
    return PageProgressRegistration(
        fraction = fraction,
        label = label?.toCore(),
        supportingText = supportingText?.toCore(),
    )
}

private fun SdkPageFooterLayoutRegistration.toCore(): PageFooterLayoutRegistration {
    return PageFooterLayoutRegistration(
        horizontalPaddingDp = horizontalPaddingDp,
        topPaddingDp = topPaddingDp,
        bottomPaddingDp = bottomPaddingDp,
    )
}

private fun PageFooterLayoutRegistration.toSdk(): SdkPageFooterLayoutRegistration {
    return SdkPageFooterLayoutRegistration(
        horizontalPaddingDp = horizontalPaddingDp,
        topPaddingDp = topPaddingDp,
        bottomPaddingDp = bottomPaddingDp,
    )
}

private fun ResolvedPage.toSdk(): SdkResolvedPage {
    return SdkResolvedPage(
        id = id,
        title = title,
        subtitle = subtitle,
        actionLabel = actionLabel,
        action = action,
        nodes = nodes.map { it.toSdk() },
        footerNodes = footerNodes.map { it.toSdk() as SdkResolvedPageNode.Widget },
    )
}

private fun SdkResolvedPage.toCore(): ResolvedPage {
    return ResolvedPage(
        id = id,
        title = title,
        subtitle = subtitle,
        actionLabel = actionLabel,
        action = action,
        nodes = nodes.map { it.toCore() },
        footerNodes = footerNodes.map { it.toCore() as ResolvedPageNode.Widget },
    )
}

private fun ResolvedPageNode.toSdk(): SdkResolvedPageNode {
    return when (this) {
        is ResolvedPageNode.Section -> SdkResolvedPageNode.Section(
            nodeId = nodeId,
            orderHint = orderHint,
            title = title,
            subtitle = subtitle,
            children = children.map { it.toSdk() },
        )

        is ResolvedPageNode.Widget -> SdkResolvedPageNode.Widget(
            nodeId = nodeId,
            orderHint = orderHint,
            footerLayout = footerLayout?.toSdk(),
            widget = widget.toSdk(),
        )
    }
}

private fun SdkResolvedPageNode.toCore(): ResolvedPageNode {
    return when (this) {
        is SdkResolvedPageNode.Section -> ResolvedPageNode.Section(
            nodeId = nodeId,
            orderHint = orderHint,
            title = title,
            subtitle = subtitle,
            children = children.map { it.toCore() },
        )

        is SdkResolvedPageNode.Widget -> ResolvedPageNode.Widget(
            nodeId = nodeId,
            orderHint = orderHint,
            footerLayout = footerLayout?.toCore(),
            widget = widget.toCore(),
        )
    }
}

private fun ResolvedPageWidget.toSdk(): SdkResolvedPageWidget {
    return when (this) {
        is ResolvedPageWidget.SummaryCard -> SdkResolvedPageWidget.SummaryCard(title, subtitle, tone.toSdk(), onClick)
        is ResolvedPageWidget.ValueCard -> SdkResolvedPageWidget.ValueCard(rows.map { it.toSdk() })
        is ResolvedPageWidget.DetailCard -> SdkResolvedPageWidget.DetailCard(title, subtitle, rows.map { it.toSdk() }, actions.map { it.toSdk() }, tone.toSdk(), enabled, onClick)
        is ResolvedPageWidget.ProgressCard -> SdkResolvedPageWidget.ProgressCard(title, subtitle, progress.toSdk(), tone.toSdk())
        is ResolvedPageWidget.ChoiceCard -> SdkResolvedPageWidget.ChoiceCard(title, subtitle, options.map { it.toSdk() }, actions.map { it.toSdk() })
        is ResolvedPageWidget.ToggleCard -> SdkResolvedPageWidget.ToggleCard(title, subtitle, checked, enabled, onCheckedChange)
        is ResolvedPageWidget.ButtonStack -> SdkResolvedPageWidget.ButtonStack(actions.map { it.toSdk() })
        is ResolvedPageWidget.LaunchBar -> SdkResolvedPageWidget.LaunchBar(title, subtitle, primaryAction.toSdk(), secondaryActions.map { it.toSdk() }, tone.toSdk())
        is ResolvedPageWidget.AutoRefresh -> SdkResolvedPageWidget.AutoRefresh(intervalMillis, onRefresh)
        is ResolvedPageWidget.TextInputCard -> SdkResolvedPageWidget.TextInputCard(title, value, placeholder, supportingText, enabled, singleLine, password, onValueChange)
        is ResolvedPageWidget.DirectoryInputCard -> SdkResolvedPageWidget.DirectoryInputCard(title, value, placeholder, supportingText, enabled, pickButtonLabel, onValueChange, onPickDirectory)
    }
}

private fun SdkResolvedPageWidget.toCore(): ResolvedPageWidget {
    return when (this) {
        is SdkResolvedPageWidget.SummaryCard -> ResolvedPageWidget.SummaryCard(title, subtitle, tone.toCore(), onClick)
        is SdkResolvedPageWidget.ValueCard -> ResolvedPageWidget.ValueCard(rows.map { it.toCore() })
        is SdkResolvedPageWidget.DetailCard -> ResolvedPageWidget.DetailCard(title, subtitle, rows.map { it.toCore() }, actions.map { it.toCore() }, tone.toCore(), enabled, onClick)
        is SdkResolvedPageWidget.ProgressCard -> ResolvedPageWidget.ProgressCard(title, subtitle, progress.toCore(), tone.toCore())
        is SdkResolvedPageWidget.ChoiceCard -> ResolvedPageWidget.ChoiceCard(title, subtitle, options.map { it.toCore() }, actions.map { it.toCore() })
        is SdkResolvedPageWidget.ToggleCard -> ResolvedPageWidget.ToggleCard(title, subtitle, checked, enabled, onCheckedChange)
        is SdkResolvedPageWidget.ButtonStack -> ResolvedPageWidget.ButtonStack(actions.map { it.toCore() })
        is SdkResolvedPageWidget.LaunchBar -> ResolvedPageWidget.LaunchBar(title, subtitle, primaryAction.toCore(), secondaryActions.map { it.toCore() }, tone.toCore())
        is SdkResolvedPageWidget.AutoRefresh -> ResolvedPageWidget.AutoRefresh(intervalMillis, onRefresh)
        is SdkResolvedPageWidget.TextInputCard -> ResolvedPageWidget.TextInputCard(title, value, placeholder, supportingText, enabled, singleLine, password, onValueChange)
        is SdkResolvedPageWidget.DirectoryInputCard -> ResolvedPageWidget.DirectoryInputCard(title, value, placeholder, supportingText, enabled, pickButtonLabel, onValueChange, onPickDirectory)
    }
}

private fun ResolvedPageValueItem.toSdk(): SdkResolvedPageValueItem = SdkResolvedPageValueItem(label, value)
private fun SdkResolvedPageValueItem.toCore(): ResolvedPageValueItem = ResolvedPageValueItem(label, value)
private fun ResolvedPageChoiceOption.toSdk(): SdkResolvedPageChoiceOption = SdkResolvedPageChoiceOption(id, label, selected, enabled, onClick)
private fun SdkResolvedPageChoiceOption.toCore(): ResolvedPageChoiceOption = ResolvedPageChoiceOption(id, label, selected, enabled, onClick)
private fun ResolvedPageAction.toSdk(): SdkResolvedPageAction = SdkResolvedPageAction(id, label, compactLabel, style.toSdk(), enabled, onClick)
private fun SdkResolvedPageAction.toCore(): ResolvedPageAction = ResolvedPageAction(id, label, compactLabel, style.toCore(), enabled, onClick)
private fun ResolvedPageProgress.toSdk(): SdkResolvedPageProgress = SdkResolvedPageProgress(fraction, label, supportingText)
private fun SdkResolvedPageProgress.toCore(): ResolvedPageProgress = ResolvedPageProgress(fraction, label, supportingText)

private fun SettingsSectionContext.toSdk(): SdkSettingsSectionContext {
    return SdkSettingsSectionContext(
        target = target.toSdk(),
        currentGame = currentGame?.toSdk(),
        currentTemplate = currentTemplate?.toSdk(),
        allGames = allGames.map { it.toSdk() },
        visibleTemplates = visibleTemplates.map { it.toSdk() },
        extensionPriorityEntries = extensionPriorityEntries.map { it.toSdk() },
        strings = strings.toSdk(),
        manageStorageAccessState = manageStorageAccessState?.toSdk(),
    )
}

private fun SdkSettingsContributionBundle.toCore(): SettingsContributionBundle {
    return SettingsContributionBundle(
        sourceId = sourceId,
        sections = sections.map { it.toCore() },
        components = components.map { it.toCore() },
        localizationBundle = localizationBundle?.toCore(),
    )
}

private fun SdkSettingsSectionRegistration.toCore(): SettingsSectionRegistration {
    return SettingsSectionRegistration(
        id = id,
        sourceId = sourceId,
        origin = origin.toCore(),
        title = title.toCore(),
        subtitle = subtitle?.toCore(),
    )
}

private fun SdkSettingsComponentRegistration.toCore(): SettingsComponentRegistration {
    return when (this) {
        is SdkSettingsComponentRegistration.Summary -> SettingsComponentRegistration.Summary(
            componentId = componentId,
            parentSectionId = parentSectionId,
            sourceId = sourceId,
            orderHint = orderHint,
            title = title.toCore(),
            subtitle = subtitle.toCore(),
        )

        is SdkSettingsComponentRegistration.Value -> SettingsComponentRegistration.Value(
            componentId = componentId,
            parentSectionId = parentSectionId,
            sourceId = sourceId,
            orderHint = orderHint,
            label = label.toCore(),
            value = value.toCore(),
        )

        is SdkSettingsComponentRegistration.Actions -> SettingsComponentRegistration.Actions(
            componentId = componentId,
            parentSectionId = parentSectionId,
            sourceId = sourceId,
            orderHint = orderHint,
            title = title?.toCore(),
            subtitle = subtitle?.toCore(),
            options = options.map { it.toCore() },
        )
    }
}

private fun SdkSettingsOptionRegistration.toCore(): SettingsOptionRegistration {
    return SettingsOptionRegistration(
        id = id,
        label = label.toCore(),
        selected = selected,
        onClick = onClick,
    )
}

private fun SdkSettingsTextRef.toCore(): SettingsTextRef {
    return when (this) {
        is SdkSettingsTextRef.Direct -> SettingsTextRef.Direct(value)
        is SdkSettingsTextRef.BundleKey -> SettingsTextRef.BundleKey(key)
    }
}

private fun SettingsTextRef.toSdk(): SdkSettingsTextRef {
    return when (this) {
        is SettingsTextRef.Direct -> SdkSettingsTextRef.Direct(value)
        is SettingsTextRef.BundleKey -> SdkSettingsTextRef.BundleKey(key)
    }
}

private fun SdkSettingsLocalizationBundle.toCore(): SettingsLocalizationBundle {
    return SettingsLocalizationBundle(
        sourceId = sourceId,
        filePaths = filePaths.mapKeys { (key, _) -> key.toCore() },
        entries = entries.mapKeys { (key, _) -> key.toCore() },
    )
}

private fun SettingsSection.toSdk(): SdkSettingsSection {
    return SdkSettingsSection(
        id = id,
        title = title,
        subtitle = subtitle,
        origin = origin.toSdk(),
        entries = entries.map { it.toSdk() },
        order = order,
    )
}

private fun SdkSettingsSection.toCore(): SettingsSection {
    return SettingsSection(
        id = id,
        title = title,
        subtitle = subtitle,
        origin = origin.toCore(),
        entries = entries.map { it.toCore() },
        order = order,
    )
}

private fun SettingsSectionEntry.toSdk(): SdkSettingsSectionEntry {
    return when (this) {
        is SettingsSectionEntry.Summary -> SdkSettingsSectionEntry.Summary(title, subtitle)
        is SettingsSectionEntry.Value -> SdkSettingsSectionEntry.Value(label, value)
        is SettingsSectionEntry.Actions -> SdkSettingsSectionEntry.Actions(title, subtitle, options.map { it.toSdk() })
    }
}

private fun SdkSettingsSectionEntry.toCore(): SettingsSectionEntry {
    return when (this) {
        is SdkSettingsSectionEntry.Summary -> SettingsSectionEntry.Summary(title, subtitle)
        is SdkSettingsSectionEntry.Value -> SettingsSectionEntry.Value(label, value)
        is SdkSettingsSectionEntry.Actions -> SettingsSectionEntry.Actions(title, subtitle, options.map { it.toCore() })
    }
}

private fun SettingsSectionOption.toSdk(): SdkSettingsSectionOption = SdkSettingsSectionOption(id, label, selected, onClick)
private fun SdkSettingsSectionOption.toCore(): SettingsSectionOption = SettingsSectionOption(id, label, selected, onClick)

private fun LauncherAction.toSdk(): SdkLauncherAction {
    return when (this) {
        is LauncherAction.OpenPage -> SdkLauncherAction.OpenPage(pageId)
        is LauncherAction.ReplaceCurrentPage -> SdkLauncherAction.ReplaceCurrentPage(pageId)
        is LauncherAction.OpenGameDetail -> SdkLauncherAction.OpenGameDetail(instanceId.toSdk())
        LauncherAction.LoadExtensionPackage -> SdkLauncherAction.LoadExtensionPackage
        is LauncherAction.DeleteExtensionPackage -> SdkLauncherAction.DeleteExtensionPackage(sourceName)
        LauncherAction.NavigateBack -> SdkLauncherAction.NavigateBack
        LauncherAction.Refresh -> SdkLauncherAction.Refresh
        is LauncherAction.OpenExternalUrl -> SdkLauncherAction.OpenExternalUrl(url)
        is LauncherAction.LaunchGame -> SdkLauncherAction.LaunchGame(request.toSdk())
        is LauncherAction.SelectCurrentGame -> SdkLauncherAction.SelectCurrentGame(instanceId.toSdk())
        is LauncherAction.CreateGameInstance -> SdkLauncherAction.CreateGameInstance(templatePackageId.toSdk(), displayName, description)
        is LauncherAction.UpdateGameInstanceDetails -> SdkLauncherAction.UpdateGameInstanceDetails(instanceId.toSdk(), displayName, description)
        is LauncherAction.DeleteGameInstance -> SdkLauncherAction.DeleteGameInstance(instanceId.toSdk())
        is LauncherAction.SetThemePreference -> SdkLauncherAction.SetThemePreference(preference.toSdk())
        is LauncherAction.ApplyLauncherIconAndClose -> SdkLauncherAction.ApplyLauncherIconAndClose(icon.toSdk())
        is LauncherAction.SetLanguagePreference -> SdkLauncherAction.SetLanguagePreference(preference.toSdk())
        is LauncherAction.ChangeExtensionPriority -> SdkLauncherAction.ChangeExtensionPriority(identityId, direction.toSdk())
        LauncherAction.CheckForUpdates -> SdkLauncherAction.CheckForUpdates
    }
}

private fun SdkLauncherAction.toCore(): LauncherAction {
    return when (this) {
        is SdkLauncherAction.OpenPage -> LauncherAction.OpenPage(pageId)
        is SdkLauncherAction.ReplaceCurrentPage -> LauncherAction.ReplaceCurrentPage(pageId)
        is SdkLauncherAction.OpenGameDetail -> LauncherAction.OpenGameDetail(instanceId.toCore())
        SdkLauncherAction.LoadExtensionPackage -> LauncherAction.LoadExtensionPackage
        is SdkLauncherAction.DeleteExtensionPackage -> LauncherAction.DeleteExtensionPackage(sourceName)
        SdkLauncherAction.NavigateBack -> LauncherAction.NavigateBack
        SdkLauncherAction.Refresh -> LauncherAction.Refresh
        is SdkLauncherAction.OpenExternalUrl -> LauncherAction.OpenExternalUrl(url)
        is SdkLauncherAction.LaunchGame -> LauncherAction.LaunchGame(request.toCore())
        is SdkLauncherAction.SelectCurrentGame -> LauncherAction.SelectCurrentGame(instanceId.toCore())
        is SdkLauncherAction.CreateGameInstance -> LauncherAction.CreateGameInstance(templatePackageId.toCore(), displayName, description)
        is SdkLauncherAction.UpdateGameInstanceDetails -> LauncherAction.UpdateGameInstanceDetails(instanceId.toCore(), displayName, description)
        is SdkLauncherAction.DeleteGameInstance -> LauncherAction.DeleteGameInstance(instanceId.toCore())
        is SdkLauncherAction.SetThemePreference -> LauncherAction.SetThemePreference(preference.toCore())
        is SdkLauncherAction.ApplyLauncherIconAndClose -> LauncherAction.ApplyLauncherIconAndClose(icon.toCore())
        is SdkLauncherAction.SetLanguagePreference -> LauncherAction.SetLanguagePreference(preference.toCore())
        is SdkLauncherAction.ChangeExtensionPriority -> LauncherAction.ChangeExtensionPriority(identityId, direction.toCore())
        SdkLauncherAction.CheckForUpdates -> LauncherAction.CheckForUpdates
    }
}

private fun LauncherActionContext.toSdk(): SdkLauncherActionContext {
    return SdkLauncherActionContext(
        target = target.toSdk(),
        currentGame = currentGame?.toSdk(),
        currentTemplate = currentTemplate?.toSdk(),
    )
}

private fun GameLaunchContext.toSdk(): SdkGameLaunchContext {
    return SdkGameLaunchContext(
        target = target.toSdk(),
        currentGame = currentGame?.toSdk(),
        currentTemplate = currentTemplate?.toSdk(),
    )
}

private fun GameLaunchRequest.toSdk(): SdkGameLaunchRequest {
    return when (this) {
        is GameLaunchRequest.AndroidRuntime -> SdkGameLaunchRequest.AndroidRuntime(
            gameInstanceId = gameInstanceId.toSdk(),
            gameDisplayName = gameDisplayName,
            templatePackageId = templatePackageId.toSdk(),
            projectDirectory = projectDirectory,
            packFileName = packFileName,
            launchContextJson = launchContextJson,
            launchContextFileName = launchContextFileName,
            nativeLibraryResourceDirectory = nativeLibraryResourceDirectory,
            dynamicJarResourcePaths = dynamicJarResourcePaths,
            runtimeBridgeClassName = runtimeBridgeClassName,
            hostProjectKey = hostProjectKey,
            classLoaderBackedNativeLibraryNames = classLoaderBackedNativeLibraryNames,
            nativeLibraryLoadOrder = nativeLibraryLoadOrder,
            hostNativeLibraryExcludes = hostNativeLibraryExcludes,
        )
    }
}

private fun SdkGameLaunchRequest.toCore(): GameLaunchRequest {
    return when (this) {
        is SdkGameLaunchRequest.AndroidRuntime -> GameLaunchRequest.AndroidRuntime(
            gameInstanceId = gameInstanceId.toCore(),
            gameDisplayName = gameDisplayName,
            templatePackageId = templatePackageId.toCore(),
            projectDirectory = projectDirectory,
            packFileName = packFileName,
            launchContextJson = launchContextJson,
            launchContextFileName = launchContextFileName,
            nativeLibraryResourceDirectory = nativeLibraryResourceDirectory,
            dynamicJarResourcePaths = dynamicJarResourcePaths,
            runtimeBridgeClassName = runtimeBridgeClassName,
            hostProjectKey = hostProjectKey,
            classLoaderBackedNativeLibraryNames = classLoaderBackedNativeLibraryNames,
            nativeLibraryLoadOrder = nativeLibraryLoadOrder,
            hostNativeLibraryExcludes = hostNativeLibraryExcludes,
        )
    }
}

private fun TemplateLaunchPreparationContext.toSdk(): SdkTemplateLaunchPreparationContext {
    return SdkTemplateLaunchPreparationContext(
        templatePackageId = templatePackageId.toSdk(),
        target = target.toSdk(),
        currentGame = currentGame?.toSdk(),
        selectedGameDirectory = selectedGameDirectory,
    )
}

private fun SdkTemplateLaunchPreparationContext.toCore(): TemplateLaunchPreparationContext {
    return TemplateLaunchPreparationContext(
        templatePackageId = templatePackageId.toCore(),
        target = target.toCore(),
        currentGame = currentGame?.toCore(),
        selectedGameDirectory = selectedGameDirectory,
    )
}

private fun ManageStorageAccessState.toSdk(): SdkManageStorageAccessState {
    return SdkManageStorageAccessState(
        isSupported = isSupported,
        isGranted = isGranted,
        requestAccess = requestAccess,
    )
}

private fun DirectoryPickerState.toSdk(): SdkDirectoryPickerState {
    return SdkDirectoryPickerState(
        isSupported = isSupported,
        pickDirectory = pickDirectory,
    )
}

private fun FilePickerState.toSdk(): SdkFilePickerState {
    return SdkFilePickerState(
        isSupported = isSupported,
        pickFile = { acceptedMimeTypes, onPicked ->
            pickFile(acceptedMimeTypes) { picked ->
                onPicked(picked?.toSdk())
            }
        },
    )
}

private fun PickedFile.toSdk(): SdkPickedFile = SdkPickedFile(name, bytes)

private fun GameLaunchState.toSdk(): SdkGameLaunchState {
    return SdkGameLaunchState(
        isSupported = isSupported,
        syncLaunchContext = { request -> syncLaunchContext(request?.toCore()) },
        launch = { request -> launch(request.toCore()) },
    )
}

private fun LocalizedText.toSdk(): SdkLocalizedText = SdkLocalizedText(zhCn, enUs)
private fun SdkLocalizedText.toCore(): LocalizedText = LocalizedText(zhCn, enUs)

private fun AppStrings.toSdk(): SdkAppStrings = SdkAppStrings(language.toSdk(), commonBack)

private fun LauncherAccount.toSdk(): SdkLauncherAccount {
    return SdkLauncherAccount(
        provider = provider.toSdk(),
        subjectId = subjectId,
        displayName = displayName,
        active = active,
        hasRefreshToken = hasRefreshToken,
        hasAccessToken = hasAccessToken,
        createdAtUnixSeconds = createdAtUnixSeconds,
        loginModes = loginModes.mapToSet { it.toSdk() },
    )
}

private fun SdkLauncherAccount.toCore(): LauncherAccount {
    return LauncherAccount(
        provider = provider.toCore(),
        subjectId = subjectId,
        displayName = displayName,
        active = active,
        hasRefreshToken = hasRefreshToken,
        hasAccessToken = hasAccessToken,
        createdAtUnixSeconds = createdAtUnixSeconds,
        loginModes = loginModes.mapToSet { it.toCore() },
    )
}

private fun SdkExtensionCompatibility.toCore(): ExtensionCompatibility {
    return ExtensionCompatibility(
        packageFormatVersion = packageFormatVersion,
        sdkApiVersion = sdkApiVersion,
        minSdkApiVersion = minSdkApiVersion,
        targetSdkApiVersion = targetSdkApiVersion,
    )
}

private fun ExtensionCompatibility.toSdk(): SdkExtensionCompatibility {
    return SdkExtensionCompatibility(
        packageFormatVersion = packageFormatVersion,
        sdkApiVersion = sdkApiVersion,
        minSdkApiVersion = minSdkApiVersion,
        targetSdkApiVersion = targetSdkApiVersion,
    )
}

private fun SdkExtensionCompatibilityResult.toCore(): ExtensionCompatibilityResult {
    return when (this) {
        is SdkExtensionCompatibilityResult.Compatible -> ExtensionCompatibilityResult.Compatible(
            issues = issues.map { it.toCore() },
        )

        is SdkExtensionCompatibilityResult.Incompatible -> ExtensionCompatibilityResult.Incompatible(
            issues = issues.map { it.toCore() },
        )
    }
}

private fun ExtensionCompatibilityResult.toSdk(): SdkExtensionCompatibilityResult {
    return when (this) {
        is ExtensionCompatibilityResult.Compatible -> SdkExtensionCompatibilityResult.Compatible(
            issues = issues.map { it.toSdk() },
        )

        is ExtensionCompatibilityResult.Incompatible -> SdkExtensionCompatibilityResult.Incompatible(
            issues = issues.map { it.toSdk() },
        )
    }
}

private fun SdkTemplateDescriptor.toCore(): TemplateDescriptor {
    return TemplateDescriptor(
        packageId = packageId.toCore(),
        name = name.toCore(),
        description = description.toCore(),
        defaultInstanceDescription = defaultInstanceDescription?.toCore(),
        schemaVersion = schemaVersion,
        sourceType = sourceType.toCore(),
        releaseState = releaseState.toCore(),
        notes = notes?.toCore(),
        platforms = platforms.map { it.toCore() },
    )
}

private fun TemplateDescriptor.toSdk(): SdkTemplateDescriptor {
    return SdkTemplateDescriptor(
        packageId = packageId.toSdk(),
        name = name.toSdk(),
        description = description.toSdk(),
        defaultInstanceDescription = defaultInstanceDescription?.toSdk(),
        schemaVersion = schemaVersion,
        sourceType = sourceType.toSdk(),
        releaseState = releaseState.toSdk(),
        notes = notes?.toSdk(),
        platforms = platforms.map { it.toSdk() },
    )
}

private fun TemplateTargetFacet.toSdk(): SdkTemplateTargetFacet {
    return SdkTemplateTargetFacet(
        target = target.toSdk(),
        supportLevel = supportLevel.toSdk(),
        runtimeRequirements = runtimeRequirements.map { it.toSdk() },
        capabilityKeys = capabilityKeys,
        capabilityLabels = capabilityLabels.mapValues { (_, value) -> value.toSdk() },
        notes = notes?.toSdk(),
    )
}

private fun SdkTemplateTargetFacet.toCore(): TemplateTargetFacet {
    return TemplateTargetFacet(
        target = target.toCore(),
        supportLevel = supportLevel.toCore(),
        runtimeRequirements = runtimeRequirements.map { it.toCore() },
        capabilityKeys = capabilityKeys,
        capabilityLabels = capabilityLabels.mapValues { (_, value) -> value.toCore() },
        notes = notes?.toCore(),
    )
}

private fun Template.toSdk(): SdkTemplate {
    return SdkTemplate(
        packageId = packageId.toSdk(),
        name = name.toSdk(),
        description = description.toSdk(),
        defaultInstanceDescription = defaultInstanceDescription?.toSdk(),
        target = target.toSdk(),
        supportedTargets = supportedTargets.mapToSet { it.toSdk() },
        runtimeRequirements = runtimeRequirements.map { it.toSdk() },
        capabilityKeys = capabilityKeys,
        capabilityLabels = capabilityLabels.mapValues { (_, value) -> value.toSdk() },
        supportLevel = supportLevel.toSdk(),
        schemaVersion = schemaVersion,
        sourceType = sourceType.toSdk(),
        releaseState = releaseState.toSdk(),
        notes = notes?.toSdk(),
    )
}

private fun SdkTemplate.toCore(): Template {
    return Template(
        packageId = packageId.toCore(),
        name = name.toCore(),
        description = description.toCore(),
        defaultInstanceDescription = defaultInstanceDescription?.toCore(),
        target = target.toCore(),
        supportedTargets = supportedTargets.mapToSet { it.toCore() },
        runtimeRequirements = runtimeRequirements.map { it.toCore() },
        capabilityKeys = capabilityKeys,
        capabilityLabels = capabilityLabels.mapValues { (_, value) -> value.toCore() },
        supportLevel = supportLevel.toCore(),
        schemaVersion = schemaVersion,
        sourceType = sourceType.toCore(),
        releaseState = releaseState.toCore(),
        notes = notes?.toCore(),
    )
}

private fun GameInstance.toSdk(): SdkGameInstance {
    return SdkGameInstance(
        id = id.toSdk(),
        templatePackageId = templatePackageId.toSdk(),
        displayName = displayName,
        description = description,
        currentVersion = currentVersion,
        installPath = installPath,
    )
}

private fun SdkGameInstance.toCore(): GameInstance {
    return GameInstance(
        id = id.toCore(),
        templatePackageId = templatePackageId.toCore(),
        displayName = displayName,
        description = description,
        currentVersion = currentVersion,
        installPath = installPath,
    )
}

private fun RuntimeRequirement.toSdk(): SdkRuntimeRequirement = SdkRuntimeRequirement(engine, language, version)
private fun SdkRuntimeRequirement.toCore(): RuntimeRequirement = RuntimeRequirement(engine, language, version)
private fun ExtensionIdentityId.toSdk(): SdkExtensionIdentityId = SdkExtensionIdentityId(value)
private fun SdkExtensionIdentityId.toCore(): ExtensionIdentityId = ExtensionIdentityId(value)
private fun GameInstanceId.toSdk(): SdkGameInstanceId = SdkGameInstanceId(value)
private fun SdkGameInstanceId.toCore(): GameInstanceId = GameInstanceId(value)

private fun HostGrant.toSdk(): SdkHostGrant = SdkHostGrant(permissionKey.toSdk(), state.toSdk(), reason)
private fun SdkHostGrant.toCore(): HostGrant = HostGrant(permissionKey.toCore(), state.toCore(), reason)

private fun LanguagePreference.toSdk(): SdkLanguagePreference {
    return when (this) {
        LanguagePreference.FollowSystem -> SdkLanguagePreference.FollowSystem
        is LanguagePreference.Fixed -> SdkLanguagePreference.Fixed(language.toSdk())
    }
}

private fun SdkLanguagePreference.toCore(): LanguagePreference {
    return when (this) {
        SdkLanguagePreference.FollowSystem -> LanguagePreference.FollowSystem
        is SdkLanguagePreference.Fixed -> LanguagePreference.Fixed(language.toCore())
    }
}

private fun ThemePreference.toSdk(): SdkThemePreference {
    return when (this) {
        ThemePreference.FollowSystem -> SdkThemePreference.FollowSystem
        is ThemePreference.Fixed -> SdkThemePreference.Fixed(themeId.toSdk())
    }
}

private fun SdkThemePreference.toCore(): ThemePreference {
    return when (this) {
        SdkThemePreference.FollowSystem -> ThemePreference.FollowSystem
        is SdkThemePreference.Fixed -> ThemePreference.Fixed(themeId.toCore())
    }
}

private fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> {
    return map(transform).toSet()
}

private fun SdkExtensionKind.toCore(): ExtensionKind = ExtensionKind.valueOf(name)
private fun ExtensionKind.toSdk(): SdkExtensionKind = SdkExtensionKind.valueOf(name)
private fun SdkPlatformTarget.toCore(): PlatformTarget = PlatformTarget.valueOf(name)
private fun PlatformTarget.toSdk(): SdkPlatformTarget = SdkPlatformTarget.valueOf(name)
private fun SdkLaunchSupportLevel.toCore(): LaunchSupportLevel = LaunchSupportLevel.valueOf(name)
private fun LaunchSupportLevel.toSdk(): SdkLaunchSupportLevel = SdkLaunchSupportLevel.valueOf(name)
private fun SdkTemplateSourceType.toCore(): TemplateSourceType = TemplateSourceType.valueOf(name)
private fun TemplateSourceType.toSdk(): SdkTemplateSourceType = SdkTemplateSourceType.valueOf(name)
private fun SdkTemplateReleaseState.toCore(): TemplateReleaseState = TemplateReleaseState.valueOf(name)
private fun TemplateReleaseState.toSdk(): SdkTemplateReleaseState = SdkTemplateReleaseState.valueOf(name)
private fun SdkExtensionCapability.toCore(): ExtensionCapability = ExtensionCapability.valueOf(name)
private fun ExtensionCapability.toSdk(): SdkExtensionCapability = SdkExtensionCapability.valueOf(name)
private fun SdkHostPermissionKey.toCore(): HostPermissionKey = HostPermissionKey.valueOf(name)
private fun HostPermissionKey.toSdk(): SdkHostPermissionKey = SdkHostPermissionKey.valueOf(name)
private fun SdkHostGrantState.toCore(): HostGrantState = HostGrantState.valueOf(name)
private fun HostGrantState.toSdk(): SdkHostGrantState = SdkHostGrantState.valueOf(name)
private fun SdkExtensionCompatibilityIssue.toCore(): ExtensionCompatibilityIssue = ExtensionCompatibilityIssue.valueOf(name)
private fun ExtensionCompatibilityIssue.toSdk(): SdkExtensionCompatibilityIssue = SdkExtensionCompatibilityIssue.valueOf(name)
private fun SdkSupportedLanguage.toCore(): SupportedLanguage = SupportedLanguage.valueOf(name)
private fun SupportedLanguage.toSdk(): SdkSupportedLanguage = SdkSupportedLanguage.valueOf(name)
private fun SdkLauncherAccountProvider.toCore(): LauncherAccountProvider = LauncherAccountProvider.valueOf(name)
private fun LauncherAccountProvider.toSdk(): SdkLauncherAccountProvider = SdkLauncherAccountProvider.valueOf(name)
private fun SdkSteamAccountLoginMode.toCore(): SteamAccountLoginMode = SteamAccountLoginMode.valueOf(name)
private fun SteamAccountLoginMode.toSdk(): SdkSteamAccountLoginMode = SdkSteamAccountLoginMode.valueOf(name)
private fun SdkPageNodePlacement.toCore(): PageNodePlacement = PageNodePlacement.valueOf(name)
private fun SdkPageWidgetTone.toCore(): PageWidgetTone = PageWidgetTone.valueOf(name)
private fun PageWidgetTone.toSdk(): SdkPageWidgetTone = SdkPageWidgetTone.valueOf(name)
private fun SdkPageActionStyle.toCore(): PageActionStyle = PageActionStyle.valueOf(name)
private fun PageActionStyle.toSdk(): SdkPageActionStyle = SdkPageActionStyle.valueOf(name)
private fun SdkSettingsSectionOrigin.toCore(): SettingsSectionOrigin = SettingsSectionOrigin.valueOf(name)
private fun SettingsSectionOrigin.toSdk(): SdkSettingsSectionOrigin = SdkSettingsSectionOrigin.valueOf(name)
private fun SdkExtensionPriorityDirection.toCore(): ExtensionPriorityDirection = ExtensionPriorityDirection.valueOf(name)
private fun ExtensionPriorityDirection.toSdk(): SdkExtensionPriorityDirection = SdkExtensionPriorityDirection.valueOf(name)
private fun SdkThemeBrightness.toCore(): ThemeBrightness = ThemeBrightness.valueOf(name)
private fun SdkThemeLauncherIcon.toCore(): ThemeLauncherIcon = ThemeLauncherIcon.valueOf(name)
private fun ThemeLauncherIcon.toSdk(): SdkThemeLauncherIcon = SdkThemeLauncherIcon.valueOf(name)
