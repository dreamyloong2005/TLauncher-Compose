package com.dreamyloong.tlauncher.feature.settings

import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageActionRegistration
import com.dreamyloong.tlauncher.core.page.PageActionStyle
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageValueItemRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.platform.launcherAppVersionName
import com.dreamyloong.tlauncher.core.update.AppUpdateCheckState
import com.dreamyloong.tlauncher.core.update.AppUpdateCheckStatus

class AboutPage(
    private val onBack: () -> Unit,
    private val updateCheckState: AppUpdateCheckState,
    private val onCheckUpdates: () -> Unit,
    private val onOpenReleasePage: () -> Unit,
    private val onOpenAuthorProfile: () -> Unit,
) : LauncherPage(PageIds.ABOUT) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        val currentVersion = launcherAppVersionName()
        val updateSubtitle = when (updateCheckState.status) {
            AppUpdateCheckStatus.Idle -> strings.aboutCheckUpdatesSubtitle
            AppUpdateCheckStatus.Checking -> strings.aboutCheckUpdatesCheckingSubtitle()
            AppUpdateCheckStatus.UpToDate -> strings.aboutCheckUpdatesLatestSubtitle(
                updateCheckState.release?.tagName ?: currentVersion,
            )
            AppUpdateCheckStatus.InternalBuild -> strings.aboutCheckUpdatesInternalBuildSubtitle(
                currentVersion = currentVersion,
                latestVersion = updateCheckState.release?.tagName ?: "",
            )
            AppUpdateCheckStatus.UpdateAvailable -> strings.aboutCheckUpdatesAvailableSubtitle(
                updateCheckState.release?.tagName ?: "",
            )
            AppUpdateCheckStatus.Failed -> strings.aboutCheckUpdatesFailedSubtitle()
        }
        val updateTone = when (updateCheckState.status) {
            AppUpdateCheckStatus.UpdateAvailable -> com.dreamyloong.tlauncher.core.page.PageWidgetTone.ACCENT
            AppUpdateCheckStatus.Failed -> com.dreamyloong.tlauncher.core.page.PageWidgetTone.DANGER
            else -> com.dreamyloong.tlauncher.core.page.PageWidgetTone.DEFAULT
        }
        val updateActions = if (updateCheckState.status == AppUpdateCheckStatus.Checking) {
            emptyList()
        } else {
            listOf(
                PageActionRegistration(
                    id = "about_updates_open_release",
                    label = PageTextRef.Direct(strings.aboutOpenReleasePageAction()),
                    style = if (updateCheckState.status == AppUpdateCheckStatus.UpdateAvailable) {
                        PageActionStyle.FILLED_TONAL
                    } else {
                        PageActionStyle.OUTLINED
                    },
                    onClick = onOpenReleasePage,
                ),
            )
        }
        return PageContributionBundle(
            sourceId = "core.page.about",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.about",
                title = PageTextRef.Direct(strings.aboutTitle),
                subtitle = PageTextRef.Direct(strings.aboutSubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = listOf(
                PageSectionRegistration(
                    nodeId = "about_overview",
                    pageId = pageId,
                    sourceId = "core.page.about",
                    orderHint = 0,
                ),
                PageWidgetRegistration(
                    nodeId = "about_intro",
                    pageId = pageId,
                    parentNodeId = "about_overview",
                    sourceId = "core.page.about",
                    orderHint = 0,
                    widget = PageWidgetRegistrationModel.SummaryCard(
                        title = PageTextRef.Direct(strings.appTitle),
                        subtitle = PageTextRef.Direct(strings.aboutIntroBody),
                    ),
                ),
                PageWidgetRegistration(
                    nodeId = "about_version",
                    pageId = pageId,
                    parentNodeId = "about_overview",
                    sourceId = "core.page.about",
                    orderHint = 10,
                    widget = PageWidgetRegistrationModel.ValueCard(
                        rows = listOf(
                            PageValueItemRegistration(
                                label = PageTextRef.Direct(strings.commonVersion),
                                value = PageTextRef.Direct(currentVersion),
                            ),
                        ),
                    ),
                ),
                PageWidgetRegistration(
                    nodeId = "about_author",
                    pageId = pageId,
                    parentNodeId = "about_overview",
                    sourceId = "core.page.about",
                    orderHint = 20,
                    widget = PageWidgetRegistrationModel.DetailCard(
                        title = PageTextRef.Direct(strings.commonAuthor),
                        subtitle = PageTextRef.Direct("Dreamyloong\n${strings.aboutAuthorSubtitle}"),
                        onClick = onOpenAuthorProfile,
                    ),
                ),
                PageWidgetRegistration(
                    nodeId = "about_updates",
                    pageId = pageId,
                    parentNodeId = "about_overview",
                    sourceId = "core.page.about",
                    orderHint = 30,
                    widget = PageWidgetRegistrationModel.DetailCard(
                        title = PageTextRef.Direct(strings.aboutCheckUpdates),
                        subtitle = PageTextRef.Direct(updateSubtitle),
                        actions = updateActions,
                        tone = updateTone,
                        enabled = updateCheckState.status != AppUpdateCheckStatus.Checking,
                        onClick = onCheckUpdates,
                    ),
                ),
            ),
        )
    }
}
