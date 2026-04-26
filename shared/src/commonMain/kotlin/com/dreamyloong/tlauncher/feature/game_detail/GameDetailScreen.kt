package com.dreamyloong.tlauncher.feature.game_detail

import com.dreamyloong.tlauncher.core.model.GameInstance
import com.dreamyloong.tlauncher.core.model.Template
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
import com.dreamyloong.tlauncher.core.page.PageWidgetTone

class GameDetailPage(
    private val game: GameInstance,
    private val template: Template?,
    private val nameDraft: String,
    private val descriptionDraft: String,
    private val onNameChange: (String) -> Unit,
    private val onDescriptionChange: (String) -> Unit,
    private val onBack: () -> Unit,
    private val onDelete: () -> Unit,
) : LauncherPage(PageIds.GAME_DETAIL) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        return PageContributionBundle(
            sourceId = "core.page.game_detail",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.game_detail",
                title = PageTextRef.Direct(strings.detailTitle),
                subtitle = PageTextRef.Direct(strings.detailSubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "detail_instance",
                        pageId = pageId,
                        sourceId = "core.page.game_detail",
                        orderHint = 0,
                        title = PageTextRef.Direct(strings.detailInstance),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "detail_instance_card",
                        pageId = pageId,
                        parentNodeId = "detail_instance",
                        sourceId = "core.page.game_detail",
                        widget = PageWidgetRegistrationModel.SummaryCard(
                            title = PageTextRef.Direct(game.displayName),
                            subtitle = PageTextRef.Direct(game.description.ifBlank { "${strings.commonTemplate}：${game.templatePackageId.value}" }),
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "detail_instance_name",
                        pageId = pageId,
                        parentNodeId = "detail_instance",
                        sourceId = "core.page.game_detail",
                        orderHint = 5,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.commonName),
                            value = PageTextRef.Direct(nameDraft),
                            onValueChange = onNameChange,
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "detail_instance_description",
                        pageId = pageId,
                        parentNodeId = "detail_instance",
                        sourceId = "core.page.game_detail",
                        orderHint = 6,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.commonDescription),
                            value = PageTextRef.Direct(descriptionDraft),
                            singleLine = false,
                            onValueChange = onDescriptionChange,
                        ),
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "detail_template",
                        pageId = pageId,
                        sourceId = "core.page.game_detail",
                        orderHint = 10,
                        title = PageTextRef.Direct(strings.detailTemplateInfo),
                    ),
                )
                if (template != null) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "detail_template_summary",
                            pageId = pageId,
                            parentNodeId = "detail_template",
                            sourceId = "core.page.game_detail",
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(template.name.resolve(strings.language)),
                                subtitle = PageTextRef.Direct(template.description.resolve(strings.language)),
                            ),
                        ),
                    )
                    add(
                        PageWidgetRegistration(
                            nodeId = "detail_template_values",
                            pageId = pageId,
                            parentNodeId = "detail_template",
                            sourceId = "core.page.game_detail",
                            orderHint = 5,
                            widget = PageWidgetRegistrationModel.ValueCard(
                                rows = listOf(
                                    PageValueItemRegistration(
                                        label = PageTextRef.Direct(strings.commonPlatformLabel),
                                        value = PageTextRef.Direct(
                                            template.supportedTargets.joinToString { target ->
                                                strings.platformName(target)
                                            },
                                        ),
                                    ),
                                    PageValueItemRegistration(
                                        label = PageTextRef.Direct(strings.commonCapabilities),
                                        value = PageTextRef.Direct(
                                            template.capabilityKeys.joinToString { key ->
                                                template.capabilityLabels[key]?.resolve(strings.language) ?: key
                                            },
                                        ),
                                    ),
                                    PageValueItemRegistration(
                                        label = PageTextRef.Direct(strings.commonSource),
                                        value = PageTextRef.Direct(strings.sourceName(template.sourceType)),
                                    ),
                                    PageValueItemRegistration(
                                        label = PageTextRef.Direct(strings.commonStatus),
                                        value = PageTextRef.Direct(strings.statusName(template.releaseState)),
                                    ),
                                    PageValueItemRegistration(
                                        label = PageTextRef.Direct(strings.commonPackageId),
                                        value = PageTextRef.Direct(template.packageId.value),
                                    ),
                                ),
                            ),
                        ),
                    )
                    template.notes?.let { notes ->
                        add(
                            PageSectionRegistration(
                                nodeId = "detail_notes",
                                pageId = pageId,
                                parentNodeId = "detail_template",
                                sourceId = "core.page.game_detail",
                                orderHint = 30,
                                title = PageTextRef.Direct(strings.detailTemplateNotes),
                            ),
                        )
                        add(
                            PageWidgetRegistration(
                                nodeId = "detail_notes_card",
                                pageId = pageId,
                                parentNodeId = "detail_notes",
                                sourceId = "core.page.game_detail",
                                widget = PageWidgetRegistrationModel.SummaryCard(
                                    title = PageTextRef.Direct(template.name.resolve(strings.language)),
                                    subtitle = PageTextRef.Direct(notes.resolve(strings.language)),
                                ),
                            ),
                        )
                    }
                } else {
                    add(
                        PageWidgetRegistration(
                            nodeId = "detail_template_missing",
                            pageId = pageId,
                            parentNodeId = "detail_template",
                            sourceId = "core.page.game_detail",
                            widget = PageWidgetRegistrationModel.SummaryCard(
                                title = PageTextRef.Direct(strings.detailTemplateUnavailableTitle()),
                                subtitle = PageTextRef.Direct(
                                    strings.detailTemplateUnavailableDetail(game.templatePackageId.value),
                                ),
                                tone = PageWidgetTone.DANGER,
                            ),
                        ),
                    )
                }
                add(
                    PageSectionRegistration(
                        nodeId = "detail_operations",
                        pageId = pageId,
                        sourceId = "core.page.game_detail",
                        orderHint = 40,
                        title = PageTextRef.Direct(strings.detailOperations),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "detail_operation_buttons",
                        pageId = pageId,
                        parentNodeId = "detail_operations",
                        sourceId = "core.page.game_detail",
                        widget = PageWidgetRegistrationModel.ButtonStack(
                            actions = listOf(
                                PageActionRegistration(
                                    id = "detail_delete",
                                    label = PageTextRef.Direct(strings.detailDeleteGame),
                                    style = PageActionStyle.FILLED_TONAL,
                                    onClick = onDelete,
                                ),
                            ),
                        ),
                    ),
                )
            },
        )
    }
}
