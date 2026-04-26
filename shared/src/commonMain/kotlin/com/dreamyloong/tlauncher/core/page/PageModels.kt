package com.dreamyloong.tlauncher.core.page

import com.dreamyloong.tlauncher.core.action.LauncherAction
import com.dreamyloong.tlauncher.core.action.LauncherActionDispatcher
import com.dreamyloong.tlauncher.core.action.NoopLauncherActionDispatcher
import com.dreamyloong.tlauncher.core.account.LauncherAccount
import com.dreamyloong.tlauncher.core.i18n.AppStrings
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.platform.DirectoryPickerState
import com.dreamyloong.tlauncher.core.platform.FilePickerState
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import com.dreamyloong.tlauncher.core.platform.GameLaunchState
import com.dreamyloong.tlauncher.core.platform.ManageStorageAccessState
import com.dreamyloong.tlauncher.core.template.TemplateLaunchPreparationContext

data class PageContext(
    val target: PlatformTarget,
    val currentGame: GameInstance?,
    val currentTemplate: Template?,
    val allGames: List<GameInstance>,
    val visibleTemplates: List<Template>,
    val extensionPriorityEntries: List<ExtensionPriorityEntry>,
    val accounts: List<LauncherAccount> = emptyList(),
    val strings: AppStrings,
    val manageStorageAccessState: ManageStorageAccessState? = null,
    val directoryPickerState: DirectoryPickerState? = null,
    val filePickerState: FilePickerState? = null,
    val gameLaunchState: GameLaunchState? = null,
    val interceptPreparedLaunchRequest: (GameLaunchRequest?, TemplateLaunchPreparationContext) -> GameLaunchRequest? = { request, _ -> request },
    val actionDispatcher: LauncherActionDispatcher = NoopLauncherActionDispatcher,
    val navigateToPage: (String) -> Unit,
    val navigateBack: () -> Unit,
    val requestUiRefresh: () -> Unit,
) {
    fun dispatchAction(action: LauncherAction) {
        actionDispatcher.dispatch(action)
    }

    fun openPage(pageId: String) {
        dispatchAction(LauncherAction.OpenPage(pageId))
    }

    fun replaceCurrentPage(pageId: String) {
        dispatchAction(LauncherAction.ReplaceCurrentPage(pageId))
    }

    fun goBack() {
        dispatchAction(LauncherAction.NavigateBack)
    }

    fun refreshPage() {
        dispatchAction(LauncherAction.Refresh)
    }

    @Suppress("DEPRECATION")
    fun refreshUi() {
        requestUiRefresh()
    }

    fun launchGame(request: GameLaunchRequest) {
        dispatchAction(LauncherAction.LaunchGame(request))
    }

    fun openExternalUrl(url: String) {
        dispatchAction(LauncherAction.OpenExternalUrl(url))
    }

    fun applyPreparedLaunchRequestInterceptors(
        request: GameLaunchRequest?,
        templatePackageId: ExtensionIdentityId,
        selectedGameDirectory: String? = null,
    ): GameLaunchRequest? {
        return interceptPreparedLaunchRequest(
            request,
            TemplateLaunchPreparationContext(
                templatePackageId = templatePackageId,
                target = target,
                currentGame = currentGame,
                selectedGameDirectory = selectedGameDirectory,
            ),
        )
    }
}

sealed interface PageTextRef {
    data class Direct(val value: String) : PageTextRef
    data class BundleKey(val key: String) : PageTextRef
}

data class PageLocalizationBundle(
    val sourceId: String,
    val filePaths: Map<SupportedLanguage, String> = emptyMap(),
    val entries: Map<SupportedLanguage, Map<String, String>>,
) {
    fun resolve(
        language: SupportedLanguage,
        key: String,
    ): String {
        return entries[language]?.get(key)
            ?: entries[SupportedLanguage.EN_US]?.get(key)
            ?: key
    }
}

data class PageRegistration(
    val id: String,
    val sourceId: String,
    val title: PageTextRef,
    val subtitle: PageTextRef? = null,
    val actionLabel: PageTextRef? = null,
    val action: (() -> Unit)? = null,
    val supportedTargets: Set<PlatformTarget> = PlatformTarget.entries.toSet(),
)

data class PageFooterLayoutRegistration(
    val horizontalPaddingDp: Int = 18,
    val topPaddingDp: Int = 4,
    val bottomPaddingDp: Int = 8,
)

data class PageDisplayPolicy(
    val visibleWhen: (PageContext) -> Boolean = { true },
)

enum class PageNodePlacement {
    CONTENT,
    FOOTER,
}

sealed interface PageNodeRegistration {
    val nodeId: String
    val pageId: String
    val parentNodeId: String?
    val sourceId: String
    val orderHint: Int
    val supportedTargets: Set<PlatformTarget>
    val displayPolicy: PageDisplayPolicy
    val placement: PageNodePlacement
}

data class PageSectionRegistration(
    override val nodeId: String,
    override val pageId: String,
    override val parentNodeId: String? = null,
    override val sourceId: String,
    override val orderHint: Int = 0,
    override val supportedTargets: Set<PlatformTarget> = PlatformTarget.entries.toSet(),
    override val displayPolicy: PageDisplayPolicy = PageDisplayPolicy(),
    override val placement: PageNodePlacement = PageNodePlacement.CONTENT,
    val title: PageTextRef? = null,
    val subtitle: PageTextRef? = null,
) : PageNodeRegistration

data class PageWidgetRegistration(
    override val nodeId: String,
    override val pageId: String,
    override val parentNodeId: String? = null,
    override val sourceId: String,
    override val orderHint: Int = 0,
    override val supportedTargets: Set<PlatformTarget> = PlatformTarget.entries.toSet(),
    override val displayPolicy: PageDisplayPolicy = PageDisplayPolicy(),
    override val placement: PageNodePlacement = PageNodePlacement.CONTENT,
    val footerLayout: PageFooterLayoutRegistration? = null,
    val widget: PageWidgetRegistrationModel,
) : PageNodeRegistration

enum class PageWidgetTone {
    DEFAULT,
    ACCENT,
    DANGER,
}

enum class PageActionStyle {
    FILLED_TONAL,
    OUTLINED,
    TEXT,
}

data class PageValueItemRegistration(
    val label: PageTextRef,
    val value: PageTextRef,
)

data class PageChoiceOptionRegistration(
    val id: String,
    val label: PageTextRef,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

data class PageActionRegistration(
    val id: String,
    val label: PageTextRef,
    val compactLabel: PageTextRef? = null,
    val style: PageActionStyle = PageActionStyle.OUTLINED,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

data class PageProgressRegistration(
    val fraction: Float? = null,
    val label: PageTextRef? = null,
    val supportingText: PageTextRef? = null,
)

sealed interface PageWidgetRegistrationModel {
    data class SummaryCard(
        val title: PageTextRef,
        val subtitle: PageTextRef,
        val tone: PageWidgetTone = PageWidgetTone.DEFAULT,
        val onClick: (() -> Unit)? = null,
    ) : PageWidgetRegistrationModel

    data class ValueCard(
        val rows: List<PageValueItemRegistration>,
    ) : PageWidgetRegistrationModel

    data class DetailCard(
        val title: PageTextRef,
        val subtitle: PageTextRef? = null,
        val rows: List<PageValueItemRegistration> = emptyList(),
        val actions: List<PageActionRegistration> = emptyList(),
        val tone: PageWidgetTone = PageWidgetTone.DEFAULT,
        val enabled: Boolean = true,
        val onClick: (() -> Unit)? = null,
    ) : PageWidgetRegistrationModel

    data class ProgressCard(
        val title: PageTextRef,
        val subtitle: PageTextRef? = null,
        val progress: PageProgressRegistration = PageProgressRegistration(),
        val tone: PageWidgetTone = PageWidgetTone.DEFAULT,
    ) : PageWidgetRegistrationModel

    data class ChoiceCard(
        val title: PageTextRef,
        val subtitle: PageTextRef? = null,
        val options: List<PageChoiceOptionRegistration>,
        val actions: List<PageActionRegistration> = emptyList(),
    ) : PageWidgetRegistrationModel

    data class ToggleCard(
        val title: PageTextRef,
        val subtitle: PageTextRef? = null,
        val checked: Boolean,
        val enabled: Boolean = true,
        val onCheckedChange: (Boolean) -> Unit,
    ) : PageWidgetRegistrationModel

    data class ButtonStack(
        val actions: List<PageActionRegistration>,
    ) : PageWidgetRegistrationModel

    data class LaunchBar(
        val title: PageTextRef? = null,
        val subtitle: PageTextRef? = null,
        val primaryAction: PageActionRegistration,
        val secondaryActions: List<PageActionRegistration> = emptyList(),
        val tone: PageWidgetTone = PageWidgetTone.ACCENT,
    ) : PageWidgetRegistrationModel

    data class AutoRefresh(
        val intervalMillis: Long,
        val onRefresh: () -> Unit,
    ) : PageWidgetRegistrationModel

    data class TextInputCard(
        val title: PageTextRef,
        val value: PageTextRef,
        val placeholder: PageTextRef? = null,
        val supportingText: PageTextRef? = null,
        val enabled: Boolean = true,
        val singleLine: Boolean = true,
        val password: Boolean = false,
        val onValueChange: (String) -> Unit,
    ) : PageWidgetRegistrationModel

    data class DirectoryInputCard(
        val title: PageTextRef,
        val value: PageTextRef,
        val placeholder: PageTextRef? = null,
        val supportingText: PageTextRef? = null,
        val enabled: Boolean = true,
        val pickButtonLabel: PageTextRef? = null,
        val onValueChange: (String) -> Unit,
        val onPickDirectory: ((currentValue: String?, onPicked: (String?) -> Unit) -> Unit)? = null,
    ) : PageWidgetRegistrationModel
}

data class PageContributionBundle(
    val sourceId: String,
    val page: PageRegistration? = null,
    val nodes: List<PageNodeRegistration>,
    val localizationBundle: PageLocalizationBundle? = null,
)

data class ResolvedPage(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
    val nodes: List<ResolvedPageNode>,
    val footerNodes: List<ResolvedPageNode.Widget> = emptyList(),
)

sealed interface ResolvedPageNode {
    val nodeId: String
    val orderHint: Int

    data class Section(
        override val nodeId: String,
        override val orderHint: Int,
        val title: String? = null,
        val subtitle: String? = null,
        val children: List<ResolvedPageNode>,
    ) : ResolvedPageNode

    data class Widget(
        override val nodeId: String,
        override val orderHint: Int,
        val footerLayout: PageFooterLayoutRegistration? = null,
        val widget: ResolvedPageWidget,
    ) : ResolvedPageNode
}

data class ResolvedPageValueItem(
    val label: String,
    val value: String,
)

data class ResolvedPageChoiceOption(
    val id: String,
    val label: String,
    val selected: Boolean,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

data class ResolvedPageAction(
    val id: String,
    val label: String,
    val compactLabel: String?,
    val style: PageActionStyle,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

data class ResolvedPageProgress(
    val fraction: Float? = null,
    val label: String? = null,
    val supportingText: String? = null,
)

sealed interface ResolvedPageWidget {
    data class SummaryCard(
        val title: String,
        val subtitle: String,
        val tone: PageWidgetTone,
        val onClick: (() -> Unit)?,
    ) : ResolvedPageWidget

    data class ValueCard(
        val rows: List<ResolvedPageValueItem>,
    ) : ResolvedPageWidget

    data class DetailCard(
        val title: String,
        val subtitle: String?,
        val rows: List<ResolvedPageValueItem>,
        val actions: List<ResolvedPageAction>,
        val tone: PageWidgetTone,
        val enabled: Boolean,
        val onClick: (() -> Unit)?,
    ) : ResolvedPageWidget

    data class ProgressCard(
        val title: String,
        val subtitle: String?,
        val progress: ResolvedPageProgress,
        val tone: PageWidgetTone,
    ) : ResolvedPageWidget

    data class ChoiceCard(
        val title: String,
        val subtitle: String?,
        val options: List<ResolvedPageChoiceOption>,
        val actions: List<ResolvedPageAction>,
    ) : ResolvedPageWidget

    data class ToggleCard(
        val title: String,
        val subtitle: String?,
        val checked: Boolean,
        val enabled: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : ResolvedPageWidget

    data class ButtonStack(
        val actions: List<ResolvedPageAction>,
    ) : ResolvedPageWidget

    data class LaunchBar(
        val title: String?,
        val subtitle: String?,
        val primaryAction: ResolvedPageAction,
        val secondaryActions: List<ResolvedPageAction>,
        val tone: PageWidgetTone,
    ) : ResolvedPageWidget

    data class AutoRefresh(
        val intervalMillis: Long,
        val onRefresh: () -> Unit,
    ) : ResolvedPageWidget

    data class TextInputCard(
        val title: String,
        val value: String,
        val placeholder: String?,
        val supportingText: String?,
        val enabled: Boolean,
        val singleLine: Boolean,
        val password: Boolean,
        val onValueChange: (String) -> Unit,
    ) : ResolvedPageWidget

    data class DirectoryInputCard(
        val title: String,
        val value: String,
        val placeholder: String?,
        val supportingText: String?,
        val enabled: Boolean,
        val pickButtonLabel: String?,
        val onValueChange: (String) -> Unit,
        val onPickDirectory: ((currentValue: String?, onPicked: (String?) -> Unit) -> Unit)?,
    ) : ResolvedPageWidget
}
