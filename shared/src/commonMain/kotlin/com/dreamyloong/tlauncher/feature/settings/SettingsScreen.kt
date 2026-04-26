package com.dreamyloong.tlauncher.feature.settings

import com.dreamyloong.tlauncher.core.i18n.LanguagePreference
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
import com.dreamyloong.tlauncher.core.i18n.launcherIconName
import com.dreamyloong.tlauncher.core.i18n.settingsLauncherIconFollowTheme
import com.dreamyloong.tlauncher.core.i18n.settingsLauncherIconSaveAndClose
import com.dreamyloong.tlauncher.core.i18n.settingsLauncherIconSubtitle
import com.dreamyloong.tlauncher.core.i18n.settingsLauncherIconTitle
import com.dreamyloong.tlauncher.core.i18n.settingsManageStorageGranted
import com.dreamyloong.tlauncher.core.i18n.settingsManageStoragePermission
import com.dreamyloong.tlauncher.core.i18n.settingsManageStorageRequired
import com.dreamyloong.tlauncher.core.i18n.settingsManageStorageUnsupported
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageActionRegistration
import com.dreamyloong.tlauncher.core.page.PageActionStyle
import com.dreamyloong.tlauncher.core.page.PageChoiceOptionRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.theme.LauncherThemeDefinition
import com.dreamyloong.tlauncher.core.theme.ThemeLauncherIcon
import com.dreamyloong.tlauncher.core.theme.ThemePreference

class SettingsPage(
    private val themePreference: ThemePreference,
    private val resolvedThemeName: String,
    private val allThemes: List<Pair<String, LauncherThemeDefinition>>,
    private val onSelectFollowSystemTheme: () -> Unit,
    private val onSelectTheme: (LauncherThemeDefinition) -> Unit,
    private val resolvedLauncherIcon: ThemeLauncherIcon,
    private val launcherIconFollowsTheme: Boolean,
    private val selectedLauncherIcon: ThemeLauncherIcon,
    private val onSelectFollowThemeLauncherIcon: (() -> Unit)?,
    private val onSelectLauncherIcon: ((ThemeLauncherIcon) -> Unit)?,
    private val onSaveLauncherIconAndClose: (() -> Unit)?,
    private val languagePreference: LanguagePreference,
    private val resolvedLanguage: SupportedLanguage,
    private val onSelectFollowSystemLanguage: () -> Unit,
    private val onSelectLanguage: (SupportedLanguage) -> Unit,
    private val accountCount: Int,
    private val onOpenAccountManager: () -> Unit,
    private val extensionCount: Int,
    private val target: PlatformTarget,
    private val onOpenExtensionManager: () -> Unit,
    private val onOpenAbout: () -> Unit,
    private val onManageStorageAccess: () -> Unit,
    private val manageStorageSupported: Boolean,
    private val manageStorageGranted: Boolean,
    private val onBack: () -> Unit,
) : LauncherPage(PageIds.SETTINGS) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        val permissionSubtitle = when {
            !manageStorageSupported -> strings.settingsManageStorageUnsupported
            manageStorageGranted -> strings.settingsManageStorageGranted
            else -> strings.settingsManageStorageRequired
        }
        return PageContributionBundle(
            sourceId = "core.page.settings",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.settings",
                title = PageTextRef.Direct(strings.settingsTitle),
                subtitle = PageTextRef.Direct(strings.settingsSubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "settings_general",
                        pageId = pageId,
                        sourceId = "core.page.settings",
                        orderHint = 0,
                        title = PageTextRef.Direct(strings.settingsGeneral),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "settings_language",
                        pageId = pageId,
                        parentNodeId = "settings_general",
                        sourceId = "core.page.settings",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.ChoiceCard(
                            title = PageTextRef.Direct(strings.settingsLanguage),
                            subtitle = PageTextRef.Direct(strings.languageName(resolvedLanguage)),
                            options = buildList {
                                add(
                                    PageChoiceOptionRegistration(
                                        id = "language.follow_system",
                                        label = PageTextRef.Direct(strings.settingsFollowSystem),
                                        selected = languagePreference == LanguagePreference.FollowSystem,
                                        onClick = onSelectFollowSystemLanguage,
                                    ),
                                )
                                SupportedLanguage.entries.forEach { language ->
                                    add(
                                        PageChoiceOptionRegistration(
                                            id = "language.${language.name}",
                                            label = PageTextRef.Direct(strings.languageName(language)),
                                            selected = languagePreference == LanguagePreference.Fixed(language),
                                            onClick = { onSelectLanguage(language) },
                                        ),
                                    )
                                }
                            },
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "settings_theme",
                        pageId = pageId,
                        parentNodeId = "settings_general",
                        sourceId = "core.page.settings",
                        orderHint = 10,
                        widget = PageWidgetRegistrationModel.ChoiceCard(
                            title = PageTextRef.Direct(strings.homeTheme),
                            subtitle = PageTextRef.Direct(resolvedThemeName),
                            options = buildList {
                                add(
                                    PageChoiceOptionRegistration(
                                        id = "theme.follow_system",
                                        label = PageTextRef.Direct(strings.settingsFollowSystem),
                                        selected = themePreference == ThemePreference.FollowSystem,
                                        onClick = onSelectFollowSystemTheme,
                                    ),
                                )
                                allThemes.forEach { (name, theme) ->
                                    add(
                                        PageChoiceOptionRegistration(
                                            id = "theme.${theme.id.value}",
                                            label = PageTextRef.Direct(name),
                                            selected = themePreference == ThemePreference.Fixed(theme.id),
                                            onClick = { onSelectTheme(theme) },
                                        ),
                                    )
                                }
                            },
                        ),
                    ),
                )
                if (
                    target == PlatformTarget.ANDROID &&
                    onSelectFollowThemeLauncherIcon != null &&
                    onSelectLauncherIcon != null &&
                    onSaveLauncherIconAndClose != null
                ) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "settings_launcher_icon",
                            pageId = pageId,
                            parentNodeId = "settings_general",
                            sourceId = "core.page.settings",
                            orderHint = 15,
                            widget = PageWidgetRegistrationModel.ChoiceCard(
                                title = PageTextRef.Direct(strings.settingsLauncherIconTitle()),
                                subtitle = PageTextRef.Direct(
                                    strings.settingsLauncherIconSubtitle(
                                        currentIconName = strings.launcherIconName(selectedLauncherIcon),
                                        themeIconName = strings.launcherIconName(resolvedLauncherIcon),
                                    ),
                                ),
                                options = listOf(
                                    PageChoiceOptionRegistration(
                                        id = "launcher_icon.follow_theme",
                                        label = PageTextRef.Direct(strings.settingsLauncherIconFollowTheme()),
                                        selected = launcherIconFollowsTheme,
                                        onClick = onSelectFollowThemeLauncherIcon,
                                    ),
                                    PageChoiceOptionRegistration(
                                        id = "launcher_icon.default",
                                        label = PageTextRef.Direct(strings.launcherIconName(ThemeLauncherIcon.DEFAULT)),
                                        selected = !launcherIconFollowsTheme &&
                                            selectedLauncherIcon == ThemeLauncherIcon.DEFAULT,
                                        onClick = { onSelectLauncherIcon(ThemeLauncherIcon.DEFAULT) },
                                    ),
                                    PageChoiceOptionRegistration(
                                        id = "launcher_icon.night",
                                        label = PageTextRef.Direct(strings.launcherIconName(ThemeLauncherIcon.NIGHT)),
                                        selected = !launcherIconFollowsTheme &&
                                            selectedLauncherIcon == ThemeLauncherIcon.NIGHT,
                                        onClick = { onSelectLauncherIcon(ThemeLauncherIcon.NIGHT) },
                                    ),
                                ),
                                actions = listOf(
                                    PageActionRegistration(
                                        id = "settings_launcher_icon_save",
                                        label = PageTextRef.Direct(strings.settingsLauncherIconSaveAndClose()),
                                        style = PageActionStyle.FILLED_TONAL,
                                        onClick = onSaveLauncherIconAndClose,
                                    ),
                                ),
                            ),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "settings_account_manager",
                        pageId = pageId,
                        parentNodeId = "settings_general",
                        sourceId = "core.page.settings",
                        orderHint = 18,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.settingsAccountManagerTitle()),
                            subtitle = PageTextRef.Direct(strings.settingsAccountManagerSubtitle(accountCount)),
                            onClick = onOpenAccountManager,
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "settings_extension_manager",
                        pageId = pageId,
                        parentNodeId = "settings_general",
                        sourceId = "core.page.settings",
                        orderHint = 20,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.settingsLoadOrder),
                            subtitle = PageTextRef.Direct(
                                strings.extensionManagerSummary(extensionCount),
                            ),
                            onClick = onOpenExtensionManager,
                        ),
                    ),
                )
                if (target == PlatformTarget.ANDROID) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "settings_manage_storage",
                            pageId = pageId,
                            parentNodeId = "settings_general",
                            sourceId = "core.page.settings",
                            orderHint = 30,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(strings.settingsManageStoragePermission),
                                subtitle = PageTextRef.Direct(permissionSubtitle),
                                enabled = manageStorageSupported,
                                onClick = if (manageStorageSupported) onManageStorageAccess else null,
                            ),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "settings_about",
                        pageId = pageId,
                        parentNodeId = "settings_general",
                        sourceId = "core.page.settings",
                        orderHint = 60,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.settingsAbout),
                            onClick = onOpenAbout,
                        ),
                    ),
                )
            },
        )
    }
}
