package com.dreamyloong.tlauncher.feature.settings

import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.account.LauncherAccountLoginStatus
import com.dreamyloong.tlauncher.core.account.LauncherAccountProvider
import com.dreamyloong.tlauncher.core.account.SteamAccountLoginMode
import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageActionRegistration
import com.dreamyloong.tlauncher.core.page.PageActionStyle
import com.dreamyloong.tlauncher.core.page.PageChoiceOptionRegistration
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageValueItemRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.page.PageWidgetTone

class AccountManagerPage(
    private val accounts: List<LauncherAccount>,
    private val isSupported: Boolean,
    private val isLoading: Boolean,
    private val isMutating: Boolean,
    private val statusMessage: String?,
    private val onAddAccount: () -> Unit,
    private val onRefresh: () -> Unit,
    private val onDeleteAccount: (LauncherAccount) -> Unit,
    private val onBack: () -> Unit,
) : LauncherPage(PageIds.ACCOUNT_MANAGER) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        return PageContributionBundle(
            sourceId = "core.page.account_manager",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.account_manager",
                title = PageTextRef.Direct(strings.accountManagerTitle()),
                subtitle = PageTextRef.Direct(strings.accountManagerSubtitle()),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "account_manager_actions",
                        pageId = pageId,
                        sourceId = "core.page.account_manager",
                        orderHint = 0,
                        title = PageTextRef.Direct(strings.accountManagerSection()),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "account_manager_add",
                        pageId = pageId,
                        parentNodeId = "account_manager_actions",
                        sourceId = "core.page.account_manager",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.accountManagerAddAccount()),
                            subtitle = PageTextRef.Direct(
                                if (isSupported) {
                                    strings.accountManagerAddAccountSubtitle()
                                } else {
                                    strings.accountManagerUnsupported()
                                },
                            ),
                            enabled = isSupported,
                            onClick = if (isSupported) onAddAccount else null,
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "account_manager_refresh",
                        pageId = pageId,
                        parentNodeId = "account_manager_actions",
                        sourceId = "core.page.account_manager",
                        orderHint = 10,
                        widget = PageWidgetRegistrationModel.ButtonStack(
                            actions = listOf(
                                PageActionRegistration(
                                    id = "account_manager_refresh_action",
                                    label = PageTextRef.Direct(
                                        if (isLoading) {
                                            strings.accountManagerRefreshing()
                                        } else {
                                            strings.accountManagerRefresh()
                                        },
                                    ),
                                    enabled = isSupported && !isLoading,
                                    onClick = onRefresh,
                                ),
                            ),
                        ),
                    ),
                )
                statusMessage?.let { message ->
                    add(
                        PageWidgetRegistration(
                            nodeId = "account_manager_status",
                            pageId = pageId,
                            parentNodeId = "account_manager_actions",
                            sourceId = "core.page.account_manager",
                            orderHint = 20,
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(strings.commonStatus),
                                subtitle = PageTextRef.Direct(message),
                            ),
                        ),
                    )
                }
                add(
                    PageSectionRegistration(
                        nodeId = "account_manager_accounts",
                        pageId = pageId,
                        sourceId = "core.page.account_manager",
                        orderHint = 10,
                        title = PageTextRef.Direct(strings.accountManagerAccounts()),
                    ),
                )
                if (accounts.isEmpty()) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "account_manager_empty",
                            pageId = pageId,
                            parentNodeId = "account_manager_accounts",
                            sourceId = "core.page.account_manager",
                            orderHint = 0,
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(strings.accountManagerNoAccountsTitle()),
                                subtitle = PageTextRef.Direct(strings.accountManagerNoAccountsSubtitle()),
                            ),
                        ),
                    )
                } else {
                    accounts.forEachIndexed { index, account ->
                        add(
                            PageWidgetRegistration(
                                nodeId = "account_manager_account_$index",
                                pageId = pageId,
                                parentNodeId = "account_manager_accounts",
                                sourceId = "core.page.account_manager",
                                orderHint = index,
                                widget = PageWidgetRegistrationModel.DetailCard(
                                    title = PageTextRef.Direct(
                                        account.displayName?.ifBlank { null }
                                            ?: strings.accountManagerUnnamedAccount(),
                                    ),
                                    subtitle = PageTextRef.Direct(
                                        strings.accountManagerAccountSubtitle(
                                            providerName = strings.accountProviderName(account.provider),
                                            subjectId = account.subjectId,
                                            active = account.active,
                                        ),
                                    ),
                                    rows = buildList {
                                        add(
                                            PageValueItemRegistration(
                                                label = PageTextRef.Direct(strings.commonStatus),
                                                value = PageTextRef.Direct(
                                                    if (account.active) {
                                                        strings.accountManagerActiveAccount()
                                                    } else {
                                                        strings.accountManagerSavedAccount()
                                                    },
                                                ),
                                            ),
                                        )
                                        add(
                                            PageValueItemRegistration(
                                                label = PageTextRef.Direct(strings.accountManagerTokenState()),
                                                value = PageTextRef.Direct(
                                                    strings.accountManagerTokenStateValue(
                                                        hasRefreshToken = account.hasRefreshToken,
                                                        hasAccessToken = account.hasAccessToken,
                                                    ),
                                                ),
                                            ),
                                        )
                                        if (account.provider == LauncherAccountProvider.STEAM) {
                                            add(
                                                PageValueItemRegistration(
                                                    label = PageTextRef.Direct(strings.accountManagerLoginModes()),
                                                    value = PageTextRef.Direct(
                                                        strings.accountManagerLoginModesValue(account.loginModes),
                                                    ),
                                                ),
                                            )
                                        }
                                    },
                                    actions = listOf(
                                        PageActionRegistration(
                                            id = "delete_account_${account.provider}_${account.subjectId}",
                                            label = PageTextRef.Direct(strings.accountManagerDeleteAccount()),
                                            style = PageActionStyle.TEXT,
                                            enabled = isSupported && !isLoading && !isMutating,
                                            onClick = { onDeleteAccount(account) },
                                        ),
                                    ),
                                ),
                            ),
                        )
                    }
                }
            },
        )
    }
}

class AddAccountPage(
    private val selectedProvider: LauncherAccountProvider,
    private val selectedSteamLoginMode: SteamAccountLoginMode,
    private val accountName: String,
    private val password: String,
    private val steamGuardCode: String,
    private val isSupported: Boolean,
    private val isLoggingIn: Boolean,
    private val loginStatus: LauncherAccountLoginStatus?,
    private val statusMessage: String?,
    private val onSelectProvider: (LauncherAccountProvider) -> Unit,
    private val onSelectSteamLoginMode: (SteamAccountLoginMode) -> Unit,
    private val onAccountNameChange: (String) -> Unit,
    private val onPasswordChange: (String) -> Unit,
    private val onSteamGuardCodeChange: (String) -> Unit,
    private val onLogin: () -> Unit,
    private val onCancelLogin: () -> Unit,
    private val onBack: () -> Unit,
) : LauncherPage(PageIds.ADD_ACCOUNT) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        val requiresSteamGuard = loginStatus == LauncherAccountLoginStatus.STEAM_GUARD_REQUIRED
        val canLogin = isSupported &&
            !isLoggingIn &&
            accountName.isNotBlank() &&
            password.isNotBlank()
        return PageContributionBundle(
            sourceId = "core.page.add_account",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.add_account",
                title = PageTextRef.Direct(strings.addAccountTitle()),
                subtitle = PageTextRef.Direct(strings.addAccountSubtitle()),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "add_account_source",
                        pageId = pageId,
                        sourceId = "core.page.add_account",
                        orderHint = 0,
                        title = PageTextRef.Direct(strings.addAccountSourceSection()),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_provider",
                        pageId = pageId,
                        parentNodeId = "add_account_source",
                        sourceId = "core.page.add_account",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.ChoiceCard(
                            title = PageTextRef.Direct(strings.addAccountType()),
                            subtitle = PageTextRef.Direct(strings.addAccountOnlySteamSupported()),
                            options = listOf(
                                PageChoiceOptionRegistration(
                                    id = "provider.steam",
                                    label = PageTextRef.Direct(strings.accountProviderName(LauncherAccountProvider.STEAM)),
                                    selected = selectedProvider == LauncherAccountProvider.STEAM,
                                    onClick = { onSelectProvider(LauncherAccountProvider.STEAM) },
                                ),
                            ),
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_steam_mode",
                        pageId = pageId,
                        parentNodeId = "add_account_source",
                        sourceId = "core.page.add_account",
                        orderHint = 10,
                        widget = PageWidgetRegistrationModel.ChoiceCard(
                            title = PageTextRef.Direct(strings.addAccountSteamLoginMode()),
                            subtitle = PageTextRef.Direct(strings.addAccountSteamLoginModeSubtitle()),
                            options = SteamAccountLoginMode.entries.map { mode ->
                                PageChoiceOptionRegistration(
                                    id = "steam_mode.${mode.name.lowercase()}",
                                    label = PageTextRef.Direct(strings.steamLoginModeName(mode)),
                                    selected = selectedSteamLoginMode == mode,
                                    onClick = { onSelectSteamLoginMode(mode) },
                                )
                            },
                        ),
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "add_account_credentials",
                        pageId = pageId,
                        sourceId = "core.page.add_account",
                        orderHint = 10,
                        title = PageTextRef.Direct(strings.addAccountCredentialsSection()),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_name",
                        pageId = pageId,
                        parentNodeId = "add_account_credentials",
                        sourceId = "core.page.add_account",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.addAccountSteamAccountName()),
                            value = PageTextRef.Direct(accountName),
                            placeholder = PageTextRef.Direct(strings.addAccountSteamAccountNamePlaceholder()),
                            enabled = isSupported && !isLoggingIn,
                            onValueChange = onAccountNameChange,
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_password",
                        pageId = pageId,
                        parentNodeId = "add_account_credentials",
                        sourceId = "core.page.add_account",
                        orderHint = 10,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.addAccountSteamPassword()),
                            value = PageTextRef.Direct(password),
                            placeholder = PageTextRef.Direct(strings.addAccountSteamPasswordPlaceholder()),
                            enabled = isSupported && !isLoggingIn,
                            password = true,
                            onValueChange = onPasswordChange,
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_steam_guard",
                        pageId = pageId,
                        parentNodeId = "add_account_credentials",
                        sourceId = "core.page.add_account",
                        orderHint = 20,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.addAccountSteamGuardCode()),
                            value = PageTextRef.Direct(steamGuardCode),
                            placeholder = PageTextRef.Direct(strings.addAccountSteamGuardCodePlaceholder()),
                            supportingText = PageTextRef.Direct(
                                if (requiresSteamGuard) {
                                    strings.addAccountSteamGuardCodeRequiredSupport()
                                } else {
                                    strings.addAccountSteamGuardCodeSupport()
                                },
                            ),
                            enabled = isSupported && !isLoggingIn,
                            onValueChange = onSteamGuardCodeChange,
                        ),
                    ),
                )
                val statusCardTitle = when {
                    isLoggingIn -> strings.addAccountPollingConfirmationTitle()
                    requiresSteamGuard -> strings.addAccountSteamGuardRequiredTitle()
                    else -> strings.commonStatus
                }
                val statusCardMessage = when {
                    isLoggingIn -> strings.addAccountPollingConfirmationMessage()
                    statusMessage != null -> statusMessage
                    else -> null
                }
                val statusCardTone = when {
                    isLoggingIn -> PageWidgetTone.ACCENT
                    requiresSteamGuard -> PageWidgetTone.ACCENT
                    else -> PageWidgetTone.DEFAULT
                }
                statusCardMessage?.let { message ->
                    add(
                        PageWidgetRegistration(
                            nodeId = "add_account_status",
                            pageId = pageId,
                            parentNodeId = "add_account_credentials",
                            sourceId = "core.page.add_account",
                            orderHint = 30,
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(statusCardTitle),
                                subtitle = PageTextRef.Direct(message),
                                tone = statusCardTone,
                            ),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "add_account_login",
                        pageId = pageId,
                        parentNodeId = "add_account_credentials",
                        sourceId = "core.page.add_account",
                        orderHint = 40,
                        widget = PageWidgetRegistrationModel.ButtonStack(
                            actions = buildList {
                                add(
                                    PageActionRegistration(
                                        id = "add_account_login_action",
                                        label = PageTextRef.Direct(
                                            if (isLoggingIn) {
                                            strings.addAccountLoggingIn()
                                        } else if (requiresSteamGuard) {
                                            strings.addAccountLoginWithSteamGuard()
                                        } else {
                                            strings.addAccountLogin()
                                        },
                                    ),
                                    style = PageActionStyle.FILLED_TONAL,
                                    enabled = canLogin,
                                    onClick = onLogin,
                                ),
                                )
                                if (isLoggingIn) {
                                    add(
                                        PageActionRegistration(
                                            id = "add_account_cancel_login_action",
                                            label = PageTextRef.Direct(strings.addAccountCancelLogin()),
                                            style = PageActionStyle.TEXT,
                                            enabled = true,
                                            onClick = onCancelLogin,
                                        ),
                                    )
                                }
                            },
                        ),
                    ),
                )
            },
        )
    }
}
