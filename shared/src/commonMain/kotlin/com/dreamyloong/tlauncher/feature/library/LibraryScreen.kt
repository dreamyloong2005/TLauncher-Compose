package com.dreamyloong.tlauncher.feature.library

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.page.LauncherPage
import com.dreamyloong.tlauncher.core.page.PageActionRegistration
import com.dreamyloong.tlauncher.core.page.PageActionStyle
import com.dreamyloong.tlauncher.core.page.PageContributionBundle
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageIds
import com.dreamyloong.tlauncher.core.page.PageRegistration
import com.dreamyloong.tlauncher.core.page.PageSectionRegistration
import com.dreamyloong.tlauncher.core.page.PageTextRef
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel
import com.dreamyloong.tlauncher.core.page.PageWidgetTone

class LibraryPage(
    private val games: List<GameInstance>,
    private val currentGameId: String?,
    private val onBack: () -> Unit,
    private val onCreateGame: () -> Unit,
    private val onSelectGame: (GameInstance) -> Unit,
    private val onOpenDetails: (GameInstance) -> Unit,
) : LauncherPage(PageIds.LIBRARY) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        return PageContributionBundle(
            sourceId = "core.page.library",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.library",
                title = PageTextRef.Direct(strings.libraryTitle),
                subtitle = PageTextRef.Direct(strings.librarySubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "library_instances",
                        pageId = pageId,
                        sourceId = "core.page.library",
                        orderHint = 0,
                        title = PageTextRef.Direct(strings.libraryInstanceList),
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "library_actions",
                        pageId = pageId,
                        sourceId = "core.page.library",
                        orderHint = 10,
                    ),
                )
                if (games.isEmpty()) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "library_empty_state",
                            pageId = pageId,
                            parentNodeId = "library_instances",
                            sourceId = "core.page.library",
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(strings.libraryNoGamesTitle),
                                subtitle = PageTextRef.Direct(strings.libraryNoGamesSubtitle),
                            ),
                        ),
                    )
                } else {
                    games.forEachIndexed { index, game ->
                        val selected = currentGameId == game.id.value
                        add(
                            PageWidgetRegistration(
                                nodeId = "library_game_${game.id.value}",
                                pageId = pageId,
                                parentNodeId = "library_instances",
                                sourceId = "core.page.library",
                                orderHint = index,
                                widget = PageWidgetRegistrationModel.DetailCard(
                                    title = PageTextRef.Direct(game.displayName),
                                    subtitle = PageTextRef.Direct(game.description.ifBlank { "${strings.commonTemplate}：${game.templatePackageId.value}" }),
                                    tone = if (selected) PageWidgetTone.ACCENT else PageWidgetTone.DEFAULT,
                                    onClick = { onSelectGame(game) },
                                    actions = listOf(
                                        PageActionRegistration(
                                            id = "details_${game.id.value}",
                                            label = PageTextRef.Direct(strings.libraryDetails),
                                            compactLabel = PageTextRef.Direct("i"),
                                            style = PageActionStyle.OUTLINED,
                                            onClick = { onOpenDetails(game) },
                                        ),
                                    ),
                                ),
                            ),
                        )
                    }
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "library_create_action",
                        pageId = pageId,
                        parentNodeId = "library_actions",
                        sourceId = "core.page.library",
                        widget = PageWidgetRegistrationModel.ButtonStack(
                            actions = listOf(
                                PageActionRegistration(
                                    id = "library_create",
                                    label = PageTextRef.Direct(strings.libraryNewGame),
                                    style = PageActionStyle.OUTLINED,
                                    onClick = onCreateGame,
                                ),
                            ),
                        ),
                    ),
                )
            },
        )
    }
}
