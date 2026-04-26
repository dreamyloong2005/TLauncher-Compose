package com.dreamyloong.tlauncher.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.extension.ExtensionCompatibilityIssue
import com.dreamyloong.tlauncher.core.extension.ExtensionCompatibilityResult
import com.dreamyloong.tlauncher.core.extension.HostGrant
import com.dreamyloong.tlauncher.core.extension.HostGrantState
import com.dreamyloong.tlauncher.core.extension.HostPermissionKey
import com.dreamyloong.tlauncher.core.model.ExtensionKind
import com.dreamyloong.tlauncher.core.model.LaunchSupportLevel
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.model.TemplateReleaseState
import com.dreamyloong.tlauncher.core.model.TemplateSourceType

class AppStrings(
    val language: SupportedLanguage,
    val appTitle: String,
    val homeCurrentGame: String,
    val homeTheme: String,
    val homeSettings: String,
    val homeNoGameTitle: String,
    val homeNoGameSubtitle: String,
    val homeSelectedGameSubtitle: String,
    val homeThemeSubtitle: String,
    val homePlatformPrefix: String,
    val libraryTitle: String,
    val librarySubtitle: String,
    val libraryInstanceList: String,
    val libraryNoGamesTitle: String,
    val libraryNoGamesSubtitle: String,
    val libraryNewGame: String,
    val libraryCurrent: String,
    val librarySwitch: String,
    val libraryDetails: String,
    val createGameTitle: String,
    val createGameSubtitle: String,
    val createGameAvailableTemplates: String,
    val createGameInstanceSettings: String,
    val createGameInstanceSettingsSubtitle: String,
    val createGameCreateInstance: String,
    val createGameAlreadyCreated: String,
    val detailTitle: String,
    val detailSubtitle: String,
    val detailInstance: String,
    val detailTemplateInfo: String,
    val detailOperations: String,
    val detailDeleteGame: String,
    val detailBackToLibrary: String,
    val detailTemplateNotes: String,
    val settingsTitle: String,
    val settingsSubtitle: String,
    val settingsGeneral: String,
    val settingsCurrentTheme: String,
    val settingsThemeSelection: String,
    val settingsThemePackages: String,
    val settingsPluginSdk: String,
    val settingsLanguage: String,
    val settingsCurrentLanguage: String,
    val settingsLoadOrder: String,
    val settingsAbout: String,
    val settingsCurrentInstance: String,
    val settingsInstancesCreated: String,
    val settingsVisibleTemplates: String,
    val settingsFollowSystem: String,
    val settingsExtension: String,
    val settingsDynamicBackgroundSupported: String,
    val settingsDynamicBackgroundStatic: String,
    val settingsLoadedPlugins: String,
    val settingsRegisteredExtensions: String,
    val extensionManagerTitle: String,
    val extensionManagerSubtitle: String,
    val extensionPriorityIncrease: String,
    val extensionPriorityDecrease: String,
    val extensionManagerThirdPartyComingSoon: String,
    val aboutTitle: String,
    val aboutSubtitle: String,
    val aboutIntroBody: String,
    val aboutAuthorSubtitle: String,
    val aboutCheckUpdates: String,
    val aboutCheckUpdatesSubtitle: String,
    val commonBack: String,
    val commonPlatformLabel: String,
    val commonCapabilities: String,
    val commonSource: String,
    val commonStatus: String,
    val commonEnabled: String,
    val commonDisabled: String,
    val commonThirdParty: String,
    val commonTemplate: String,
    val commonPackageId: String,
    val commonScene: String,
    val commonDynamicBackground: String,
    val commonAuthor: String,
    val commonVersion: String,
    val commonNone: String,
    val commonName: String,
    val commonDescription: String,
    val commonSave: String,
    val commonPath: String,
    val commonDelete: String,
) {
    fun templateCount(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$count 个兼容模板"
            SupportedLanguage.EN_US -> "$count compatible templates"
        }
    }

    fun themePackageCount(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$count 个主题包"
            SupportedLanguage.EN_US -> "$count theme packages"
        }
    }

    fun pluginCount(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$count 个已加载插件"
            SupportedLanguage.EN_US -> "$count loaded plugins"
        }
    }

    fun settingsAccountManagerTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "账号管理"
            SupportedLanguage.EN_US -> "Account Manager"
        }
    }

    fun settingsAccountManagerSubtitle(accountCount: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> if (accountCount == 0) {
                "添加和管理启动器保存的账号。"
            } else {
                "已保存 $accountCount 个账号。"
            }
            SupportedLanguage.EN_US -> if (accountCount == 0) {
                "Add and manage accounts saved by the launcher."
            } else {
                "$accountCount account(s) saved."
            }
        }
    }

    fun accountProviderName(provider: LauncherAccountProvider): String {
        return when (provider) {
            LauncherAccountProvider.STEAM -> "Steam"
        }
    }

    fun steamLoginModeName(mode: SteamAccountLoginMode): String {
        return when (mode) {
            SteamAccountLoginMode.CM -> "CM"
            SteamAccountLoginMode.WEB -> "Web"
            SteamAccountLoginMode.MOBILE -> "Mobile"
        }
    }

    fun accountManagerTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "账号管理器"
            SupportedLanguage.EN_US -> "Account Manager"
        }
    }

    fun accountManagerSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "查看已保存账号，或添加新的 Steam 账号。"
            SupportedLanguage.EN_US -> "Review saved accounts or add a new Steam account."
        }
    }

    fun accountManagerSection(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "账号操作"
            SupportedLanguage.EN_US -> "Account Actions"
        }
    }

    fun accountManagerAddAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "添加账号"
            SupportedLanguage.EN_US -> "Add Account"
        }
    }

    fun accountManagerAddAccountSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "选择账号类型和登录模式后登录。"
            SupportedLanguage.EN_US -> "Choose an account type and login mode, then sign in."
        }
    }

    fun accountManagerUnsupported(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前平台暂不支持账号管理。"
            SupportedLanguage.EN_US -> "Account management is not supported on this platform yet."
        }
    }

    fun accountManagerRefresh(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "刷新账号列表"
            SupportedLanguage.EN_US -> "Refresh Accounts"
        }
    }

    fun accountManagerRefreshing(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "正在刷新"
            SupportedLanguage.EN_US -> "Refreshing"
        }
    }

    fun accountManagerAccounts(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已保存账号"
            SupportedLanguage.EN_US -> "Saved Accounts"
        }
    }

    fun accountManagerNoAccountsTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "还没有账号"
            SupportedLanguage.EN_US -> "No Accounts Yet"
        }
    }

    fun accountManagerNoAccountsSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "点击添加账号来保存第一个 Steam 登录。"
            SupportedLanguage.EN_US -> "Tap Add Account to save your first Steam sign-in."
        }
    }

    fun accountManagerUnnamedAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "未命名账号"
            SupportedLanguage.EN_US -> "Unnamed Account"
        }
    }

    fun accountManagerAccountSubtitle(
        providerName: String,
        subjectId: String,
        active: Boolean,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$providerName ID：$subjectId" + if (active) "，当前使用" else ""
            SupportedLanguage.EN_US -> "$providerName ID: $subjectId" + if (active) ", active" else ""
        }
    }

    fun accountManagerActiveAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前使用"
            SupportedLanguage.EN_US -> "Active"
        }
    }

    fun accountManagerSavedAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已保存"
            SupportedLanguage.EN_US -> "Saved"
        }
    }

    fun accountManagerTokenState(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "令牌"
            SupportedLanguage.EN_US -> "Tokens"
        }
    }

    fun accountManagerTokenStateValue(
        hasRefreshToken: Boolean,
        hasAccessToken: Boolean,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> listOfNotNull(
                "刷新令牌".takeIf { hasRefreshToken },
                "访问令牌".takeIf { hasAccessToken },
            ).ifEmpty { listOf("无") }.joinToString("、")
            SupportedLanguage.EN_US -> listOfNotNull(
                "refresh".takeIf { hasRefreshToken },
                "access".takeIf { hasAccessToken },
            ).ifEmpty { listOf("none") }.joinToString(", ")
        }
    }

    fun accountManagerLoginModes(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "Steam 登录类型"
            SupportedLanguage.EN_US -> "Steam Login Types"
        }
    }

    fun accountManagerLoginModesValue(modes: Set<SteamAccountLoginMode>): String {
        return when {
            modes.isEmpty() -> when (language) {
                SupportedLanguage.ZH_CN -> "未知"
                SupportedLanguage.EN_US -> "Unknown"
            }
            else -> SteamAccountLoginMode.entries
                .filter { mode -> mode in modes }
                .joinToString(" / ") { mode -> steamLoginModeName(mode) }
        }
    }

    fun accountManagerDeleteAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "删除账号"
            SupportedLanguage.EN_US -> "Delete Account"
        }
    }

    fun accountManagerCancelDeleteAccount(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "取消"
            SupportedLanguage.EN_US -> "Cancel"
        }
    }

    fun accountManagerDeleteAccountTitle(displayName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "删除账号 $displayName？"
            SupportedLanguage.EN_US -> "Delete $displayName?"
        }
    }

    fun accountManagerDeleteAccountMessage(providerName: String, subjectId: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "这会删除该 $providerName 账号保存的所有登录模式：$subjectId。"
            SupportedLanguage.EN_US -> "This removes every saved login mode for this $providerName account: $subjectId."
        }
    }

    fun accountManagerDeleteAccountSucceeded(displayName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已删除账号：$displayName"
            SupportedLanguage.EN_US -> "Deleted account: $displayName"
        }
    }

    fun accountManagerDeleteAccountFailed(displayName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "删除账号失败：$displayName"
            SupportedLanguage.EN_US -> "Failed to delete account: $displayName"
        }
    }

    fun addAccountTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "添加账号"
            SupportedLanguage.EN_US -> "Add Account"
        }
    }

    fun addAccountSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "目前仅支持 Steam。可以选择 CM、Web 或 Mobile 登录模式。"
            SupportedLanguage.EN_US -> "Steam is supported for now. Choose CM, Web, or Mobile login."
        }
    }

    fun addAccountSourceSection(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "账号类型"
            SupportedLanguage.EN_US -> "Account Type"
        }
    }

    fun addAccountType(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "要添加的账号"
            SupportedLanguage.EN_US -> "Account to Add"
        }
    }

    fun addAccountOnlySteamSupported(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前版本只支持 Steam。"
            SupportedLanguage.EN_US -> "Only Steam is supported in this version."
        }
    }

    fun addAccountSteamLoginMode(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "Steam 登录模式"
            SupportedLanguage.EN_US -> "Steam Login Mode"
        }
    }

    fun addAccountSteamLoginModeSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "CM 更接近 Steam 客户端登录；Web 和 Mobile 使用对应平台身份。"
            SupportedLanguage.EN_US -> "CM is closest to Steam client login; Web and Mobile use those platform identities."
        }
    }

    fun addAccountCredentialsSection(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "登录信息"
            SupportedLanguage.EN_US -> "Credentials"
        }
    }

    fun addAccountSteamAccountName(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "Steam 账号名"
            SupportedLanguage.EN_US -> "Steam Account Name"
        }
    }

    fun addAccountSteamAccountNamePlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "输入 Steam 登录名"
            SupportedLanguage.EN_US -> "Enter Steam login name"
        }
    }

    fun addAccountSteamPassword(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "密码"
            SupportedLanguage.EN_US -> "Password"
        }
    }

    fun addAccountSteamPasswordPlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "输入密码"
            SupportedLanguage.EN_US -> "Enter password"
        }
    }

    fun addAccountSteamGuardCode(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "验证代码（如需要）"
            SupportedLanguage.EN_US -> "Verification Code (If Needed)"
        }
    }

    fun addAccountSteamGuardCodePlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "仅在 Steam 明确要求输入时填写"
            SupportedLanguage.EN_US -> "Fill this only if Steam explicitly asks for a code"
        }
    }

    fun addAccountSteamGuardCodeSupport(): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "CAuth 登录需要完成账号的二步验证。若你的账号使用 Steam 手机端确认，请先前往手机端批准；若 Steam 明确要求验证码，再在这里填写。"
            SupportedLanguage.EN_US ->
                "CAuth sign-in requires the account's two-factor verification. If your account uses Steam mobile confirmation, approve it there first. If Steam explicitly asks for a code, enter it here."
        }
    }

    fun addAccountSteamGuardRequiredTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "需要二步验证"
            SupportedLanguage.EN_US -> "Two-Factor Verification Required"
        }
    }

    fun addAccountSteamGuardRequiredMessage(): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "请先前往 Steam 对应的验证渠道完成确认。若你的账号使用 Steam 手机端确认，请先在手机上批准；若 Steam 明确要求验证码，再填写下方并重新登录。"
            SupportedLanguage.EN_US ->
                "Complete the required Steam verification first. If your account uses Steam mobile confirmation, approve it on your phone. If Steam explicitly asks for a code, enter it below and sign in again."
        }
    }

    fun addAccountSteamGuardCodeRequiredSupport(): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "当前登录正在等待你完成二步验证。请先去 Steam 对应的验证渠道确认；如果 Steam 另外要求验证码，再在这里填写并重试。"
            SupportedLanguage.EN_US ->
                "This sign-in is waiting for two-factor verification. Complete it in the required Steam verification channel first. If Steam additionally asks for a code, enter it here and retry."
        }
    }

    fun addAccountLogin(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "登录并保存"
            SupportedLanguage.EN_US -> "Sign In and Save"
        }
    }

    fun addAccountLoginWithSteamGuard(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "完成验证后再次登录"
            SupportedLanguage.EN_US -> "Sign In Again After Verification"
        }
    }

    fun addAccountCancelLogin(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "取消登录"
            SupportedLanguage.EN_US -> "Cancel Sign-In"
        }
    }

    fun addAccountLoggingIn(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "轮询确认中"
            SupportedLanguage.EN_US -> "Waiting for Confirmation"
        }
    }

    fun addAccountPollingConfirmationTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "轮询中"
            SupportedLanguage.EN_US -> "Polling"
        }
    }

    fun addAccountPollingConfirmationMessage(): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "请打开 Steam 手机端确认这次登录。如果 Steam 另外要求验证码，再填写下方并重新登录。"
            SupportedLanguage.EN_US ->
                "Open the Steam mobile app and confirm this sign-in. If Steam additionally asks for a code, enter it below and sign in again."
        }
    }

    fun addAccountCancelConfirmationTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "退出并取消登录？"
            SupportedLanguage.EN_US -> "Leave and Cancel Sign-In?"
        }
    }

    fun addAccountCancelConfirmationMessage(): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "当前正在等待 Steam 登录确认。退出后会停止等待这次登录结果。"
            SupportedLanguage.EN_US ->
                "Steam sign-in is still waiting for confirmation. Leaving now will stop waiting for this sign-in result."
        }
    }

    fun addAccountCancelConfirmationStay(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "继续等待"
            SupportedLanguage.EN_US -> "Keep Waiting"
        }
    }

    fun addAccountCancelConfirmationExit(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "退出并取消"
            SupportedLanguage.EN_US -> "Leave and Cancel"
        }
    }

    fun addAccountLoginCanceledMessage(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已取消这次登录。"
            SupportedLanguage.EN_US -> "This sign-in was canceled."
        }
    }

    fun addAccountLoginSucceeded(displayName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已添加账号：$displayName"
            SupportedLanguage.EN_US -> "Added account: $displayName"
        }
    }

    fun extensionCount(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$count 个已注册扩展"
            SupportedLanguage.EN_US -> "$count registered extensions"
        }
    }

    fun extensionManagerSummary(totalCount: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已安装 $totalCount 个扩展包"
            SupportedLanguage.EN_US -> "$totalCount extension packages installed"
        }
    }

    fun createTemplateSelectorTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "选择模板"
            SupportedLanguage.EN_US -> "Choose Template"
        }
    }

    fun createTemplateSelectorSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "先选择一个模板，再填写下面这个实例的名称和描述。"
            SupportedLanguage.EN_US -> "Choose a template first, then fill in the name and description for this instance."
        }
    }

    fun createTemplateSelectorPlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "未选择模板"
            SupportedLanguage.EN_US -> "No Template Selected"
        }
    }

    fun createTemplateSelectorHint(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "展开后可从 $count 个兼容模板里选择一个。"
            SupportedLanguage.EN_US -> "Expand to choose from $count compatible templates."
        }
    }

    fun createTemplateSelectionRequired(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "必须先选择模板"
            SupportedLanguage.EN_US -> "Select a template first"
        }
    }

    fun createTemplateSelectionRequiredDetail(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "还没有选择模板，所以当前不能填写实例信息，也不能创建实例。"
            SupportedLanguage.EN_US -> "No template is selected yet, so instance details cannot be edited and creation stays disabled."
        }
    }

    fun createGameSelectTemplateFirst(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "请先选择模板"
            SupportedLanguage.EN_US -> "Select Template First"
        }
    }

    fun createGameNamePlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "输入实例名称"
            SupportedLanguage.EN_US -> "Enter instance name"
        }
    }

    fun createGameDescriptionPlaceholder(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "输入实例描述"
            SupportedLanguage.EN_US -> "Enter instance description"
        }
    }

    fun createGameUsingTemplateName(templateName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前模板：$templateName"
            SupportedLanguage.EN_US -> "Current template: $templateName"
        }
    }

    fun detailTemplateUnavailableTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "模板包未启用"
            SupportedLanguage.EN_US -> "Template Package Not Enabled"
        }
    }

    fun detailTemplateUnavailableDetail(templatePackageId: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "这个实例由模板包 $templatePackageId 创建。请安装或启用这个模板包后再查看模板详情。"
            SupportedLanguage.EN_US -> "This instance was created by template package $templatePackageId. Install or enable that package to view template details."
        }
    }

    fun fileCount(count: Int): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "$count 个文件"
            SupportedLanguage.EN_US -> "$count files"
        }
    }

    fun aboutCheckUpdatesCheckingSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "正在连接 GitHub Releases 检查最新版本。"
            SupportedLanguage.EN_US -> "Checking the latest version from GitHub Releases."
        }
    }

    fun aboutCheckUpdatesLatestSubtitle(version: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前已是最新版本：$version。点击可再次检查。"
            SupportedLanguage.EN_US -> "You're already on the latest version: $version. Tap to check again."
        }
    }

    fun aboutCheckUpdatesInternalBuildSubtitle(currentVersion: String, latestVersion: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "你可能正在使用内部/测试版本：当前 $currentVersion，高于最新公开版本 $latestVersion。点击可再次检查。"
            SupportedLanguage.EN_US -> "You may be using an internal/test build: current $currentVersion is newer than the latest public release $latestVersion. Tap to check again."
        }
    }

    fun aboutCheckUpdatesAvailableSubtitle(version: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "发现新版本：$version。下方可以直接打开 GitHub Release。"
            SupportedLanguage.EN_US -> "New version available: $version. Open the GitHub Release below."
        }
    }

    fun aboutCheckUpdatesFailedSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "无法从 GitHub Releases 取得最新版本信息，点击重试。"
            SupportedLanguage.EN_US -> "Couldn't reach GitHub Releases for the latest version. Tap to retry."
        }
    }

    fun aboutOpenReleasePageAction(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "打开 GitHub Release"
            SupportedLanguage.EN_US -> "Open GitHub Release"
        }
    }

    fun platformName(target: PlatformTarget): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> when (target) {
                PlatformTarget.WINDOWS -> "Windows"
                PlatformTarget.ANDROID -> "Android"
                PlatformTarget.IOS -> "iOS"
                PlatformTarget.MACOS -> "macOS"
            }
            SupportedLanguage.EN_US -> when (target) {
                PlatformTarget.WINDOWS -> "Windows"
                PlatformTarget.ANDROID -> "Android"
                PlatformTarget.IOS -> "iOS"
                PlatformTarget.MACOS -> "macOS"
            }
        }
    }

    fun sourceName(source: TemplateSourceType): String {
        return when (source) {
            TemplateSourceType.OFFICIAL -> if (language == SupportedLanguage.ZH_CN) "官方" else "Official"
            TemplateSourceType.COMMUNITY -> if (language == SupportedLanguage.ZH_CN) "社区" else "Community"
            TemplateSourceType.PLUGIN -> if (language == SupportedLanguage.ZH_CN) "插件" else "Plugin"
        }
    }

    fun extensionKindName(kind: ExtensionKind): String {
        return when (kind) {
            ExtensionKind.TEMPLATE -> if (language == SupportedLanguage.ZH_CN) "模板" else "Template"
            ExtensionKind.THEME -> if (language == SupportedLanguage.ZH_CN) "主题" else "Theme"
            ExtensionKind.PLUGIN -> if (language == SupportedLanguage.ZH_CN) "插件" else "Plugin"
        }
    }

    fun statusName(state: TemplateReleaseState): String {
        return when (state) {
            TemplateReleaseState.DRAFT -> if (language == SupportedLanguage.ZH_CN) "草稿" else "Draft"
            TemplateReleaseState.EXPERIMENTAL -> if (language == SupportedLanguage.ZH_CN) "实验性" else "Experimental"
            TemplateReleaseState.READY -> if (language == SupportedLanguage.ZH_CN) "可用" else "Ready"
        }
    }

    fun supportLevelName(level: LaunchSupportLevel): String {
        return when (level) {
            LaunchSupportLevel.BROWSE_ONLY -> if (language == SupportedLanguage.ZH_CN) "仅浏览" else "Browse only"
            LaunchSupportLevel.INSTALLABLE -> if (language == SupportedLanguage.ZH_CN) "可安装" else "Installable"
            LaunchSupportLevel.LAUNCHABLE -> if (language == SupportedLanguage.ZH_CN) "可启动" else "Launchable"
        }
    }

    fun sceneName(kind: String): String {
        return when (kind) {
            "static-surface" -> if (language == SupportedLanguage.ZH_CN) "静态背景" else "Static surface"
            "daylight-scene" -> if (language == SupportedLanguage.ZH_CN) "白天场景" else "Daylight scene"
            "night-scene" -> if (language == SupportedLanguage.ZH_CN) "黑夜场景" else "Night scene"
            else -> kind
        }
    }

    fun languageName(language: SupportedLanguage): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "简体中文"
            SupportedLanguage.EN_US -> "English"
        }
    }

    fun extensionCompatibilityLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "SDK 兼容性"
            SupportedLanguage.EN_US -> "SDK Compatibility"
        }
    }

    fun extensionManagerLoadPackageTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "加载扩展"
            SupportedLanguage.EN_US -> "Load Extension"
        }
    }

    fun extensionManagerLoadPackageSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "选择一个 .textension 文件并在注册阶段执行 manifest、kind、capability 和 feature 硬校验。"
            SupportedLanguage.EN_US -> "Choose a .textension file and run strict manifest, kind, capability, and feature validation during registration."
        }
    }

    fun extensionManagerLoadPackageAction(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "选择 .textension 文件"
            SupportedLanguage.EN_US -> "Choose .textension File"
        }
    }

    fun extensionManagerLoadUnsupported(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前平台还没有接入本地扩展包选择器。"
            SupportedLanguage.EN_US -> "A local extension package picker is not available on this platform yet."
        }
    }

    fun extensionManagerLoadSuccess(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已加载扩展：$name"
            SupportedLanguage.EN_US -> "Loaded extension: $name"
        }
    }

    fun extensionManagerLoadFailed(message: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "加载失败：$message"
            SupportedLanguage.EN_US -> "Load failed: $message"
        }
    }

    fun extensionManagerLoadCancelled(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已取消加载扩展。"
            SupportedLanguage.EN_US -> "Extension loading was cancelled."
        }
    }

    fun extensionManagerInstallSuccess(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已安装扩展包：$name"
            SupportedLanguage.EN_US -> "Installed extension package: $name"
        }
    }

    fun extensionManagerReplacePackageTitle(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "覆盖安装 $name？"
            SupportedLanguage.EN_US -> "Overwrite $name?"
        }
    }

    fun extensionManagerReplacePackageMessage(
        existingVersion: String,
        incomingVersion: String,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "已安装版本 $existingVersion。当前选择的版本为 $incomingVersion，不高于已安装版本。是否删除原扩展包并安装这个版本？"

            SupportedLanguage.EN_US ->
                "Version $existingVersion is already installed. The selected version is $incomingVersion, which is not newer. Delete the existing package and install this version?"
        }
    }

    fun extensionManagerUpdatePackageTitle(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "更新 $name？"
            SupportedLanguage.EN_US -> "Update $name?"
        }
    }

    fun extensionManagerUpdatePackageMessage(
        name: String,
        existingVersion: String,
        incomingVersion: String,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN ->
                "是否把 $name 从 $existingVersion 更新至 $incomingVersion？"

            SupportedLanguage.EN_US ->
                "Update $name from $existingVersion to $incomingVersion?"
        }
    }

    fun extensionManagerReplacePackageConfirm(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "覆盖安装"
            SupportedLanguage.EN_US -> "Overwrite"
        }
    }

    fun extensionManagerUpdatePackageConfirm(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "安装更新"
            SupportedLanguage.EN_US -> "Install Update"
        }
    }

    fun extensionManagerReplacePackageCancel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "取消"
            SupportedLanguage.EN_US -> "Cancel"
        }
    }

    fun extensionManagerInstallUpdated(
        name: String,
        previousVersion: String,
        nextVersion: String,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已更新扩展包：$name ($previousVersion -> $nextVersion)"
            SupportedLanguage.EN_US -> "Updated extension package: $name ($previousVersion -> $nextVersion)"
        }
    }

    fun extensionManagerInstallOverwritten(
        name: String,
        previousVersion: String,
        nextVersion: String,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已覆盖安装扩展包：$name ($previousVersion -> $nextVersion)"
            SupportedLanguage.EN_US -> "Overwrote extension package: $name ($previousVersion -> $nextVersion)"
        }
    }

    fun extensionPermissionReviewTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "权限确认"
            SupportedLanguage.EN_US -> "Permission Review"
        }
    }

    fun extensionPermissionReviewSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已安装扩展会在启动时自动扫描。请求宿主权限的扩展需要先确认，确认后才会真正加载运行时。"
            SupportedLanguage.EN_US -> "Installed extensions are scanned automatically at startup. Extensions that request host permissions must be reviewed before their runtime is loaded."
        }
    }

    fun extensionPermissionApproveAction(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "允许"
            SupportedLanguage.EN_US -> "Allow"
        }
    }

    fun extensionPermissionDenyAction(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "拒绝"
            SupportedLanguage.EN_US -> "Deny"
        }
    }

    fun extensionPermissionGrantedStatus(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已允许，将在下次扫描时自动加载。"
            SupportedLanguage.EN_US -> "Allowed. The extension will load automatically on the next scan."
        }
    }

    fun extensionPermissionPendingStatus(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "还没有完成权限确认，所以运行时暂时不会加载。"
            SupportedLanguage.EN_US -> "Permission review is still pending, so the runtime is not loaded yet."
        }
    }

    fun extensionInstalledToggleTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "启用扩展"
            SupportedLanguage.EN_US -> "Enable Extension"
        }
    }

    fun extensionDetailsTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "扩展详情"
            SupportedLanguage.EN_US -> "Extension Details"
        }
    }

    fun extensionInstalledToggleSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "关闭后会保留安装包，但启动扫描时不会加载运行时。"
            SupportedLanguage.EN_US -> "Turn this off to keep the package installed while skipping runtime loading during startup scans."
        }
    }

    fun extensionManagerInstalledPackagesTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已安装扩展包"
            SupportedLanguage.EN_US -> "Installed Extension Packages"
        }
    }

    fun extensionManagerInstalledPackagesSubtitle(installedPackageDirectory: String?): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> buildString {
                append("已安装的 .textension 会复制到宿主的扩展包目录，并在启动时自动扫描。")
                if (!installedPackageDirectory.isNullOrBlank()) {
                    append('\n')
                    append("目录：")
                    append(installedPackageDirectory)
                }
                append('\n')
                append("逐项权限开关会直接决定运行时是否允许加载，用户手动安装的包也可以在这里直接删除。")
            }

            SupportedLanguage.EN_US -> buildString {
                append("Installed .textension packages are copied into the host package directory and scanned automatically at startup.")
                if (!installedPackageDirectory.isNullOrBlank()) {
                    append('\n')
                    append("Directory: ")
                    append(installedPackageDirectory)
                }
                append('\n')
                append("Per-permission toggles directly control whether the runtime is allowed to load, and user-installed packages can also be deleted here.")
            }
        }
    }

    fun extensionManagerScanProblemsTitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "扩展包扫描问题"
            SupportedLanguage.EN_US -> "Extension Package Scan Problems"
        }
    }

    fun extensionManagerScanProblemsSubtitle(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "这些 .textension 文件位于已安装扩展目录中，但在扫描时未能通过解析或基础校验。"
            SupportedLanguage.EN_US -> "These .textension files are present in the installed package directory, but failed parsing or baseline validation during scanning."
        }
    }

    fun extensionManagerScanProblemTitle(sourceName: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "扫描失败：$sourceName"
            SupportedLanguage.EN_US -> "Scan failed: $sourceName"
        }
    }

    fun extensionManagerInstalledPackageManagedNote(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "这个扩展由已安装的 .textension 包管理，停用不会删除安装包。"
            SupportedLanguage.EN_US -> "This extension is managed by an installed .textension package, and disabling it does not delete the package."
        }
    }

    fun extensionManagerDeletePackageAction(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "删除扩展包"
            SupportedLanguage.EN_US -> "Delete Package"
        }
    }

    fun extensionManagerDeletePackageSuccess(packageLabel: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已删除扩展包：$packageLabel"
            SupportedLanguage.EN_US -> "Deleted extension package: $packageLabel"
        }
    }

    fun extensionManagerDeletePackageFailed(packageLabel: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "删除扩展包失败：$packageLabel"
            SupportedLanguage.EN_US -> "Failed to delete extension package: $packageLabel"
        }
    }

    fun extensionPermissionToggleReason(reason: String?): String {
        val trimmedReason = reason?.trim().orEmpty()
        if (trimmedReason.isBlank()) {
            return when (language) {
                SupportedLanguage.ZH_CN -> "未记录额外说明。"
                SupportedLanguage.EN_US -> "No additional note recorded."
            }
        }
        return trimmedReason
    }

    fun extensionPermissionToggleChanged(
        extensionName: String,
        permissionName: String,
        granted: Boolean,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> {
                if (granted) {
                    "已允许 $extensionName 的权限：$permissionName"
                } else {
                    "已拒绝 $extensionName 的权限：$permissionName"
                }
            }

            SupportedLanguage.EN_US -> {
                if (granted) {
                    "Allowed $permissionName for $extensionName"
                } else {
                    "Denied $permissionName for $extensionName"
                }
            }
        }
    }

    fun extensionEnabledChanged(
        extensionName: String,
        enabled: Boolean,
    ): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> {
                if (enabled) {
                    "已启用扩展：$extensionName"
                } else {
                    "已停用扩展：$extensionName"
                }
            }

            SupportedLanguage.EN_US -> {
                if (enabled) {
                    "Enabled extension: $extensionName"
                } else {
                    "Disabled extension: $extensionName"
                }
            }
        }
    }

    fun extensionPermissionDeniedStatus(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前有被拒绝的权限，所以运行时仍然不会加载。"
            SupportedLanguage.EN_US -> "Some permissions are denied, so the runtime will remain unloaded."
        }
    }

    fun extensionPermissionApproveSuccess(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已允许扩展权限：$name"
            SupportedLanguage.EN_US -> "Allowed extension permissions: $name"
        }
    }

    fun extensionPermissionDenySuccess(name: String): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "已拒绝扩展权限：$name"
            SupportedLanguage.EN_US -> "Denied extension permissions: $name"
        }
    }

    fun extensionRequestedPermissionsLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "请求的宿主权限"
            SupportedLanguage.EN_US -> "Requested Host Permissions"
        }
    }

    fun extensionGrantedPermissionsLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "当前宿主授权"
            SupportedLanguage.EN_US -> "Current Host Grants"
        }
    }

    fun extensionPackageVersionLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "扩展包版本"
            SupportedLanguage.EN_US -> "Package Version"
        }
    }

    fun extensionSdkApiVersionLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "SDK API"
            SupportedLanguage.EN_US -> "SDK API"
        }
    }

    fun extensionDescriptionLabel(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "说明"
            SupportedLanguage.EN_US -> "Description"
        }
    }

    fun extensionDescriptionFallback(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "未提供额外说明。"
            SupportedLanguage.EN_US -> "No additional description provided."
        }
    }

    fun extensionSourceSummary(sourceName: String?): String {
        return if (sourceName.isNullOrBlank()) {
            if (language == SupportedLanguage.ZH_CN) "未知来源的扩展" else "Extension with unknown source"
        } else {
            if (language == SupportedLanguage.ZH_CN) {
                ".textension 包：$sourceName"
            } else {
                ".textension package: $sourceName"
            }
        }
    }

    fun extensionPermissionBehaviorNote(): String {
        return when (language) {
            SupportedLanguage.ZH_CN -> "权限说明：只要存在未允许的宿主权限，这个扩展的运行时就不会被加载。"
            SupportedLanguage.EN_US -> "Permission note: the runtime stays unloaded until every requested host permission is allowed."
        }
    }

    fun hostPermissionName(permissionKey: HostPermissionKey): String {
        return when (permissionKey) {
            HostPermissionKey.READ_GAME_LIBRARY ->
                if (language == SupportedLanguage.ZH_CN) "读取游戏库" else "Read Game Library"

            HostPermissionKey.WRITE_GAME_LIBRARY ->
                if (language == SupportedLanguage.ZH_CN) "写入游戏库" else "Write Game Library"

            HostPermissionKey.OPEN_EXTERNAL_URLS ->
                if (language == SupportedLanguage.ZH_CN) "打开外部链接" else "Open External URLs"

            HostPermissionKey.NETWORK_ACCESS ->
                if (language == SupportedLanguage.ZH_CN) "网络访问" else "Network Access"

            HostPermissionKey.FILESYSTEM_READ ->
                if (language == SupportedLanguage.ZH_CN) "文件系统读取" else "Filesystem Read"

            HostPermissionKey.FILESYSTEM_WRITE ->
                if (language == SupportedLanguage.ZH_CN) "文件系统写入" else "Filesystem Write"
        }
    }

    fun hostGrantStateName(state: HostGrantState): String {
        return when (state) {
            HostGrantState.GRANTED -> if (language == SupportedLanguage.ZH_CN) "已授权" else "Granted"
            HostGrantState.DENIED -> if (language == SupportedLanguage.ZH_CN) "已拒绝" else "Denied"
        }
    }

    fun hostGrantSummary(grants: Set<HostGrant>): String {
        if (grants.isEmpty()) {
            return commonNone
        }
        return grants.sortedBy { it.permissionKey.name }.joinToString { grant ->
            "${hostPermissionName(grant.permissionKey)} (${hostGrantStateName(grant.state)})"
        }
    }

    fun extensionCompatibilitySummary(result: ExtensionCompatibilityResult): String {
        return when (result) {
            is ExtensionCompatibilityResult.Compatible -> when (language) {
                SupportedLanguage.ZH_CN -> "兼容"
                SupportedLanguage.EN_US -> "Compatible"
            }

            is ExtensionCompatibilityResult.Incompatible -> {
                val reasons = result.issues.joinToString(" / ") { issue ->
                    extensionCompatibilityIssueName(issue)
                }
                when (language) {
                    SupportedLanguage.ZH_CN -> "不兼容（$reasons）"
                    SupportedLanguage.EN_US -> "Incompatible ($reasons)"
                }
            }
        }
    }

    fun extensionRuntimeStatusSummary(
        kind: ExtensionKind,
        currentTarget: PlatformTarget,
        supportedTargets: Set<PlatformTarget>,
        userEnabled: Boolean,
        compatibility: ExtensionCompatibilityResult,
        runtimeLoaded: Boolean,
        requestedPermissions: Set<HostPermissionKey>,
        grantedPermissions: Set<HostGrant>,
        runtimeLoadError: String?,
    ): String {
        if (supportedTargets.none { target -> target == currentTarget }) {
            return if (language == SupportedLanguage.ZH_CN) "当前平台不支持" else "Unsupported on this platform"
        }
        if (compatibility is ExtensionCompatibilityResult.Incompatible) {
            return extensionCompatibilitySummary(compatibility)
        }
        if (!userEnabled) {
            return if (language == SupportedLanguage.ZH_CN) "已安装，但已被手动停用" else "Installed, but manually disabled"
        }
        val allPermissionsGranted = requestedPermissions.all { permissionKey ->
            grantedPermissions.any { grant -> grant.permissionKey == permissionKey && grant.isGranted }
        }
        if (requestedPermissions.isNotEmpty() && !allPermissionsGranted) {
            val hasDeniedGrant = grantedPermissions.any { grant -> grant.state == HostGrantState.DENIED }
            return when (language) {
                SupportedLanguage.ZH_CN -> if (hasDeniedGrant) {
                    "等待权限确认（存在已拒绝项）"
                } else {
                    "等待权限确认"
                }

                SupportedLanguage.EN_US -> if (hasDeniedGrant) {
                    "Waiting for permission review (some denied)"
                } else {
                    "Waiting for permission review"
                }
            }
        }
        if (runtimeLoaded) {
            return if (language == SupportedLanguage.ZH_CN) "已加载运行时" else "Runtime loaded"
        }
        if (!runtimeLoadError.isNullOrBlank()) {
            return when (language) {
                SupportedLanguage.ZH_CN -> "扩展加载或执行失败：$runtimeLoadError"
                SupportedLanguage.EN_US -> "Extension load or execution failed: $runtimeLoadError"
            }
        }
        return when (kind) {
            ExtensionKind.TEMPLATE, ExtensionKind.THEME -> {
                if (language == SupportedLanguage.ZH_CN) "已启用" else "Enabled"
            }

            ExtensionKind.PLUGIN -> {
                if (language == SupportedLanguage.ZH_CN) "已安装，等待下次扫描" else "Installed, waiting for the next scan"
            }
        }
    }

    private fun extensionCompatibilityIssueName(issue: ExtensionCompatibilityIssue): String {
        return when (issue) {
            ExtensionCompatibilityIssue.PACKAGE_FORMAT_VERSION_MISMATCH ->
                if (language == SupportedLanguage.ZH_CN) "包格式版本不匹配" else "Package format version mismatch"

            ExtensionCompatibilityIssue.SDK_API_VERSION_MISMATCH ->
                if (language == SupportedLanguage.ZH_CN) "SDK API 版本不匹配" else "SDK API version mismatch"

            ExtensionCompatibilityIssue.SDK_API_VERSION_RANGE_INVALID ->
                if (language == SupportedLanguage.ZH_CN) "SDK API 版本范围无效" else "SDK API version range is invalid"

            ExtensionCompatibilityIssue.MIN_SDK_API_VERSION_TOO_NEW ->
                if (language == SupportedLanguage.ZH_CN) "需要更新的宿主 SDK" else "Requires a newer host SDK"

            ExtensionCompatibilityIssue.TARGET_SDK_API_VERSION_TOO_OLD ->
                if (language == SupportedLanguage.ZH_CN) "目标 SDK 版本过旧" else "Target SDK version is too old"
        }
    }
}

fun appStringsFor(language: SupportedLanguage): AppStrings {
    return when (language) {
        SupportedLanguage.ZH_CN -> AppStrings(
            language = language,
            appTitle = "TLauncher",
            homeCurrentGame = "当前游戏",
            homeTheme = "主题",
            homeSettings = "设置",
            homeNoGameTitle = "还没有游戏",
            homeNoGameSubtitle = "点击进入游戏库，先创建一个实例。",
            homeSelectedGameSubtitle = "已选中。模板扩展会在这里追加可用操作。",
            homeThemeSubtitle = "主题选择和主题包信息现在放在设置页里。",
            homePlatformPrefix = "当前平台：",
            libraryTitle = "游戏库",
            librarySubtitle = "点击卡片切换当前游戏，点右侧详情查看实例信息。",
            libraryInstanceList = "实例列表",
            libraryNoGamesTitle = "还没有游戏",
            libraryNoGamesSubtitle = "先新建一个实例，再由对应模板提供后续操作。",
            libraryNewGame = "新建游戏",
            libraryCurrent = "当前",
            librarySwitch = "切换",
            libraryDetails = "详情",
            createGameTitle = "新建游戏",
            createGameSubtitle = "系统只展示当前平台兼容的模板。",
            createGameAvailableTemplates = "可用模板",
            createGameInstanceSettings = "实例设置",
            createGameInstanceSettingsSubtitle = "下面的名称和描述只会用于这个即将创建的实例。",
            createGameCreateInstance = "创建实例",
            createGameAlreadyCreated = "已创建",
            detailTitle = "游戏详情",
            detailSubtitle = "这里管理实例信息；模板扩展会追加自己的详情和操作。",
            detailInstance = "实例",
            detailTemplateInfo = "模板信息",
            detailOperations = "操作",
            detailDeleteGame = "删除游戏",
            detailBackToLibrary = "返回游戏库",
            detailTemplateNotes = "模板备注",
            settingsTitle = "设置",
            settingsSubtitle = "管理语言、主题和扩展。",
            settingsGeneral = "通用设置",
            settingsCurrentTheme = "当前主题",
            settingsThemeSelection = "主题选择",
            settingsThemePackages = "主题包",
            settingsPluginSdk = "插件 SDK",
            settingsLanguage = "语言",
            settingsCurrentLanguage = "当前语言",
            settingsLoadOrder = "扩展管理",
            settingsAbout = "关于",
            settingsCurrentInstance = "当前实例",
            settingsInstancesCreated = "实例数量",
            settingsVisibleTemplates = "可见模板",
            settingsFollowSystem = "跟随系统",
            settingsExtension = "扩展",
            settingsDynamicBackgroundSupported = "支持",
            settingsDynamicBackgroundStatic = "静态",
            settingsLoadedPlugins = "已加载插件",
            settingsRegisteredExtensions = "已注册扩展",
            extensionManagerTitle = "扩展管理器",
            extensionManagerSubtitle = "这里管理扩展的加载优先级、权限确认和启用状态。",
            extensionPriorityIncrease = "提高优先级",
            extensionPriorityDecrease = "降低优先级",
            extensionManagerThirdPartyComingSoon = "第三方扩展的删除入口以后再补，权限和启用状态已经可以在这里管理。",
            aboutTitle = "关于",
            aboutSubtitle = "项目介绍、版本信息和相关链接。",
            aboutIntroBody = "TLauncher 是一个跨平台、模块化的游戏启动器，目标是把模板、主题和插件组合成可扩展的启动体验。",
            aboutAuthorSubtitle = "点击打开 GitHub 主页。",
            aboutCheckUpdates = "检查更新",
            aboutCheckUpdatesSubtitle = "从 GitHub Releases 检查最新版本。",
            commonBack = "返回",
            commonPlatformLabel = "平台",
            commonCapabilities = "能力",
            commonSource = "来源",
            commonStatus = "状态",
            commonEnabled = "启用",
            commonDisabled = "禁用",
            commonThirdParty = "第三方",
            commonTemplate = "模板",
            commonPackageId = "扩展 ID",
            commonScene = "场景",
            commonDynamicBackground = "动态背景",
            commonAuthor = "作者",
            commonVersion = "版本",
            commonNone = "无",
            commonName = "名称",
            commonDescription = "描述",
            commonSave = "保存",
            commonPath = "路径",
            commonDelete = "删除",
        )
        SupportedLanguage.EN_US -> AppStrings(
            language = language,
            appTitle = "TLauncher",
            homeCurrentGame = "Current Game",
            homeTheme = "Theme",
            homeSettings = "Settings",
            homeNoGameTitle = "No Games Yet",
            homeNoGameSubtitle = "Open the library and create your first instance.",
            homeSelectedGameSubtitle = "Selected. Template extensions can add available actions here.",
            homeThemeSubtitle = "Theme selection and theme package details now live in Settings.",
            homePlatformPrefix = "Platform: ",
            libraryTitle = "Library",
            librarySubtitle = "Tap a card to make it current, or open details on the right.",
            libraryInstanceList = "Instances",
            libraryNoGamesTitle = "No Games Yet",
            libraryNoGamesSubtitle = "Create an instance first, then let its template provide the next actions.",
            libraryNewGame = "Create Game",
            libraryCurrent = "Current",
            librarySwitch = "Switch",
            libraryDetails = "Details",
            createGameTitle = "Create Game",
            createGameSubtitle = "Only templates compatible with the current platform are shown.",
            createGameAvailableTemplates = "Available Templates",
            createGameInstanceSettings = "Instance Settings",
            createGameInstanceSettingsSubtitle = "The name and description below only apply to the instance created from this template.",
            createGameCreateInstance = "Create Instance",
            createGameAlreadyCreated = "Already Created",
            detailTitle = "Game Details",
            detailSubtitle = "Manage instance info here; template extensions can add their own details and actions.",
            detailInstance = "Instance",
            detailTemplateInfo = "Template Info",
            detailOperations = "Actions",
            detailDeleteGame = "Delete Game",
            detailBackToLibrary = "Back to Library",
            detailTemplateNotes = "Template Notes",
            settingsTitle = "Settings",
            settingsSubtitle = "Manage language, theme, and extensions.",
            settingsGeneral = "General Settings",
            settingsCurrentTheme = "Current Theme",
            settingsThemeSelection = "Theme Selection",
            settingsThemePackages = "Theme Packages",
            settingsPluginSdk = "Plugin SDK",
            settingsLanguage = "Language",
            settingsCurrentLanguage = "Current Language",
            settingsLoadOrder = "Extension Manager",
            settingsAbout = "About",
            settingsCurrentInstance = "Current Instance",
            settingsInstancesCreated = "Instances",
            settingsVisibleTemplates = "Visible Templates",
            settingsFollowSystem = "Follow System",
            settingsExtension = "Extension",
            settingsDynamicBackgroundSupported = "Supported",
            settingsDynamicBackgroundStatic = "Static",
            settingsLoadedPlugins = "Loaded Plugins",
            settingsRegisteredExtensions = "Registered Extensions",
            extensionManagerTitle = "Extension Manager",
            extensionManagerSubtitle = "This page manages extension load priority, permission review, and enabled state.",
            extensionPriorityIncrease = "Increase Priority",
            extensionPriorityDecrease = "Decrease Priority",
            extensionManagerThirdPartyComingSoon = "Delete controls for third-party extensions can come later. Permissions and enabled state are managed here now.",
            aboutTitle = "About",
            aboutSubtitle = "Project intro, version info, and related links.",
            aboutIntroBody = "TLauncher is a cross-platform, modular game launcher aimed at building an extensible launch experience from templates, themes, and plugins.",
            aboutAuthorSubtitle = "Tap to open the GitHub profile.",
            aboutCheckUpdates = "Check for Updates",
            aboutCheckUpdatesSubtitle = "Check the latest version from GitHub Releases.",
            commonBack = "Back",
            commonPlatformLabel = "Platform",
            commonCapabilities = "Capabilities",
            commonSource = "Source",
            commonStatus = "Status",
            commonEnabled = "Enabled",
            commonDisabled = "Disabled",
            commonThirdParty = "Third-party",
            commonTemplate = "Template",
            commonPackageId = "Extension ID",
            commonScene = "Scene",
            commonDynamicBackground = "Dynamic Background",
            commonAuthor = "Author",
            commonVersion = "Version",
            commonNone = "None",
            commonName = "Name",
            commonDescription = "Description",
            commonSave = "Save",
            commonPath = "Path",
            commonDelete = "Delete",
        )
    }
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> {
    appStringsFor(SupportedLanguage.EN_US)
}

@Composable
fun ProvideAppStrings(
    strings: AppStrings,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppStrings provides strings,
        content = content,
    )
}
