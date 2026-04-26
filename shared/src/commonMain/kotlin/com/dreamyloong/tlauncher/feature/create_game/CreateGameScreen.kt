package com.dreamyloong.tlauncher.feature.create_game

import com.dreamyloong.tlauncher.core.model.Template
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
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistration
import com.dreamyloong.tlauncher.core.page.PageWidgetRegistrationModel

class CreateGamePage(
    private val templates: List<Template>,
    private val selectedTemplate: Template?,
    private val onSelectTemplate: (Template) -> Unit,
    private val canCreate: (Template) -> Boolean,
    private val nameFor: (Template) -> String,
    private val descriptionFor: (Template) -> String,
    private val onNameChange: (Template, String) -> Unit,
    private val onDescriptionChange: (Template, String) -> Unit,
    private val onBack: () -> Unit,
    private val onCreate: (Template, String, String) -> Unit,
) : LauncherPage(PageIds.CREATE_GAME) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        return PageContributionBundle(
            sourceId = "core.page.create_game",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.create_game",
                title = PageTextRef.Direct(strings.createGameTitle),
                subtitle = PageTextRef.Direct(strings.createGameSubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                val selected = selectedTemplate
                val instanceEditingEnabled = selected != null
                val createEnabled = selected?.let(canCreate) == true
                val selectedName = selected?.let(nameFor).orEmpty()
                val selectedDescription = selected?.let(descriptionFor).orEmpty()

                add(
                    PageSectionRegistration(
                        nodeId = "create_game_template_selector",
                        pageId = pageId,
                        sourceId = "core.page.create_game",
                        title = PageTextRef.Direct(strings.createTemplateSelectorTitle()),
                        subtitle = PageTextRef.Direct(strings.createTemplateSelectorSubtitle()),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "create_game_template_choice",
                        pageId = pageId,
                        parentNodeId = "create_game_template_selector",
                        sourceId = "core.page.create_game",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.ChoiceCard(
                            title = PageTextRef.Direct(
                                selected?.name?.resolve(strings.language)
                                    ?: strings.createTemplateSelectorPlaceholder(),
                            ),
                            subtitle = PageTextRef.Direct(
                                selected?.description?.resolve(strings.language)
                                    ?: strings.createTemplateSelectorHint(templates.size),
                            ),
                            options = templates.map { template ->
                                PageChoiceOptionRegistration(
                                    id = template.packageId.value,
                                    label = PageTextRef.Direct(template.name.resolve(strings.language)),
                                    selected = selected?.packageId == template.packageId,
                                    onClick = { onSelectTemplate(template) },
                                )
                            },
                        ),
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "create_game_template_details",
                        pageId = pageId,
                        sourceId = "core.page.create_game",
                        orderHint = 1,
                        title = PageTextRef.Direct(strings.detailTemplateInfo),
                        subtitle = PageTextRef.Direct(
                            selected?.name?.resolve(strings.language)
                                ?: strings.createTemplateSelectionRequired(),
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "create_game_selected_template_details",
                        pageId = pageId,
                        parentNodeId = "create_game_template_details",
                        sourceId = "core.page.create_game",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.commonTemplate),
                            subtitle = PageTextRef.Direct(
                                selected?.let { template ->
                                    buildString {
                                        append(template.description.resolve(strings.language))
                                        append('\n')
                                        append(strings.commonCapabilities)
                                        append(": ")
                                        append(template.capabilityKeys.joinToString { key ->
                                            template.capabilityLabels[key]?.resolve(strings.language) ?: key
                                        })
                                        append('\n')
                                        append(strings.commonSource)
                                        append(": ")
                                        append(strings.sourceName(template.sourceType))
                                        append('\n')
                                        append(strings.commonStatus)
                                        append(": ")
                                        append(strings.statusName(template.releaseState))
                                    }
                                } ?: strings.createTemplateSelectionRequiredDetail(),
                            ),
                        ),
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "create_game_instance_settings",
                        pageId = pageId,
                        sourceId = "core.page.create_game",
                        orderHint = 2,
                        title = PageTextRef.Direct(strings.createGameInstanceSettings),
                        subtitle = PageTextRef.Direct(strings.createGameInstanceSettingsSubtitle),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "create_game_name",
                        pageId = pageId,
                        parentNodeId = "create_game_instance_settings",
                        sourceId = "core.page.create_game",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.commonName),
                            value = PageTextRef.Direct(selectedName),
                            enabled = instanceEditingEnabled,
                            placeholder = PageTextRef.Direct(strings.createGameNamePlaceholder()),
                            supportingText = PageTextRef.Direct(
                                selected?.let { template ->
                                    strings.createGameUsingTemplateName(template.name.resolve(strings.language))
                                } ?: strings.createTemplateSelectionRequiredDetail()
                            ),
                            onValueChange = { value ->
                                selected?.let { template -> onNameChange(template, value) }
                            },
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "create_game_description",
                        pageId = pageId,
                        parentNodeId = "create_game_instance_settings",
                        sourceId = "core.page.create_game",
                        orderHint = 1,
                        widget = PageWidgetRegistrationModel.TextInputCard(
                            title = PageTextRef.Direct(strings.commonDescription),
                            value = PageTextRef.Direct(selectedDescription),
                            enabled = instanceEditingEnabled,
                            placeholder = PageTextRef.Direct(strings.createGameDescriptionPlaceholder()),
                            singleLine = false,
                            onValueChange = { value ->
                                selected?.let { template -> onDescriptionChange(template, value) }
                            },
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "create_game_action",
                        pageId = pageId,
                        parentNodeId = "create_game_instance_settings",
                        sourceId = "core.page.create_game",
                        orderHint = 2,
                        widget = PageWidgetRegistrationModel.ButtonStack(
                            actions = listOf(
                                PageActionRegistration(
                                    id = "create_instance",
                                    label = PageTextRef.Direct(
                                        when {
                                            selected == null -> strings.createGameSelectTemplateFirst()
                                            createEnabled -> strings.createGameCreateInstance
                                            else -> strings.createGameAlreadyCreated
                                        },
                                    ),
                                    style = if (createEnabled) {
                                        PageActionStyle.FILLED_TONAL
                                    } else {
                                        PageActionStyle.OUTLINED
                                    },
                                    enabled = createEnabled,
                                    onClick = {
                                        selected?.let { template ->
                                            onCreate(template, selectedName, selectedDescription)
                                        }
                                    },
                                ),
                            ),
                        ),
                    ),
                )
            },
        )
    }
}
