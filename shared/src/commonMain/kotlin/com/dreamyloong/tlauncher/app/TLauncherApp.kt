package com.dreamyloong.tlauncher.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dreamyloong.tlauncher.core.action.LauncherAction
import com.dreamyloong.tlauncher.core.action.LauncherActionContext
import com.dreamyloong.tlauncher.core.action.LauncherActionDispatcher
import com.dreamyloong.tlauncher.core.action.ExtensionPriorityDirection
import com.dreamyloong.tlauncher.core.action.dispatchLauncherAction
import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginResult
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginStatus
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.LauncherAccountService
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginRequest
import com.dreamyloong.tlauncher.core.account.UnsupportedLauncherAccountService
import com.dreamyloong.tlauncher.core.extension.DefaultExtensionPackageParser
import com.dreamyloong.tlauncher.core.extension.ExtensionHostPaths
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageParser
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource
import com.dreamyloong.tlauncher.core.extension.ExtensionSdkContract
import com.dreamyloong.tlauncher.core.extension.ExtensionFeaturePolicy
import com.dreamyloong.tlauncher.core.extension.HostGrantState
import com.dreamyloong.tlauncher.core.extension.InstalledTExtensionExtension
import com.dreamyloong.tlauncher.core.extension.LauncherExtension
import com.dreamyloong.tlauncher.core.extension.ExtensionRegistry
import com.dreamyloong.tlauncher.core.extension.ExtensionRuntimeLoader
import com.dreamyloong.tlauncher.core.extension.ParsedExtensionPackage
import com.dreamyloong.tlauncher.core.extension.TExtensionPackageFormat
import com.dreamyloong.tlauncher.core.i18n.DefaultLanguageResolver
import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.i18n.ProvideAppStrings
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.i18n.appStringsFor
import com.dreamyloong.tlauncher.core.i18n.platformLanguageTag
import com.dreamyloong.tlauncher.core.model.ExtensionDescriptor
import com.dreamyloong.tlauncher.core.model.ExtensionPackageScanProblem
import com.dreamyloong.tlauncher.core.model.ExtensionPermissionReview
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.GameInstanceId
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.page.HostedLauncherPage
import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageResolver
import com.dreamyloong.tlauncher.core.page.RenderLauncherPage
import com.dreamyloong.tlauncher.core.page.ResolvedPage
import com.dreamyloong.tlauncher.core.platform.BindLauncherIcon
import com.dreamyloong.tlauncher.core.platform.BindSystemBackHandler
import com.dreamyloong.tlauncher.core.platform.GameLaunchContext
import com.dreamyloong.tlauncher.core.platform.GameLaunchState
import com.dreamyloong.tlauncher.core.platform.InstalledExtensionPackageStore
import com.dreamyloong.tlauncher.core.platform.LauncherIconController
import com.dreamyloong.tlauncher.core.platform.dispatchGameLaunch
import com.dreamyloong.tlauncher.core.platform.launcherAppFilesDirectoryPath
import com.dreamyloong.tlauncher.core.platform.launcherPackageName
import com.dreamyloong.tlauncher.core.platform.launcherAppVersionName
import com.dreamyloong.tlauncher.core.platform.launcherStorageDirectoryPath
import com.dreamyloong.tlauncher.core.platform.openExternalUrl
import com.dreamyloong.tlauncher.core.platform.rememberExtensionPackagePickerState
import com.dreamyloong.tlauncher.core.platform.rememberFilePickerState
import com.dreamyloong.tlauncher.core.platform.rememberInstalledExtensionPackageStore
import com.dreamyloong.tlauncher.core.platform.rememberDirectoryPickerState
import com.dreamyloong.tlauncher.core.platform.toLauncherIconSource
import com.dreamyloong.tlauncher.core.theme.BuiltinThemeIds
import com.dreamyloong.tlauncher.core.theme.DefaultThemeResolver
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import com.dreamyloong.tlauncher.core.theme.ThemePreference
import com.dreamyloong.tlauncher.core.theme.builtInThemeDefinitions
import com.dreamyloong.tlauncher.core.template.DefaultCompatibilityEvaluator
import com.dreamyloong.tlauncher.core.template.TemplateRegistry
import com.dreamyloong.tlauncher.core.template.toTemplateDescriptor
import com.dreamyloong.tlauncher.core.theme.toThemeDefinition
import com.dreamyloong.tlauncher.core.update.AppUpdateCheckState
import com.dreamyloong.tlauncher.core.update.AppUpdateCheckStatus
import com.dreamyloong.tlauncher.core.update.GithubReleaseUpdateChecker
import com.dreamyloong.tlauncher.data.i18n.PersistentLanguageSettingsStore
import com.dreamyloong.tlauncher.data.library.PersistentGameLibraryService
import com.dreamyloong.tlauncher.data.persistence.LauncherStateRepository
import com.dreamyloong.tlauncher.data.plugin.LauncherExtensionContext
import com.dreamyloong.tlauncher.data.plugin.PersistentExtensionStateStores
import com.dreamyloong.tlauncher.data.plugin.RuntimeExtensionRegistry
import com.dreamyloong.tlauncher.core.plugin.ActionInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.LaunchInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.TemplateProviderExtension
import com.dreamyloong.tlauncher.core.plugin.ThemeProviderExtension
import com.dreamyloong.tlauncher.core.extension.RegisteredExtensionFeature
import com.dreamyloong.tlauncher.core.plugin.TemplateLaunchPreparationInterceptorExtension
import com.dreamyloong.tlauncher.core.template.dispatchTemplateLaunchPreparation
import com.dreamyloong.tlauncher.data.settings.PersistentExtensionPriorityStore
import com.dreamyloong.tlauncher.data.settings.PersistentExtensionHostGrantStore
import com.dreamyloong.tlauncher.data.settings.PersistentInstalledExtensionStateStore
import com.dreamyloong.tlauncher.data.theme.PersistentThemeSettingsStore
import com.dreamyloong.tlauncher.feature.create_game.CreateGamePage
import com.dreamyloong.tlauncher.feature.game_detail.GameDetailPage
import com.dreamyloong.tlauncher.feature.home.HomePage
import com.dreamyloong.tlauncher.feature.library.LibraryPage
import com.dreamyloong.tlauncher.feature.settings.AccountManagerPage
import com.dreamyloong.tlauncher.feature.settings.AddAccountPage
import com.dreamyloong.tlauncher.feature.navigation.TLauncherScreen
import com.dreamyloong.tlauncher.feature.settings.AboutPage
import com.dreamyloong.tlauncher.feature.settings.ExtensionDetailPage
import com.dreamyloong.tlauncher.feature.settings.ExtensionManagerPage
import com.dreamyloong.tlauncher.feature.settings.SettingsPage
import com.dreamyloong.tlauncher.ui.theme.TLauncherTheme
import com.dreamyloong.tlauncher.ui.theme.rememberSystemDarkTheme
import com.dreamyloong.tlauncher.sdk.host.EmptyExtensionHostServices
import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices
import kotlinx.coroutines.launch

private data class PageMotionTarget(
    val screenKey: String,
    val backStackSize: Int,
)

private enum class AndroidLauncherIconSelection {
    FOLLOW_THEME,
    DEFAULT,
    NIGHT,
}

@Composable
fun TLauncherApp(
    target: PlatformTarget,
    gameLaunchState: GameLaunchState? = null,
    accountService: LauncherAccountService = UnsupportedLauncherAccountService,
    hostServices: ExtensionHostServices = EmptyExtensionHostServices,
    externalRefreshTick: Int = 0,
) {
    val launcherStateRepository = remember { LauncherStateRepository() }
    val extensionStateStores = remember { PersistentExtensionStateStores(launcherStateRepository) }
    val extensionHostPaths = remember {
        ExtensionHostPaths(
            appFilesDirectoryPath = launcherAppFilesDirectoryPath(),
            launcherStorageDirectoryPath = launcherStorageDirectoryPath(),
            packageName = launcherPackageName(),
        )
    }
    val installedTExtensionExtensions = remember { mutableStateListOf<LauncherExtension>() }
    val extensionRegistry = remember(hostServices) {
        RuntimeExtensionRegistry(
            extensionContext = LauncherExtensionContext(
                hostPaths = extensionHostPaths,
                hostServices = hostServices,
            ),
            installedExtensions = installedTExtensionExtensions,
            extensionStateStoreFactory = extensionStateStores::storeFor,
        )
    }
    val templateRegistry = remember(extensionRegistry) {
        object : TemplateRegistry {
            override fun allTemplates() = collectTemplatePackages(
                registeredFeatures = extensionRegistry.registeredFeatures(),
                extensionRegistry = extensionRegistry,
            )
                .map { pkg -> pkg.toTemplateDescriptor() }
        }
    }
    val themeSettingsStore = remember { PersistentThemeSettingsStore(launcherStateRepository) }
    val themeResolver = remember { DefaultThemeResolver() }
    val languageSettingsStore = remember { PersistentLanguageSettingsStore(launcherStateRepository) }
    val languageResolver = remember { DefaultLanguageResolver() }
    val manageStorageAccessState = com.dreamyloong.tlauncher.core.platform.rememberManageStorageAccessState()
    val directoryPickerState = rememberDirectoryPickerState()
    val filePickerState = rememberFilePickerState()
    val extensionPackagePickerState = rememberExtensionPackagePickerState()
    val installedExtensionPackageStore = rememberInstalledExtensionPackageStore()
    val extensionPackageParser = remember { DefaultExtensionPackageParser }
    val hostSdk = remember { ExtensionSdkContract.hostDescriptor() }
    val pageResolver = remember(extensionRegistry) { PageResolver(extensionRegistry = extensionRegistry) }
    val currentVersionName = remember { launcherAppVersionName() }
    val updateChecker = remember { GithubReleaseUpdateChecker(owner = "dreamyloong2005", repository = "TLauncher-Compose") }
    val coroutineScope = rememberCoroutineScope()
    val extensionPriorityStore = remember { PersistentExtensionPriorityStore(launcherStateRepository) }
    val extensionHostGrantStore = remember { PersistentExtensionHostGrantStore(launcherStateRepository) }
    val installedExtensionStateStore = remember { PersistentInstalledExtensionStateStore(launcherStateRepository) }
    val library = remember {
        PersistentGameLibraryService(
            templateRegistry = templateRegistry,
            compatibilityEvaluator = DefaultCompatibilityEvaluator(),
            stateRepository = launcherStateRepository,
        )
    }

    val backStack = rememberSaveable(
        saver = listSaver<SnapshotStateList<TLauncherScreen>, String>(
            save = { stack -> stack.map(TLauncherScreen::toSaveableToken) },
            restore = { tokens ->
                mutableStateListOf<TLauncherScreen>().apply {
                    addAll(tokens.mapNotNull(String::toTLauncherScreen).ifEmpty { listOf(TLauncherScreen.Home) })
                }
            },
        ),
    ) { mutableStateListOf(TLauncherScreen.Home) }
    val createGameNameDrafts = remember { mutableStateMapOf<String, String>() }
    val createGameDescriptionDrafts = remember { mutableStateMapOf<String, String>() }
    var selectedCreateTemplatePackageId by remember { mutableStateOf<String?>(null) }
    val detailGameNameDrafts = remember { mutableStateMapOf<String, String>() }
    val detailGameDescriptionDrafts = remember { mutableStateMapOf<String, String>() }
    var uiRefreshTick by remember { mutableIntStateOf(0) }
    var pageRefreshTick by remember { mutableIntStateOf(0) }
    var extensionScanTick by remember { mutableIntStateOf(0) }
    var themePreference by remember { mutableStateOf(themeSettingsStore.currentPreference()) }
    var androidLauncherIconSelection by remember { mutableStateOf(AndroidLauncherIconSelection.FOLLOW_THEME) }
    var languagePreference by remember { mutableStateOf(languageSettingsStore.currentPreference()) }
    var updateCheckState by remember { mutableStateOf(AppUpdateCheckState()) }
    var extensionLoadStatusMessage by remember { mutableStateOf<String?>(null) }
    var discoveredTExtensionPackages by remember { mutableStateOf<List<DiscoveredTExtensionPackage>>(emptyList()) }
    var extensionPermissionReviews by remember { mutableStateOf<List<ExtensionPermissionReview>>(emptyList()) }
    var extensionPackageScanProblems by remember { mutableStateOf<List<ExtensionPackageScanProblem>>(emptyList()) }
    var pendingExtensionPackageReplacement by remember {
        mutableStateOf<ExtensionPackageReplacement?>(null)
    }
    var accounts by remember { mutableStateOf<List<LauncherAccount>>(emptyList()) }
    var accountLoadInProgress by remember { mutableStateOf(false) }
    var accountMutationInProgress by remember { mutableStateOf(false) }
    var accountManagerStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingAccountDeletion by remember { mutableStateOf<LauncherAccount?>(null) }
    var selectedAddAccountProvider by remember { mutableStateOf(LauncherAccountProvider.STEAM) }
    var selectedSteamLoginMode by remember { mutableStateOf(SteamAccountLoginMode.CM) }
    var steamAccountNameDraft by remember { mutableStateOf("") }
    var steamPasswordDraft by remember { mutableStateOf("") }
    var steamGuardCodeDraft by remember { mutableStateOf("") }
    var accountLoginInProgress by remember { mutableStateOf(false) }
    var addAccountLoginStatus by remember { mutableStateOf<LauncherAccountLoginStatus?>(null) }
    var addAccountStatusMessage by remember { mutableStateOf<String?>(null) }
    var activeAddAccountLoginAttemptId by remember { mutableStateOf<Long?>(null) }
    var nextAddAccountLoginAttemptId by remember { mutableStateOf(0L) }
    var pendingAddAccountExitConfirmation by remember { mutableStateOf(false) }
    val canceledAddAccountLoginKnownSubjects = remember { mutableMapOf<Long, Set<String>>() }

    val screen = backStack.last()
    val pageMotionPages = remember { linkedMapOf<PageMotionTarget, ResolvedPage>() }

    fun refreshUi() {
        pageMotionPages.clear()
        uiRefreshTick += 1
    }

    fun refreshCurrentPage() {
        pageMotionPages.remove(
            PageMotionTarget(
                screenKey = screen.toString(),
                backStackSize = backStack.size,
            ),
        )
        pageRefreshTick += 1
    }

    fun refreshExtensions() {
        extensionScanTick += 1
        refreshUi()
    }

    LaunchedEffect(externalRefreshTick) {
        if (externalRefreshTick > 0) {
            refreshCurrentPage()
        }
    }

    fun navigateTo(destination: TLauncherScreen) {
        if (backStack.lastOrNull() != destination) {
            backStack += destination
        }
    }

    fun navigateToPage(pageId: String) {
        navigateTo(screenForPageId(pageId))
    }

    fun replaceCurrentWith(destination: TLauncherScreen) {
        when {
            backStack.isEmpty() -> backStack += destination
            backStack.size > 1 && backStack[backStack.lastIndex - 1] == destination ->
                backStack.removeAt(backStack.lastIndex)
            else -> backStack[backStack.lastIndex] = destination
        }
    }

    fun replaceCurrentPage(pageId: String) {
        replaceCurrentWith(screenForPageId(pageId))
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popBackOr(fallback: TLauncherScreen) {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        } else {
            backStack.clear()
            backStack += fallback
        }
    }

    fun checkForUpdates() {
        if (updateCheckState.status == AppUpdateCheckStatus.Checking) {
            return
        }
        updateCheckState = AppUpdateCheckState(status = AppUpdateCheckStatus.Checking)
        coroutineScope.launch {
            updateCheckState = updateChecker.check(currentVersionName)
        }
    }

    fun refreshAccounts() {
        if (!accountService.isSupported || accountLoadInProgress) {
            return
        }
        accountLoadInProgress = true
        coroutineScope.launch {
            runCatching { accountService.listAccounts() }
                .onSuccess { loadedAccounts ->
                    accounts = loadedAccounts
                    accountManagerStatusMessage = null
                }
                .onFailure { error ->
                    accountManagerStatusMessage = error.message ?: "Failed to load accounts."
                }
            accountLoadInProgress = false
        }
    }

    val installedExtensionSnapshot = installedTExtensionExtensions.toList()
    val games = remember(uiRefreshTick) { library.instances() }
    val currentGame = remember(uiRefreshTick) { library.currentInstance() }
    val compatibleTemplates = remember(uiRefreshTick, target, installedExtensionSnapshot) { library.compatibleTemplates(target) }
    LaunchedEffect(compatibleTemplates, selectedCreateTemplatePackageId) {
        if (selectedCreateTemplatePackageId != null && compatibleTemplates.none { it.packageId.value == selectedCreateTemplatePackageId }) {
            selectedCreateTemplatePackageId = null
        }
    }
    val systemDark = rememberSystemDarkTheme()
    val systemLanguageTag = remember { platformLanguageTag() }
    val resolvedLanguage = remember(languagePreference, systemLanguageTag) {
        languageResolver.resolve(
            preference = languagePreference,
            systemTag = systemLanguageTag,
        )
    }
    val strings = remember(resolvedLanguage) { appStringsFor(resolvedLanguage) }
    LaunchedEffect(accountService) {
        if (accountService.isSupported) {
            refreshAccounts()
        }
    }

    fun knownSteamSubjectIds(): Set<String> {
        return accounts
            .asSequence()
            .filter { account -> account.provider == LauncherAccountProvider.STEAM }
            .map(LauncherAccount::subjectId)
            .toSet()
    }

    suspend fun rollbackCanceledAddAccountLoginIfNeeded(
        attemptId: Long,
        result: LauncherAccountLoginResult,
    ) {
        val knownSubjects = canceledAddAccountLoginKnownSubjects.remove(attemptId) ?: return
        val account = result.account ?: return
        if (result.status != LauncherAccountLoginStatus.SUCCEEDED || account.provider != LauncherAccountProvider.STEAM) {
            return
        }
        if (account.subjectId in knownSubjects) {
            return
        }
        runCatching {
            accountService.deleteAccount(account.provider, account.subjectId)
            accountService.listAccounts()
        }.onSuccess { loadedAccounts ->
            accounts = loadedAccounts
            accountManagerStatusMessage = strings.addAccountLoginCanceledMessage()
        }
    }

    fun clearVisibleAddAccountLoginAttempt(attemptId: Long) {
        if (activeAddAccountLoginAttemptId == attemptId) {
            activeAddAccountLoginAttemptId = null
            accountLoginInProgress = false
        }
    }

    fun cancelVisibleAddAccountLogin(
        navigateAway: Boolean,
    ) {
        val attemptId = activeAddAccountLoginAttemptId
        if (attemptId != null) {
            canceledAddAccountLoginKnownSubjects[attemptId] = knownSteamSubjectIds()
            coroutineScope.launch {
                runCatching { accountService.cancelSteamLogin() }
            }
        }
        activeAddAccountLoginAttemptId = null
        accountLoginInProgress = false
        addAccountLoginStatus = LauncherAccountLoginStatus.CANCELED
        addAccountStatusMessage = strings.addAccountLoginCanceledMessage()
        pendingAddAccountExitConfirmation = false
        if (navigateAway) {
            navigateBack()
        }
    }

    fun accountDisplayName(account: LauncherAccount): String {
        return account.displayName?.ifBlank { null }
            ?: account.subjectId.ifBlank { strings.accountManagerUnnamedAccount() }
    }

    fun deleteAccount(account: LauncherAccount) {
        if (!accountService.isSupported || accountMutationInProgress) {
            return
        }
        accountMutationInProgress = true
        coroutineScope.launch {
            val displayName = accountDisplayName(account)
            runCatching {
                accountService.deleteAccount(account.provider, account.subjectId)
                accountService.listAccounts()
            }.onSuccess { loadedAccounts ->
                accounts = loadedAccounts
                accountManagerStatusMessage = strings.accountManagerDeleteAccountSucceeded(displayName)
            }.onFailure { error ->
                accountManagerStatusMessage = buildString {
                    append(strings.accountManagerDeleteAccountFailed(displayName))
                    error.message?.takeIf { it.isNotBlank() }?.let { message ->
                        append(": ")
                        append(message)
                    }
                }
            }
            accountMutationInProgress = false
        }
    }

    fun resolvedInstanceDescription(game: GameInstance, template: Template?): String {
        return game.description.ifBlank {
            template?.defaultInstanceDescription?.resolve(strings.language)
                ?: template?.description?.resolve(strings.language)
                ?: ""
        }
    }
    LaunchedEffect(
        extensionScanTick,
        target,
        installedExtensionPackageStore.isSupported,
    ) {
        if (!installedExtensionPackageStore.isSupported) {
            installedTExtensionExtensions.clear()
            discoveredTExtensionPackages = emptyList()
            extensionPermissionReviews = emptyList()
            extensionPackageScanProblems = emptyList()
            return@LaunchedEffect
        }
        val knownIdentityIds = installedExtensionPackageStore.listInstalledPackages().mapNotNull { source ->
            runCatching { extensionPackageParser.parse(source).manifest.extension.identityId }.getOrNull()
        }
        installedExtensionStateStore.syncKnownExtensions(knownIdentityIds)
        extensionHostGrantStore.syncKnownExtensions(knownIdentityIds)
        val scanResult = scanInstalledExtensionPackages(
            target = target,
            packageSources = installedExtensionPackageStore.listInstalledPackages(),
            parser = extensionPackageParser,
            extensionHostGrantStore = extensionHostGrantStore,
            installedExtensionStateStore = installedExtensionStateStore,
        )
        installedTExtensionExtensions.clear()
        installedTExtensionExtensions.addAll(scanResult.loadedExtensions)
        discoveredTExtensionPackages = scanResult.discoveredPackages
        extensionPermissionReviews = scanResult.permissionReviews
        extensionPackageScanProblems = scanResult.scanProblems
    }
    val registeredExtensionFeatures = remember(installedExtensionSnapshot) {
        extensionRegistry.registeredFeatures()
    }
    val templatePackages = remember(registeredExtensionFeatures) {
        collectTemplatePackages(
            registeredFeatures = registeredExtensionFeatures,
            extensionRegistry = extensionRegistry,
        )
    }
    val themePackages = remember(registeredExtensionFeatures) {
        collectThemePackages(
            registeredFeatures = registeredExtensionFeatures,
            extensionRegistry = extensionRegistry,
        )
    }
    val allTemplates = remember(templatePackages) { templatePackages.map { pkg -> pkg.toTemplateDescriptor() } }
    val allThemes = remember(themePackages, target) {
        val extensionThemes = themePackages
            .map { pkg -> pkg.toThemeDefinition(target) }
            .filterNot { theme -> theme.id in BuiltinThemeIds.all }
        builtInThemeDefinitions(target) + extensionThemes
    }
    LaunchedEffect(themePreference, allThemes) {
        val fixedPreference = themePreference as? ThemePreference.Fixed ?: return@LaunchedEffect
        if (allThemes.none { theme -> theme.id == fixedPreference.themeId }) {
            themeSettingsStore.setCurrentPreference(ThemePreference.FollowSystem)
            themePreference = ThemePreference.FollowSystem
        }
    }
    val resolvedTheme = remember(themePreference, systemDark, allThemes) {
        themeResolver.resolve(
            preference = themePreference,
            systemDark = systemDark,
            availableThemes = allThemes,
        )
    }
    BindLauncherIcon(icon = resolvedTheme.launcherIcon)
    val selectedAndroidLauncherIcon = remember(androidLauncherIconSelection, resolvedTheme.launcherIcon) {
        when (androidLauncherIconSelection) {
            AndroidLauncherIconSelection.FOLLOW_THEME -> resolvedTheme.launcherIcon
            AndroidLauncherIconSelection.DEFAULT -> ThemeLauncherIcon.DEFAULT
            AndroidLauncherIconSelection.NIGHT -> ThemeLauncherIcon.NIGHT
        }
    }

    val currentTemplate = remember(currentGame, allTemplates, target) {
        currentGame?.let { game -> templateFor(game.templatePackageId.value, allTemplates, target) }
    }
    val currentGameWithResolvedDescription = remember(currentGame, allTemplates, target, strings) {
        currentGame?.let { game ->
            game.copy(
                description = resolvedInstanceDescription(
                    game,
                    templateFor(game.templatePackageId.value, allTemplates, target),
                ),
            )
        }
    }
    val gamesWithResolvedDescriptions = remember(games, allTemplates, target, strings) {
        games.map { game ->
            game.copy(
                description = resolvedInstanceDescription(
                    game,
                    templateFor(game.templatePackageId.value, allTemplates, target),
                ),
            )
        }
    }
    val localizedThemes = remember(allThemes, strings) {
        allThemes.map { theme -> theme.name.resolve(strings.language) to theme }
    }
    val selectedCreateTemplate = remember(compatibleTemplates, selectedCreateTemplatePackageId) {
        compatibleTemplates.firstOrNull { it.packageId.value == selectedCreateTemplatePackageId }
    }
    val selectedCreateNameDraft = selectedCreateTemplate?.let { template ->
        createGameNameDrafts[template.packageId.value]
            ?: template.name.resolve(strings.language)
    }.orEmpty()
    val selectedCreateDescriptionDraft = selectedCreateTemplate?.let { template ->
        createGameDescriptionDrafts[template.packageId.value]
            ?: template.defaultInstanceDescription?.resolve(strings.language)
            ?: template.description.resolve(strings.language)
    }.orEmpty()
    val extensionFailureSnapshot = extensionRegistry.extensionFailureSnapshot()
    val extensionDescriptors = remember(target, hostSdk, discoveredTExtensionPackages, extensionFailureSnapshot) {
        discoveredTExtensionPackages.map { discovered ->
            val compatibility = discovered.extension.compatibilityAgainst(hostSdk)
            val combinedRuntimeLoadError = listOfNotNull(
                discovered.runtimeLoadError?.trim()?.takeIf { it.isNotEmpty() },
                extensionFailureSnapshot[discovered.extension.identityId]?.trim()?.takeIf { it.isNotEmpty() },
            ).distinct().joinToString("\n").trim().ifEmpty { "" }.takeIf { it.isNotEmpty() }
            ExtensionDescriptor(
                extension = discovered.extension,
                displayName = discovered.displayName,
                enabled = target in discovered.extension.supportedTargets &&
                    compatibility.isCompatible &&
                    discovered.isLoaded,
                compatibility = compatibility,
                priorityPinnedToBottom = target !in discovered.extension.supportedTargets || !compatibility.isCompatible,
                hostGrants = discovered.hostGrants,
                userEnabled = discovered.userEnabled,
                sourceName = discovered.sourceName,
                packageVersion = discovered.packageVersion,
                apiVersion = discovered.apiVersion,
                packageDescription = discovered.description,
                runtimeLoaded = discovered.isLoaded,
                runtimeLoadError = combinedRuntimeLoadError,
            )
        }
    }
    val extensionPriorityEntries = remember(extensionDescriptors, uiRefreshTick) {
        extensionPriorityStore.prioritizedEntries(extensionDescriptors)
    }
    val prioritizedExtensionFeatures = remember(extensionPriorityEntries, registeredExtensionFeatures) {
        prioritizeExtensionFeatures(
            registeredFeatures = registeredExtensionFeatures,
            priorityEntries = extensionPriorityEntries,
        )
    }
    val templateLaunchPreparationInterceptors = remember(prioritizedExtensionFeatures) {
        prioritizedExtensionFeatures.mapNotNull { registered ->
            registered.feature as? TemplateLaunchPreparationInterceptorExtension
        }
    }

    fun installExtensionPackage(
        source: ExtensionPackageSource,
        parsedPackage: ParsedExtensionPackage,
    ) {
        runCatching {
            installedExtensionPackageStore.installPackage(source)
        }.onSuccess {
            extensionRegistry.clearExtensionFailures(parsedPackage.manifest.extension.identityId)
            extensionLoadStatusMessage = strings.extensionManagerInstallSuccess(parsedPackage.manifest.displayName)
            refreshExtensions()
        }.onFailure { error ->
            extensionLoadStatusMessage = strings.extensionManagerLoadFailed(
                error.message ?: strings.commonNone,
            )
        }
    }

    fun replaceExtensionPackage(
        replacement: ExtensionPackageReplacement,
        isUpdate: Boolean,
    ) {
        runCatching {
            replacement.existingSourceNames.forEach { sourceName ->
                if (!installedExtensionPackageStore.uninstallPackage(sourceName)) {
                    error("Unable to delete existing extension package: $sourceName")
                }
            }
            installedExtensionPackageStore.installPackage(replacement.source)
        }.onSuccess {
            val manifest = replacement.parsedPackage.manifest
            extensionRegistry.clearExtensionFailures(manifest.extension.identityId)
            extensionLoadStatusMessage = if (isUpdate) {
                strings.extensionManagerInstallUpdated(
                    name = manifest.displayName,
                    previousVersion = replacement.existingVersion,
                    nextVersion = manifest.version,
                )
            } else {
                strings.extensionManagerInstallOverwritten(
                    name = manifest.displayName,
                    previousVersion = replacement.existingVersion,
                    nextVersion = manifest.version,
                )
            }
            refreshExtensions()
        }.onFailure { error ->
            extensionLoadStatusMessage = strings.extensionManagerLoadFailed(
                error.message ?: strings.commonNone,
            )
        }
    }

    fun handlePickedExtensionPackage(source: ExtensionPackageSource) {
        runCatching {
            val parsedPackage = extensionPackageParser.parse(source)
            val replacement = extensionPackageReplacementFor(
                source = source,
                parsedPackage = parsedPackage,
                installedSources = installedExtensionPackageStore.listInstalledPackages(),
                parser = extensionPackageParser,
            )
            parsedPackage to replacement
        }.onSuccess { (parsedPackage, replacement) ->
            if (replacement == null) {
                installExtensionPackage(source, parsedPackage)
                return@onSuccess
            }
            pendingExtensionPackageReplacement = replacement
        }.onFailure { error ->
            extensionLoadStatusMessage = strings.extensionManagerLoadFailed(
                error.message ?: strings.commonNone,
            )
        }
    }

    val actionDispatcher = remember(
        target,
        currentGame,
        currentTemplate,
        allTemplates,
        extensionDescriptors,
        prioritizedExtensionFeatures,
        gameLaunchState,
        updateCheckState,
        extensionPackagePickerState,
        installedExtensionPackageStore,
        extensionPackageParser,
        installedExtensionSnapshot,
        discoveredTExtensionPackages,
        strings,
        extensionHostGrantStore,
        installedExtensionStateStore,
    ) {
        LauncherActionDispatcher { action ->
            val actionContext = LauncherActionContext(
                target = target,
                currentGame = currentGame,
                currentTemplate = currentTemplate,
            )
            val actionInterceptors = prioritizedExtensionFeatures.mapNotNull { registered ->
                registered.feature as? ActionInterceptorExtension
            }
            dispatchLauncherAction(action, actionContext, actionInterceptors) { resolvedAction ->
                when (resolvedAction) {
                    is LauncherAction.OpenPage -> navigateToPage(resolvedAction.pageId)
                    is LauncherAction.ReplaceCurrentPage -> replaceCurrentPage(resolvedAction.pageId)
                    is LauncherAction.OpenGameDetail -> navigateTo(TLauncherScreen.GameDetail(resolvedAction.instanceId))
                    LauncherAction.LoadExtensionPackage -> {
                        if (!extensionPackagePickerState.isSupported || !installedExtensionPackageStore.isSupported) {
                            extensionLoadStatusMessage = strings.extensionManagerLoadUnsupported()
                            return@dispatchLauncherAction
                        }
                        extensionPackagePickerState.pickPackage { source ->
                            if (source == null) {
                                extensionLoadStatusMessage = strings.extensionManagerLoadCancelled()
                                return@pickPackage
                            }
                            handlePickedExtensionPackage(source)
                        }
                    }
                    is LauncherAction.DeleteExtensionPackage -> {
                        val packageLabel = discoveredTExtensionPackages
                            .firstOrNull { discovered -> discovered.sourceName == resolvedAction.sourceName }
                            ?.displayName
                            ?: resolvedAction.sourceName
                        val removed = installedExtensionPackageStore.isSupported &&
                            installedExtensionPackageStore.uninstallPackage(resolvedAction.sourceName)
                        if (removed) {
                            extensionLoadStatusMessage = strings.extensionManagerDeletePackageSuccess(packageLabel)
                            refreshExtensions()
                        } else {
                            extensionLoadStatusMessage = strings.extensionManagerDeletePackageFailed(packageLabel)
                        }
                    }
                    LauncherAction.NavigateBack -> navigateBack()
                    LauncherAction.Refresh -> refreshCurrentPage()
                    is LauncherAction.OpenExternalUrl -> openExternalUrl(resolvedAction.url)
                    is LauncherAction.LaunchGame -> {
                        val launchContext = GameLaunchContext(
                            target = target,
                            currentGame = currentGame,
                            currentTemplate = currentTemplate,
                        )
                        val launchInterceptors = prioritizedExtensionFeatures.mapNotNull { registered ->
                            registered.feature as? LaunchInterceptorExtension
                        }
                        dispatchGameLaunch(resolvedAction.request, launchContext, launchInterceptors) { request ->
                            gameLaunchState?.syncLaunchContext(request)
                            gameLaunchState?.launch?.invoke(request)
                        }
                    }
                    is LauncherAction.SelectCurrentGame -> {
                        library.setCurrentInstance(resolvedAction.instanceId)
                        refreshUi()
                    }
                    is LauncherAction.CreateGameInstance -> {
                        library.createInstance(
                            templatePackageId = resolvedAction.templatePackageId,
                            displayName = resolvedAction.displayName,
                            description = resolvedAction.description,
                        )
                        createGameNameDrafts.remove(resolvedAction.templatePackageId.value)
                        createGameDescriptionDrafts.remove(resolvedAction.templatePackageId.value)
                        refreshUi()
                        navigateBack()
                    }
                    is LauncherAction.UpdateGameInstanceDetails -> {
                        if (
                            library.updateInstanceDetails(
                                resolvedAction.instanceId,
                                resolvedAction.displayName,
                                resolvedAction.description,
                            )
                        ) {
                            detailGameNameDrafts.remove(resolvedAction.instanceId.value)
                            detailGameDescriptionDrafts.remove(resolvedAction.instanceId.value)
                            refreshUi()
                        }
                    }
                    is LauncherAction.DeleteGameInstance -> {
                        detailGameNameDrafts.remove(resolvedAction.instanceId.value)
                        detailGameDescriptionDrafts.remove(resolvedAction.instanceId.value)
                        library.deleteInstance(resolvedAction.instanceId)
                        refreshUi()
                        navigateBack()
                    }
                    is LauncherAction.SetThemePreference -> {
                        themeSettingsStore.setCurrentPreference(resolvedAction.preference)
                        themePreference = resolvedAction.preference
                    }
                    is LauncherAction.ApplyLauncherIconAndClose -> {
                        LauncherIconController.setIconAndCloseLauncher(resolvedAction.icon.toLauncherIconSource())
                    }
                    is LauncherAction.SetLanguagePreference -> {
                        languageSettingsStore.setCurrentPreference(resolvedAction.preference)
                        languagePreference = resolvedAction.preference
                    }
                    is LauncherAction.ChangeExtensionPriority -> {
                        when (resolvedAction.direction) {
                            ExtensionPriorityDirection.INCREASE ->
                                extensionPriorityStore.increasePriority(resolvedAction.identityId, extensionDescriptors)

                            ExtensionPriorityDirection.DECREASE ->
                                extensionPriorityStore.decreasePriority(resolvedAction.identityId, extensionDescriptors)
                        }
                        refreshUi()
                    }
                    LauncherAction.CheckForUpdates -> checkForUpdates()
                }
            }
        }
    }
    val openLatestReleasePage: () -> Unit = {
        val url = updateCheckState.release?.htmlUrl ?: updateChecker.releasesPageUrl()
        actionDispatcher.dispatch(LauncherAction.OpenExternalUrl(url))
    }
    val requestBackNavigation: () -> Unit = {
        if (
            screen == TLauncherScreen.AddAccount &&
            selectedAddAccountProvider == LauncherAccountProvider.STEAM &&
            accountLoginInProgress
        ) {
            pendingAddAccountExitConfirmation = true
        } else {
            actionDispatcher.dispatch(LauncherAction.NavigateBack)
        }
    }
    val pageContext = remember(
        uiRefreshTick,
        pageRefreshTick,
        currentGame,
        currentTemplate,
        games,
        compatibleTemplates,
        extensionPriorityEntries,
        accounts,
        strings,
        target,
        manageStorageAccessState,
        directoryPickerState,
        filePickerState,
        gameLaunchState,
        templateLaunchPreparationInterceptors,
        actionDispatcher,
    ) {
        PageContext(
            target = target,
            currentGame = currentGame,
            currentTemplate = currentTemplate,
            allGames = games,
            visibleTemplates = compatibleTemplates,
            extensionPriorityEntries = extensionPriorityEntries,
            accounts = accounts,
            strings = strings,
            manageStorageAccessState = manageStorageAccessState.takeIf { target == PlatformTarget.ANDROID },
            directoryPickerState = directoryPickerState.takeIf { it.isSupported },
            filePickerState = filePickerState.takeIf { it.isSupported },
            gameLaunchState = gameLaunchState?.takeIf { target == PlatformTarget.ANDROID && it.isSupported },
            interceptPreparedLaunchRequest = { request, launchContext ->
                dispatchTemplateLaunchPreparation(request, launchContext, templateLaunchPreparationInterceptors)
            },
            actionDispatcher = actionDispatcher,
            navigateToPage = ::navigateToPage,
            navigateBack = ::navigateBack,
            requestUiRefresh = ::refreshUi,
        )
    }
    BindSystemBackHandler(enabled = backStack.size > 1) {
        requestBackNavigation()
    }

    ProvideAppStrings(strings) {
        TLauncherTheme(theme = resolvedTheme) {
            Surface {
                val page: LauncherPage? = when (val destination = screen) {
                    TLauncherScreen.Home -> remember(target, currentGameWithResolvedDescription, strings) {
                        HomePage(
                            target = target,
                            currentGame = currentGameWithResolvedDescription,
                            onOpenLibrary = { actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.LIBRARY)) },
                            onOpenSettings = { actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.SETTINGS)) },
                        )
                    }

                    TLauncherScreen.Library -> remember(
                        gamesWithResolvedDescriptions,
                        currentGame?.id?.value,
                        strings,
                    ) {
                        LibraryPage(
                            games = gamesWithResolvedDescriptions,
                            currentGameId = currentGame?.id?.value,
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                            onCreateGame = {
                                selectedCreateTemplatePackageId = null
                                actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.CREATE_GAME))
                            },
                            onSelectGame = { game ->
                                actionDispatcher.dispatch(LauncherAction.SelectCurrentGame(game.id))
                            },
                            onOpenDetails = { game ->
                                actionDispatcher.dispatch(LauncherAction.OpenGameDetail(game.id))
                            },
                        )
                    }

                    TLauncherScreen.CreateGame -> remember(
                        compatibleTemplates,
                        selectedCreateTemplate,
                        selectedCreateNameDraft,
                        selectedCreateDescriptionDraft,
                        games,
                        strings,
                    ) {
                        CreateGamePage(
                            templates = compatibleTemplates,
                            selectedTemplate = selectedCreateTemplate,
                            onSelectTemplate = { template ->
                                selectedCreateTemplatePackageId = template.packageId.value
                            },
                            canCreate = { template -> library.canCreate(template.packageId) },
                            nameFor = { template ->
                                createGameNameDrafts[template.packageId.value]
                                    ?: template.name.resolve(strings.language)
                            },
                            descriptionFor = { template ->
                                createGameDescriptionDrafts[template.packageId.value]
                                    ?: template.defaultInstanceDescription?.resolve(strings.language)
                                    ?: template.description.resolve(strings.language)
                            },
                            onNameChange = { template, value ->
                                createGameNameDrafts[template.packageId.value] = value
                            },
                            onDescriptionChange = { template, value ->
                                createGameDescriptionDrafts[template.packageId.value] = value
                            },
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                            onCreate = { template, draftName, draftDescription ->
                                val defaultName = template.name.resolve(strings.language)
                                val defaultDescription = template.defaultInstanceDescription?.resolve(strings.language)
                                    ?: template.description.resolve(strings.language)
                                selectedCreateTemplatePackageId = null
                                actionDispatcher.dispatch(
                                    LauncherAction.CreateGameInstance(
                                        templatePackageId = template.packageId,
                                        displayName = draftName.trim().ifBlank { defaultName },
                                        description = draftDescription.trim().ifBlank { defaultDescription },
                                    ),
                                )
                            },
                        )
                    }

                    is TLauncherScreen.GameDetail -> {
                        val game = library.getInstance(destination.instanceId)
                        if (game == null) {
                            LaunchedEffect(destination.instanceId) {
                                popBackOr(TLauncherScreen.Library)
                            }
                            null
                        } else {
                            val template = templateFor(game.templatePackageId.value, allTemplates, target)
                            val resolvedGame = game.copy(
                                description = resolvedInstanceDescription(game, template),
                            )
                            fun persistGameDetailDraft(
                                nextNameDraft: String? = null,
                                nextDescriptionDraft: String? = null,
                            ) {
                                val rawName = nextNameDraft ?: detailGameNameDrafts[game.id.value] ?: resolvedGame.displayName
                                val rawDescription = nextDescriptionDraft
                                    ?: detailGameDescriptionDrafts[game.id.value]
                                    ?: resolvedGame.description
                                val nextName = rawName.trim().ifBlank { resolvedGame.displayName }
                                val nextDescription = rawDescription.trim()
                                detailGameNameDrafts.remove(game.id.value)
                                detailGameDescriptionDrafts.remove(game.id.value)
                                actionDispatcher.dispatch(
                                    LauncherAction.UpdateGameInstanceDetails(
                                        instanceId = game.id,
                                        displayName = nextName,
                                        description = nextDescription,
                                    ),
                                )
                            }
                            val nameDraft = detailGameNameDrafts[game.id.value] ?: resolvedGame.displayName
                            val descriptionDraft = detailGameDescriptionDrafts[game.id.value]
                                ?: resolvedGame.description
                            remember(resolvedGame, template, nameDraft, descriptionDraft, strings) {
                                GameDetailPage(
                                    game = resolvedGame,
                                    template = template,
                                    nameDraft = nameDraft,
                                    descriptionDraft = descriptionDraft,
                                    onNameChange = { value ->
                                        detailGameNameDrafts[game.id.value] = value
                                        persistGameDetailDraft(
                                            nextNameDraft = value,
                                        )
                                    },
                                    onDescriptionChange = { value ->
                                        detailGameDescriptionDrafts[game.id.value] = value
                                        persistGameDetailDraft(
                                            nextDescriptionDraft = value,
                                        )
                                    },
                                    onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                                    onDelete = {
                                        actionDispatcher.dispatch(LauncherAction.DeleteGameInstance(game.id))
                                    },
                                )
                            }
                        }
                    }

                    TLauncherScreen.Settings -> remember(
                        themePreference,
                        resolvedTheme.id,
                        resolvedTheme.launcherIcon,
                        selectedAndroidLauncherIcon,
                        androidLauncherIconSelection,
                        localizedThemes,
                        languagePreference,
                        resolvedLanguage,
                        accounts.size,
                        extensionPriorityEntries.size,
                        target,
                        manageStorageAccessState.isSupported,
                        manageStorageAccessState.isGranted,
                        strings,
                    ) {
                        SettingsPage(
                            themePreference = themePreference,
                            resolvedThemeName = resolvedTheme.name.resolve(strings.language),
                            allThemes = localizedThemes,
                            onSelectFollowSystemTheme = {
                                themeSettingsStore.setCurrentPreference(ThemePreference.FollowSystem)
                                themePreference = ThemePreference.FollowSystem
                            },
                            onSelectTheme = { theme ->
                                val nextPreference = ThemePreference.Fixed(theme.id)
                                themeSettingsStore.setCurrentPreference(nextPreference)
                                themePreference = nextPreference
                            },
                            resolvedLauncherIcon = resolvedTheme.launcherIcon,
                            launcherIconFollowsTheme = androidLauncherIconSelection == AndroidLauncherIconSelection.FOLLOW_THEME,
                            selectedLauncherIcon = selectedAndroidLauncherIcon,
                            onSelectFollowThemeLauncherIcon = if (target == PlatformTarget.ANDROID) {
                                { androidLauncherIconSelection = AndroidLauncherIconSelection.FOLLOW_THEME }
                            } else {
                                null
                            },
                            onSelectLauncherIcon = if (target == PlatformTarget.ANDROID) {
                                { icon ->
                                    androidLauncherIconSelection = when (icon) {
                                        ThemeLauncherIcon.DEFAULT -> AndroidLauncherIconSelection.DEFAULT
                                        ThemeLauncherIcon.NIGHT -> AndroidLauncherIconSelection.NIGHT
                                    }
                                }
                            } else {
                                null
                            },
                            onSaveLauncherIconAndClose = if (target == PlatformTarget.ANDROID) {
                                {
                                    actionDispatcher.dispatch(
                                        LauncherAction.ApplyLauncherIconAndClose(selectedAndroidLauncherIcon),
                                    )
                                }
                            } else {
                                null
                            },
                            languagePreference = languagePreference,
                            resolvedLanguage = resolvedLanguage,
                            onSelectFollowSystemLanguage = {
                                languageSettingsStore.setCurrentPreference(LanguagePreference.FollowSystem)
                                languagePreference = LanguagePreference.FollowSystem
                            },
                            onSelectLanguage = { language ->
                                val nextPreference = LanguagePreference.Fixed(language)
                                languageSettingsStore.setCurrentPreference(nextPreference)
                                languagePreference = nextPreference
                            },
                            accountCount = accounts.size,
                            onOpenAccountManager = {
                                refreshAccounts()
                                actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.ACCOUNT_MANAGER))
                            },
                            extensionCount = extensionPriorityEntries.size,
                            target = target,
                            onOpenExtensionManager = { actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.EXTENSION_MANAGER)) },
                            onOpenAbout = { actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.ABOUT)) },
                            onManageStorageAccess = manageStorageAccessState.requestAccess,
                            manageStorageSupported = manageStorageAccessState.isSupported,
                            manageStorageGranted = manageStorageAccessState.isGranted,
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                        )
                    }

                    TLauncherScreen.AccountManager -> remember(
                        accounts,
                        accountService.isSupported,
                        accountLoadInProgress,
                        accountMutationInProgress,
                        accountManagerStatusMessage,
                        strings,
                    ) {
                        AccountManagerPage(
                            accounts = accounts,
                            isSupported = accountService.isSupported,
                            isLoading = accountLoadInProgress,
                            isMutating = accountMutationInProgress,
                            statusMessage = accountManagerStatusMessage,
                            onAddAccount = {
                                addAccountStatusMessage = null
                                addAccountLoginStatus = null
                                pendingAddAccountExitConfirmation = false
                                actionDispatcher.dispatch(LauncherAction.OpenPage(PageIds.ADD_ACCOUNT))
                            },
                            onRefresh = ::refreshAccounts,
                            onDeleteAccount = { account -> pendingAccountDeletion = account },
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                        )
                    }

                    TLauncherScreen.AddAccount -> remember(
                        selectedAddAccountProvider,
                        selectedSteamLoginMode,
                        steamAccountNameDraft,
                        steamPasswordDraft,
                        steamGuardCodeDraft,
                        accountService.isSupported,
                        accountLoginInProgress,
                        addAccountLoginStatus,
                        addAccountStatusMessage,
                        strings,
                    ) {
                        AddAccountPage(
                            selectedProvider = selectedAddAccountProvider,
                            selectedSteamLoginMode = selectedSteamLoginMode,
                            accountName = steamAccountNameDraft,
                            password = steamPasswordDraft,
                            steamGuardCode = steamGuardCodeDraft,
                            isSupported = accountService.isSupported,
                            isLoggingIn = accountLoginInProgress,
                            loginStatus = addAccountLoginStatus,
                            statusMessage = addAccountStatusMessage,
                            onSelectProvider = { provider ->
                                selectedAddAccountProvider = provider
                                addAccountLoginStatus = null
                                addAccountStatusMessage = null
                            },
                            onSelectSteamLoginMode = { mode ->
                                selectedSteamLoginMode = mode
                                addAccountLoginStatus = null
                                addAccountStatusMessage = null
                            },
                            onAccountNameChange = { value ->
                                steamAccountNameDraft = value
                                addAccountLoginStatus = null
                                addAccountStatusMessage = null
                            },
                            onPasswordChange = { value ->
                                steamPasswordDraft = value
                                addAccountLoginStatus = null
                                addAccountStatusMessage = null
                            },
                            onSteamGuardCodeChange = { value -> steamGuardCodeDraft = value },
                            onLogin = {
                                if (!accountLoginInProgress) {
                                    val attemptId = nextAddAccountLoginAttemptId + 1L
                                    nextAddAccountLoginAttemptId = attemptId
                                    activeAddAccountLoginAttemptId = attemptId
                                    accountLoginInProgress = true
                                    addAccountLoginStatus = null
                                    addAccountStatusMessage = null
                                    coroutineScope.launch {
                                        runCatching {
                                            accountService.loginSteam(
                                                SteamAccountLoginRequest(
                                                    accountName = steamAccountNameDraft.trim(),
                                                    password = steamPasswordDraft,
                                                    steamGuardCode = steamGuardCodeDraft.trim().ifBlank { null },
                                                    mode = selectedSteamLoginMode,
                                                ),
                                            )
                                        }.onSuccess { result ->
                                            if (attemptId in canceledAddAccountLoginKnownSubjects) {
                                                rollbackCanceledAddAccountLoginIfNeeded(attemptId, result)
                                                return@onSuccess
                                            }
                                            if (activeAddAccountLoginAttemptId != attemptId) {
                                                return@onSuccess
                                            }
                                            addAccountLoginStatus = result.status
                                            addAccountStatusMessage = when (result.status) {
                                                LauncherAccountLoginStatus.STEAM_GUARD_REQUIRED ->
                                                    if (result.moduleStatus.equals("polling", ignoreCase = true)) {
                                                        strings.addAccountPollingConfirmationMessage()
                                                    } else {
                                                        strings.addAccountSteamGuardRequiredMessage()
                                                    }

                                                LauncherAccountLoginStatus.CANCELED ->
                                                    strings.addAccountLoginCanceledMessage()

                                                else -> result.message
                                            }
                                            if (result.status == LauncherAccountLoginStatus.SUCCEEDED) {
                                                runCatching { accountService.listAccounts() }
                                                    .onSuccess { loadedAccounts -> accounts = loadedAccounts }
                                                    .onFailure { error ->
                                                        accountManagerStatusMessage =
                                                            error.message ?: "Failed to refresh accounts."
                                                    }
                                                val displayName = result.account?.displayName
                                                    ?: result.account?.subjectId
                                                    ?: steamAccountNameDraft.trim()
                                                accountManagerStatusMessage = strings.addAccountLoginSucceeded(displayName)
                                                steamPasswordDraft = ""
                                                steamGuardCodeDraft = ""
                                                pendingAddAccountExitConfirmation = false
                                                navigateBack()
                                            }
                                        }.onFailure { error ->
                                            if (attemptId in canceledAddAccountLoginKnownSubjects) {
                                                canceledAddAccountLoginKnownSubjects.remove(attemptId)
                                                return@onFailure
                                            }
                                            if (activeAddAccountLoginAttemptId != attemptId) {
                                                return@onFailure
                                            }
                                            addAccountStatusMessage = error.message ?: "Login failed."
                                        }
                                        clearVisibleAddAccountLoginAttempt(attemptId)
                                    }
                                }
                            },
                            onCancelLogin = { cancelVisibleAddAccountLogin(navigateAway = false) },
                            onBack = requestBackNavigation,
                        )
                    }

                    TLauncherScreen.ExtensionManager -> remember(
                        extensionPriorityEntries,
                        extensionPermissionReviews,
                        extensionPackageScanProblems,
                        target,
                        installedExtensionPackageStore.isSupported,
                        extensionPackagePickerState.isSupported,
                        extensionLoadStatusMessage,
                        strings,
                    ) {
                        ExtensionManagerPage(
                            entries = extensionPriorityEntries,
                            permissionReviews = extensionPermissionReviews,
                            scanProblems = extensionPackageScanProblems,
                            currentTarget = target,
                            installedPackageDirectory = if (installedExtensionPackageStore.isSupported) {
                                launcherStorageDirectoryPath() + "/extensions/packages"
                            } else {
                                null
                            },
                            onLoadExtension = if (extensionPackagePickerState.isSupported) {
                                { actionDispatcher.dispatch(LauncherAction.LoadExtensionPackage) }
                            } else {
                                null
                            },
                            onDeleteExtensionPackage = if (installedExtensionPackageStore.isSupported) {
                                { sourceName ->
                                    actionDispatcher.dispatch(LauncherAction.DeleteExtensionPackage(sourceName))
                                }
                            } else {
                                null
                            },
                            loadStatusMessage = extensionLoadStatusMessage,
                            onTogglePermissionGrant = { review, permissionKey, granted ->
                                extensionHostGrantStore.setSingleGrantDecision(
                                    extensionManifest = review.extension,
                                    permissionKey = permissionKey,
                                    state = if (granted) HostGrantState.GRANTED else HostGrantState.DENIED,
                                    reason = if (granted) {
                                        "Allowed in extension manager."
                                    } else {
                                        "Denied in extension manager."
                                    },
                                )
                                extensionLoadStatusMessage = strings.extensionPermissionToggleChanged(
                                    extensionName = review.displayName,
                                    permissionName = strings.hostPermissionName(permissionKey),
                                    granted = granted,
                                )
                                refreshUi()
                            },
                            onOpenExtensionDetail = { identityId -> navigateTo(TLauncherScreen.ExtensionDetail(identityId)) },
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                            onIncreasePriority = { identityId ->
                                actionDispatcher.dispatch(
                                    LauncherAction.ChangeExtensionPriority(
                                        identityId = identityId,
                                        direction = ExtensionPriorityDirection.INCREASE,
                                    ),
                                )
                            },
                            onDecreasePriority = { identityId ->
                                actionDispatcher.dispatch(
                                    LauncherAction.ChangeExtensionPriority(
                                        identityId = identityId,
                                        direction = ExtensionPriorityDirection.DECREASE,
                                    ),
                                )
                            },
                        )
                    }

                    is TLauncherScreen.ExtensionDetail -> {
                        val entry = extensionPriorityEntries
                            .firstOrNull { candidate ->
                                candidate.descriptor.extension.identityId == destination.identityId
                            }
                        if (entry == null) {
                            LaunchedEffect(destination.identityId) {
                                popBackOr(TLauncherScreen.ExtensionManager)
                            }
                            null
                        } else {
                            remember(
                                entry,
                                target,
                                installedExtensionPackageStore.isSupported,
                                strings,
                            ) {
                                ExtensionDetailPage(
                                    entry = entry,
                                    currentTarget = target,
                                    onToggleExtensionEnabled = { identityId, enabled ->
                                        val extensionName = extensionPriorityEntries
                                            .firstOrNull { candidate ->
                                                candidate.descriptor.extension.identityId == identityId
                                            }
                                            ?.descriptor
                                            ?.displayName
                                            ?: identityId
                                        installedExtensionStateStore.setEnabled(identityId, enabled)
                                        extensionLoadStatusMessage = strings.extensionEnabledChanged(extensionName, enabled)
                                        refreshUi()
                                    },
                                    onDeleteExtensionPackage = if (installedExtensionPackageStore.isSupported) {
                                        { sourceName ->
                                            actionDispatcher.dispatch(LauncherAction.DeleteExtensionPackage(sourceName))
                                        }
                                    } else {
                                        null
                                    },
                                    onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                                )
                            }
                        }
                    }

                    TLauncherScreen.About -> remember(updateCheckState, strings) {
                        AboutPage(
                            onBack = { actionDispatcher.dispatch(LauncherAction.NavigateBack) },
                            updateCheckState = updateCheckState,
                            onCheckUpdates = { actionDispatcher.dispatch(LauncherAction.CheckForUpdates) },
                            onOpenReleasePage = openLatestReleasePage,
                            onOpenAuthorProfile = {
                                actionDispatcher.dispatch(
                                    LauncherAction.OpenExternalUrl("https://github.com/dreamyloong2005"),
                                )
                            },
                        )
                    }

                    is TLauncherScreen.ContributedPage -> remember(destination.pageId) {
                        HostedLauncherPage(destination.pageId)
                    }
                }

                if (page != null) {
                    val pageMotionTarget = PageMotionTarget(
                        screenKey = screen.toString(),
                        backStackSize = backStack.size,
                    )
                    val resolvedPage = rememberResolvedLauncherPage(
                        page = page,
                        pageResolver = pageResolver,
                        pageContext = pageContext,
                    )
                    pageMotionPages[pageMotionTarget] = resolvedPage
                    if (pageMotionPages.size > 6) {
                        pageMotionPages.keys
                            .filter { key -> key != pageMotionTarget }
                            .take(pageMotionPages.size - 6)
                            .forEach { key -> pageMotionPages.remove(key) }
                    }
                    AnimatedContent(
                        targetState = pageMotionTarget,
                        transitionSpec = {
                            val direction = if (targetState.backStackSize >= initialState.backStackSize) {
                                1
                            } else {
                                -1
                            }
                            (
                                fadeIn(
                                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                                ) + slideInHorizontally(
                                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
                                    initialOffsetX = { direction * (it / 16) },
                                ) + scaleIn(
                                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
                                    initialScale = 0.99f,
                                )
                            ).togetherWith(
                                fadeOut(
                                    animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
                                ) + slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
                                    targetOffsetX = { -direction * (it / 20) },
                                ) + scaleOut(
                                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                                    targetScale = 0.998f,
                                ),
                            )
                        },
                        label = "launcher_page_transition",
                    ) { targetPage ->
                        val targetResolvedPage = pageMotionPages[targetPage]
                            ?: if (targetPage == pageMotionTarget) resolvedPage else null
                        targetResolvedPage?.let { RenderLauncherPage(it) }
                    }
                }
                pendingExtensionPackageReplacement?.let { replacement ->
                    val manifest = replacement.parsedPackage.manifest
                    val isUpdate = compareExtensionPackageVersions(
                        manifest.version,
                        replacement.existingVersion,
                    ) > 0
                    AlertDialog(
                        onDismissRequest = {
                            pendingExtensionPackageReplacement = null
                            extensionLoadStatusMessage = strings.extensionManagerLoadCancelled()
                        },
                        title = {
                            Text(
                                if (isUpdate) {
                                    strings.extensionManagerUpdatePackageTitle(manifest.displayName)
                                } else {
                                    strings.extensionManagerReplacePackageTitle(manifest.displayName)
                                },
                            )
                        },
                        text = {
                            Text(
                                if (isUpdate) {
                                    strings.extensionManagerUpdatePackageMessage(
                                        name = manifest.displayName,
                                        existingVersion = replacement.existingVersion,
                                        incomingVersion = manifest.version,
                                    )
                                } else {
                                    strings.extensionManagerReplacePackageMessage(
                                        existingVersion = replacement.existingVersion,
                                        incomingVersion = manifest.version,
                                    )
                                },
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    pendingExtensionPackageReplacement = null
                                    replaceExtensionPackage(replacement, isUpdate = isUpdate)
                                },
                            ) {
                                Text(
                                    if (isUpdate) {
                                        strings.extensionManagerUpdatePackageConfirm()
                                    } else {
                                        strings.extensionManagerReplacePackageConfirm()
                                    },
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    pendingExtensionPackageReplacement = null
                                    extensionLoadStatusMessage = strings.extensionManagerLoadCancelled()
                                },
                            ) {
                                Text(strings.extensionManagerReplacePackageCancel())
                            }
                        },
                    )
                }
                pendingAccountDeletion?.let { account ->
                    val displayName = accountDisplayName(account)
                    AlertDialog(
                        onDismissRequest = {
                            if (!accountMutationInProgress) {
                                pendingAccountDeletion = null
                            }
                        },
                        title = {
                            Text(strings.accountManagerDeleteAccountTitle(displayName))
                        },
                        text = {
                            Text(
                                strings.accountManagerDeleteAccountMessage(
                                    providerName = strings.accountProviderName(account.provider),
                                    subjectId = account.subjectId,
                                ),
                            )
                        },
                        confirmButton = {
                            Button(
                                enabled = !accountMutationInProgress,
                                onClick = {
                                    pendingAccountDeletion = null
                                    deleteAccount(account)
                                },
                            ) {
                                Text(strings.accountManagerDeleteAccount())
                            }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !accountMutationInProgress,
                                onClick = { pendingAccountDeletion = null },
                            ) {
                                Text(strings.accountManagerCancelDeleteAccount())
                            }
                        },
                    )
                }
                if (pendingAddAccountExitConfirmation) {
                    AlertDialog(
                        onDismissRequest = { pendingAddAccountExitConfirmation = false },
                        title = {
                            Text(strings.addAccountCancelConfirmationTitle())
                        },
                        text = {
                            Text(strings.addAccountCancelConfirmationMessage())
                        },
                        confirmButton = {
                            Button(
                                onClick = { cancelVisibleAddAccountLogin(navigateAway = true) },
                            ) {
                                Text(strings.addAccountCancelConfirmationExit())
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { pendingAddAccountExitConfirmation = false },
                            ) {
                                Text(strings.addAccountCancelConfirmationStay())
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun templateFor(
    templatePackageId: String,
    templates: List<com.dreamyloong.tlauncher.core.model.TemplateDescriptor>,
    target: PlatformTarget,
): Template? {
    return templates.firstOrNull { it.packageId.value == templatePackageId }?.resolve(target)
}

@Composable
private fun rememberResolvedLauncherPage(
    page: LauncherPage,
    pageResolver: PageResolver,
    pageContext: PageContext,
): ResolvedPage {
    return remember(page, pageResolver, pageContext) {
        pageResolver.resolve(page, pageContext)
    }
}

private fun screenForPageId(pageId: String): TLauncherScreen {
    return when (pageId) {
        PageIds.HOME -> TLauncherScreen.Home
        PageIds.LIBRARY -> TLauncherScreen.Library
        PageIds.CREATE_GAME -> TLauncherScreen.CreateGame
        PageIds.SETTINGS -> TLauncherScreen.Settings
        PageIds.ACCOUNT_MANAGER -> TLauncherScreen.AccountManager
        PageIds.ADD_ACCOUNT -> TLauncherScreen.AddAccount
        PageIds.EXTENSION_MANAGER -> TLauncherScreen.ExtensionManager
        PageIds.ABOUT -> TLauncherScreen.About
        else -> TLauncherScreen.ContributedPage(pageId)
    }
}

private fun TLauncherScreen.toSaveableToken(): String {
    return when (this) {
        TLauncherScreen.Home -> "home"
        TLauncherScreen.Library -> "library"
        TLauncherScreen.CreateGame -> "create_game"
        TLauncherScreen.Settings -> "settings"
        TLauncherScreen.AccountManager -> "account_manager"
        TLauncherScreen.AddAccount -> "add_account"
        TLauncherScreen.ExtensionManager -> "extension_manager"
        TLauncherScreen.About -> "about"
        is TLauncherScreen.GameDetail -> "game_detail:${instanceId.value}"
        is TLauncherScreen.ExtensionDetail -> "extension_detail:$identityId"
        is TLauncherScreen.ContributedPage -> "contributed:$pageId"
    }
}

private fun String.toTLauncherScreen(): TLauncherScreen? {
    return when {
        this == "home" -> TLauncherScreen.Home
        this == "library" -> TLauncherScreen.Library
        this == "create_game" -> TLauncherScreen.CreateGame
        this == "settings" -> TLauncherScreen.Settings
        this == "account_manager" -> TLauncherScreen.AccountManager
        this == "add_account" -> TLauncherScreen.AddAccount
        this == "extension_manager" -> TLauncherScreen.ExtensionManager
        this == "about" -> TLauncherScreen.About
        startsWith("game_detail:") -> TLauncherScreen.GameDetail(
            GameInstanceId(removePrefix("game_detail:")),
        )
        startsWith("extension_detail:") -> TLauncherScreen.ExtensionDetail(
            removePrefix("extension_detail:"),
        )
        startsWith("contributed:") -> TLauncherScreen.ContributedPage(
            removePrefix("contributed:"),
        )
        else -> null
    }
}

private fun prioritizeExtensionFeatures(
    registeredFeatures: List<RegisteredExtensionFeature>,
    priorityEntries: List<com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry>,
): List<RegisteredExtensionFeature> {
    return registeredFeatures.sortedBy { registered ->
        priorityFor(registered.owner.extension.identityId, priorityEntries)
    }
}

private fun collectTemplatePackages(
    registeredFeatures: List<RegisteredExtensionFeature>,
    extensionRegistry: ExtensionRegistry,
): List<com.dreamyloong.tlauncher.core.template.TemplatePackage> {
    return registeredFeatures
        .mapNotNull { registered ->
            (registered.feature as? TemplateProviderExtension)?.let { feature ->
                registered to feature
            }
        }
        .flatMap { (registered, feature) ->
            runCatching { feature.provideTemplatePackages() }
                .onSuccess {
                    extensionRegistry.clearExtensionFailure(
                        registered.owner,
                        RuntimeExtensionRegistry.STAGE_TEMPLATE_PACKAGES,
                    )
                }
                .getOrElse { error ->
                    extensionRegistry.reportExtensionFailure(
                        registered.owner,
                        RuntimeExtensionRegistry.STAGE_TEMPLATE_PACKAGES,
                        error,
                    )
                    emptyList()
                }
        }
        .sortedBy { pkg -> pkg.extension.registrationId }
}

private fun collectThemePackages(
    registeredFeatures: List<RegisteredExtensionFeature>,
    extensionRegistry: ExtensionRegistry,
): List<com.dreamyloong.tlauncher.core.theme.ThemePackage> {
    return registeredFeatures
        .mapNotNull { registered ->
            (registered.feature as? ThemeProviderExtension)?.let { feature ->
                registered to feature
            }
        }
        .flatMap { (registered, feature) ->
            runCatching { feature.provideThemePackages() }
                .onSuccess {
                    extensionRegistry.clearExtensionFailure(
                        registered.owner,
                        RuntimeExtensionRegistry.STAGE_THEME_PACKAGES,
                    )
                }
                .getOrElse { error ->
                    extensionRegistry.reportExtensionFailure(
                        registered.owner,
                        RuntimeExtensionRegistry.STAGE_THEME_PACKAGES,
                        error,
                    )
                    emptyList()
                }
        }
        .sortedBy { pkg -> pkg.extension.registrationId }
}

private fun priorityFor(
    identityId: String,
    entries: List<com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry>,
): Int {
    return entries.firstOrNull { entry -> entry.descriptor.extension.identityId == identityId }?.priority ?: Int.MAX_VALUE
}

private data class DiscoveredTExtensionPackage(
    val extension: com.dreamyloong.tlauncher.core.model.ExtensionManifest,
    val displayName: String,
    val sourceName: String,
    val packageVersion: String,
    val apiVersion: String,
    val description: String?,
    val hostGrants: Set<com.dreamyloong.tlauncher.core.extension.HostGrant>,
    val userEnabled: Boolean,
    val isLoaded: Boolean,
    val runtimeLoadError: String? = null,
)

private data class InstalledExtensionScanResult(
    val discoveredPackages: List<DiscoveredTExtensionPackage>,
    val loadedExtensions: List<LauncherExtension>,
    val permissionReviews: List<ExtensionPermissionReview>,
    val scanProblems: List<ExtensionPackageScanProblem>,
)

private data class ParsedInstalledExtensionCandidate(
    val source: ExtensionPackageSource,
    val parsedPackage: ParsedExtensionPackage,
)

private data class ExtensionPackageReplacement(
    val source: ExtensionPackageSource,
    val parsedPackage: ParsedExtensionPackage,
    val existingSourceNames: List<String>,
    val existingVersion: String,
)

private data class InstalledExtensionPackageVersion(
    val sourceName: String,
    val version: String,
    val registrationId: String,
)

private fun extensionPackageReplacementFor(
    source: ExtensionPackageSource,
    parsedPackage: ParsedExtensionPackage,
    installedSources: List<ExtensionPackageSource>,
    parser: ExtensionPackageParser,
): ExtensionPackageReplacement? {
    val incomingRegistrationId = parsedPackage.manifest.extension.registrationId
    val existingPackages = installedSources.mapNotNull { installedSource ->
        val installedPackage = installedExtensionPackageVersion(installedSource, parser)
            ?: return@mapNotNull null
        if (installedPackage.registrationId != incomingRegistrationId) {
            return@mapNotNull null
        }
        installedPackage
    }
    if (existingPackages.isEmpty()) {
        return null
    }
    val currentPackage = existingPackages.maxWithOrNull { left, right ->
        compareExtensionPackageVersions(left.version, right.version)
    } ?: return null
    return ExtensionPackageReplacement(
        source = source,
        parsedPackage = parsedPackage,
        existingSourceNames = existingPackages.map { installedPackage -> installedPackage.sourceName }.distinct(),
        existingVersion = currentPackage.version,
    )
}

private fun installedExtensionPackageVersion(
    source: ExtensionPackageSource,
    parser: ExtensionPackageParser,
): InstalledExtensionPackageVersion? {
    val manifest = runCatching { parser.parse(source).manifest }
        .getOrElse {
            val manifestText = source.readUtf8(TExtensionPackageFormat.MANIFEST_PATH)
                ?: return null
            runCatching { parser.parseManifestText(manifestText) }.getOrNull()
                ?: return null
        }
    return InstalledExtensionPackageVersion(
        sourceName = source.sourceName,
        version = manifest.version,
        registrationId = manifest.extension.registrationId,
    )
}

private fun scanInstalledExtensionPackages(
    target: PlatformTarget,
    packageSources: List<com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource>,
    parser: com.dreamyloong.tlauncher.core.extension.ExtensionPackageParser,
    extensionHostGrantStore: PersistentExtensionHostGrantStore,
    installedExtensionStateStore: PersistentInstalledExtensionStateStore,
): InstalledExtensionScanResult {
    val discoveredPackages = mutableListOf<DiscoveredTExtensionPackage>()
    val loadedExtensions = mutableListOf<LauncherExtension>()
    val permissionReviews = mutableListOf<ExtensionPermissionReview>()
    val scanProblems = mutableListOf<ExtensionPackageScanProblem>()
    val parsedCandidates = mutableListOf<ParsedInstalledExtensionCandidate>()

    packageSources.forEach { source ->
        val parsedPackage = runCatching {
            parser.parse(source)
        }.getOrElse { error ->
            scanProblems += ExtensionPackageScanProblem(
                sourceName = source.sourceName,
                message = error.message ?: "Unknown error",
            )
            return@forEach
        }
        parsedCandidates += ParsedInstalledExtensionCandidate(
            source = source,
            parsedPackage = parsedPackage,
        )
    }

    val selectedCandidates = parsedCandidates
        .groupBy { candidate -> candidate.parsedPackage.manifest.extension.identityId }
        .values
        .map { candidates ->
            val selected = candidates.maxWithOrNull(
                compareBy<ParsedInstalledExtensionCandidate>(
                    { extensionTargetSelectionScore(target, it.parsedPackage.manifest.extension) },
                    { if (it.parsedPackage.manifest.extension.supportedTargets.contains(target)) 1 else 0 },
                    { it.parsedPackage.sourceName.lowercase() },
                ),
            )!!
            candidates.filterNot { candidate -> candidate === selected }.forEach { skipped ->
                scanProblems += ExtensionPackageScanProblem(
                    sourceName = skipped.parsedPackage.sourceName,
                    message = "Skipped duplicate extension identity ${skipped.parsedPackage.manifest.extension.identityId}; using ${selected.parsedPackage.sourceName}.",
                )
            }
            selected
        }

    selectedCandidates.forEach { candidate ->
        val source = candidate.source
        val parsedPackage = candidate.parsedPackage
        val packageManifest = parsedPackage.manifest
        val userEnabled = installedExtensionStateStore.isEnabled(packageManifest.extension.identityId)
        val persistedGrants = extensionHostGrantStore.syncGrants(
            extensionManifest = packageManifest.extension,
            requestedPermissionKeys = packageManifest.permissionKeys,
            suggestedGrants = emptySet(),
        )
        val allRequestedGranted = packageManifest.permissionKeys.all { permissionKey ->
            persistedGrants.any { grant -> grant.permissionKey == permissionKey && grant.isGranted }
        }

        var isLoaded = false
        var runtimeLoadError: String? = null
        if (packageManifest.permissionKeys.isNotEmpty() && !allRequestedGranted) {
            permissionReviews += ExtensionPermissionReview(
                extension = packageManifest.extension,
                displayName = packageManifest.displayName,
                sourceName = parsedPackage.sourceName,
                currentGrants = persistedGrants,
            )
        } else if (userEnabled && target in packageManifest.extension.supportedTargets) {
            runCatching {
                val loadedRuntime = ExtensionRuntimeLoader.load(
                    parsedPackage = parsedPackage,
                    source = source,
                    target = target,
                )
                val resolvedHostGrants = extensionHostGrantStore.syncGrants(
                    extensionManifest = packageManifest.extension,
                    requestedPermissionKeys = packageManifest.permissionKeys,
                    suggestedGrants = loadedRuntime.hostGrants,
                )
                InstalledTExtensionExtension(
                    packageManifest = packageManifest,
                    sourceName = parsedPackage.sourceName,
                    runtimeExtension = loadedRuntime.extension,
                    hostGrants = resolvedHostGrants,
                    packageResources = loadedRuntime.packageResources,
                    runtimeHandle = loadedRuntime,
                )
            }.mapCatching { extension ->
                ExtensionFeaturePolicy.validateExtension(extension)
                extension
            }.onSuccess { extension ->
                loadedExtensions += extension
                isLoaded = true
            }.onFailure { error ->
                runtimeLoadError = error.message ?: error::class.simpleName ?: "Unknown error"
            }
        }

        discoveredPackages += DiscoveredTExtensionPackage(
            extension = packageManifest.extension,
            displayName = packageManifest.displayName,
            sourceName = parsedPackage.sourceName,
            packageVersion = packageManifest.version,
            apiVersion = packageManifest.apiVersion,
            description = packageManifest.description,
            hostGrants = persistedGrants,
            userEnabled = userEnabled,
            isLoaded = isLoaded,
            runtimeLoadError = runtimeLoadError,
        )
    }

    return InstalledExtensionScanResult(
        discoveredPackages = discoveredPackages.sortedBy { discovered -> discovered.displayName.lowercase() },
        loadedExtensions = loadedExtensions,
        permissionReviews = permissionReviews.sortedBy { review -> review.displayName.lowercase() },
        scanProblems = scanProblems.sortedBy { problem -> problem.sourceName.lowercase() },
    )
}

private fun extensionTargetSelectionScore(
    target: PlatformTarget,
    extension: com.dreamyloong.tlauncher.core.model.ExtensionManifest,
): Int {
    return when {
        extension.targetQualifier == target -> 4
        extension.targetQualifier == null && target in extension.supportedTargets -> 3
        target in extension.supportedTargets -> 2
        extension.targetQualifier == null -> 1
        else -> 0
    }
}

private fun compareExtensionPackageVersions(
    left: String,
    right: String,
): Int {
    val leftVersion = ComparableExtensionVersion.parse(left)
    val rightVersion = ComparableExtensionVersion.parse(right)
    val releaseComparison = compareReleaseIdentifiers(leftVersion.releaseParts, rightVersion.releaseParts)
    if (releaseComparison != 0) {
        return releaseComparison
    }
    val leftPrerelease = leftVersion.prereleaseParts
    val rightPrerelease = rightVersion.prereleaseParts
    if (leftPrerelease.isEmpty() && rightPrerelease.isEmpty()) {
        return 0
    }
    if (leftPrerelease.isEmpty()) {
        return 1
    }
    if (rightPrerelease.isEmpty()) {
        return -1
    }
    return comparePrereleaseIdentifiers(leftPrerelease, rightPrerelease)
}

private data class ComparableExtensionVersion(
    val releaseParts: List<String>,
    val prereleaseParts: List<String>,
) {
    companion object {
        fun parse(value: String): ComparableExtensionVersion {
            val withoutBuildMetadata = value.trim().substringBefore('+')
            val releaseText = withoutBuildMetadata.substringBefore('-')
            val prereleaseText = withoutBuildMetadata.substringAfter('-', missingDelimiterValue = "")
            return ComparableExtensionVersion(
                releaseParts = splitVersionParts(releaseText).ifEmpty { listOf("0") },
                prereleaseParts = splitVersionParts(prereleaseText),
            )
        }
    }
}

private fun compareReleaseIdentifiers(
    left: List<String>,
    right: List<String>,
): Int {
    val maxSize = maxOf(left.size, right.size)
    for (index in 0 until maxSize) {
        val result = compareReleaseIdentifier(
            left = left.getOrNull(index) ?: "0",
            right = right.getOrNull(index) ?: "0",
        )
        if (result != 0) {
            return result
        }
    }
    return 0
}

private fun comparePrereleaseIdentifiers(
    left: List<String>,
    right: List<String>,
): Int {
    val maxSize = maxOf(left.size, right.size)
    for (index in 0 until maxSize) {
        val leftPart = left.getOrNull(index) ?: return -1
        val rightPart = right.getOrNull(index) ?: return 1
        val result = comparePrereleaseIdentifier(leftPart, rightPart)
        if (result != 0) {
            return result
        }
    }
    return 0
}

private fun compareReleaseIdentifier(
    left: String,
    right: String,
): Int {
    return compareVersionIdentifier(
        left = left,
        right = right,
        numericIdentifierSortsAfterText = true,
    )
}

private fun comparePrereleaseIdentifier(
    left: String,
    right: String,
): Int {
    return compareVersionIdentifier(
        left = left,
        right = right,
        numericIdentifierSortsAfterText = false,
    )
}

private fun compareVersionIdentifier(
    left: String,
    right: String,
    numericIdentifierSortsAfterText: Boolean,
): Int {
    val leftNumber = left.toLongOrNull()
    val rightNumber = right.toLongOrNull()
    return when {
        leftNumber != null && rightNumber != null -> compareValues(leftNumber, rightNumber)
        leftNumber != null -> if (numericIdentifierSortsAfterText) 1 else -1
        rightNumber != null -> if (numericIdentifierSortsAfterText) -1 else 1
        else -> left.compareTo(right, ignoreCase = true)
    }
}

private fun splitVersionParts(value: String): List<String> {
    return value
        .split(Regex("[^A-Za-z0-9]+"))
        .map { part -> part.trim() }
        .filter { part -> part.isNotEmpty() }
}
