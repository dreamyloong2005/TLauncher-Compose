package com.dreamyloong.tlauncher.feature.home

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.PlatformTarget
import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.page.PageContext

class HomePage(
    private val target: PlatformTarget,
    private val currentGame: GameInstance?,
    private val onOpenLibrary: () -> Unit,
    private val onOpenSettings: () -> Unit,
) : LauncherPage(PageIds.HOME) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        return PageContributionBundle(
            sourceId = "core.page.home",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.home",
                title = PageTextRef.Direct(strings.appTitle),
                subtitle = PageTextRef.Direct(strings.homePlatformPrefix + strings.platformName(target)),
                actionLabel = PageTextRef.Direct(strings.homeSettings),
                action = onOpenSettings,
            ),
            nodes = listOf(
                PageSectionRegistration(
                    nodeId = "home_current_game",
                    pageId = pageId,
                    sourceId = "core.page.home",
                    orderHint = 0,
                    title = PageTextRef.Direct(strings.homeCurrentGame),
                ),
                PageSectionRegistration(
                    nodeId = "home_alerts",
                    pageId = pageId,
                    sourceId = "core.page.home",
                    orderHint = 10,
                ),
                PageWidgetRegistration(
                    nodeId = "home_current_game_card",
                    pageId = pageId,
                    parentNodeId = "home_current_game",
                    sourceId = "core.page.home",
                    widget = PageWidgetRegistrationModel.SummaryCard(
                        title = PageTextRef.Direct(currentGame?.displayName ?: strings.homeNoGameTitle),
                        subtitle = PageTextRef.Direct(
                            if (currentGame == null) {
                                strings.homeNoGameSubtitle
                            } else {
                                currentGame.description.ifBlank { strings.homeSelectedGameSubtitle }
                            }
                        ),
                        onClick = onOpenLibrary,
                    ),
                ),
            ),
        )
    }
}
