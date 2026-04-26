package com.dreamyloong.tlauncher.core.page

import com.dreamyloong.tlauncher.core.extension.ExtensionRegistry
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.plugin.PageContributionProviderExtension
import com.dreamyloong.tlauncher.core.plugin.PageTreeMutatorExtension
import com.dreamyloong.tlauncher.data.plugin.RuntimeExtensionRegistry

class PageResolver(
    private val extensionRegistry: ExtensionRegistry,
) {
    fun resolve(
        page: LauncherPage,
        context: PageContext,
    ): ResolvedPage {
        val baseContribution = page.buildBaseContribution(context)
        val extensionContributions = extensionRegistry.registeredFeatures()
            .mapNotNull { registered ->
                (registered.feature as? PageContributionProviderExtension)?.let { feature ->
                    registered to feature
                }
            }
            .flatMap { (registered, extension) ->
                val failureStage = RuntimeExtensionRegistry.STAGE_PAGE_CONTRIBUTIONS_PREFIX + page.pageId
                runCatching { extension.providePageContributions(context) }
                    .onSuccess {
                        extensionRegistry.clearExtensionFailure(registered.owner, failureStage)
                    }
                    .getOrElse { error ->
                        extensionRegistry.reportExtensionFailure(registered.owner, failureStage, error)
                        emptyList()
                    }
            }

        val contributions = buildList {
            add(baseContribution)
            addAll(extensionContributions)
        }

        val localizationBundles = contributions.mapNotNull { it.localizationBundle }
            .associateBy { it.sourceId }

        val pageRegistration = contributions.mapNotNull { it.page }
            .filter { registration ->
                registration.id == page.pageId && context.target in registration.supportedTargets
            }
            .maxWithOrNull(compareBy<PageRegistration>(
                { -priorityFor(it.sourceId, context.extensionPriorityEntries) },
            ))
            ?: error("No page registration found for ${page.pageId}")

        val resolvedPage = ResolvedPage(
            id = pageRegistration.id,
            title = resolveText(pageRegistration.title, pageRegistration.sourceId, localizationBundles, context),
            subtitle = pageRegistration.subtitle?.let { subtitle ->
                resolveText(subtitle, pageRegistration.sourceId, localizationBundles, context)
            },
            actionLabel = pageRegistration.actionLabel?.let { label ->
                resolveText(label, pageRegistration.sourceId, localizationBundles, context)
            },
            action = pageRegistration.action,
            nodes = resolveContentNodes(
                pageId = page.pageId,
                contributions = contributions,
                localizationBundles = localizationBundles,
                context = context,
            ),
            footerNodes = resolveFooterNodes(
                pageId = page.pageId,
                contributions = contributions,
                localizationBundles = localizationBundles,
                context = context,
            ),
        )

        return extensionRegistry.registeredFeatures()
            .sortedBy { registered -> priorityFor(registered.owner.extension.identityId, context.extensionPriorityEntries) }
            .mapNotNull { registered ->
                (registered.feature as? PageTreeMutatorExtension)?.let { feature ->
                    registered to feature
                }
            }
            .fold(resolvedPage) { currentPage, (registered, mutator) ->
                val failureStage = RuntimeExtensionRegistry.STAGE_PAGE_MUTATOR_PREFIX + page.pageId
                runCatching { mutator.mutatePage(context, currentPage) }
                    .onSuccess {
                        extensionRegistry.clearExtensionFailure(registered.owner, failureStage)
                    }
                    .getOrElse { error ->
                        extensionRegistry.reportExtensionFailure(registered.owner, failureStage, error)
                        currentPage
                    }
            }
    }
}

private fun resolveContentNodes(
    pageId: String,
    contributions: List<PageContributionBundle>,
    localizationBundles: Map<String, PageLocalizationBundle>,
    context: PageContext,
): List<ResolvedPageNode> {
    val availableRegistrations = contributions.flatMap { contribution -> contribution.nodes }
        .filter { registration ->
            registration.pageId == pageId &&
                context.target in registration.supportedTargets &&
                registration.placement == PageNodePlacement.CONTENT &&
                registration.displayPolicy.visibleWhen(context)
        }

    fun buildTree(
        parentNodeId: String?,
        sectionPath: List<String>,
        ancestors: Set<String>,
    ): List<ResolvedPageNode> {
        return availableRegistrations
            .filter { registration -> registration.parentNodeId == parentNodeId }
            .groupBy { registration ->
                registrationPathKey(
                    pageId = pageId,
                    sectionPath = sectionPath,
                    nodeId = registration.nodeId,
                )
            }
            .mapValues { (_, registrations) ->
                registrations.maxWithOrNull(compareBy<PageNodeRegistration>(
                    { -priorityFor(it.sourceId, context.extensionPriorityEntries) },
                ))!!
            }
            .values
            .sortedWith(compareBy<PageNodeRegistration>({ it.orderHint }, { it.nodeId }))
            .mapNotNull { registration ->
                if (registration.nodeId in ancestors) {
                    null
                } else {
                    when (registration) {
                        is PageSectionRegistration -> ResolvedPageNode.Section(
                            nodeId = registration.nodeId,
                            orderHint = registration.orderHint,
                            title = registration.title?.let { title ->
                                resolveText(title, registration.sourceId, localizationBundles, context)
                            },
                            subtitle = registration.subtitle?.let { subtitle ->
                                resolveText(subtitle, registration.sourceId, localizationBundles, context)
                            },
                            children = buildTree(
                                parentNodeId = registration.nodeId,
                                sectionPath = sectionPath + registration.nodeId,
                                ancestors = ancestors + registration.nodeId,
                            ),
                        )

                        is PageWidgetRegistration -> ResolvedPageNode.Widget(
                            nodeId = registration.nodeId,
                            orderHint = registration.orderHint,
                            footerLayout = null,
                            widget = registration.widget.toResolved(
                                sourceId = registration.sourceId,
                                localizationBundles = localizationBundles,
                                context = context,
                            ),
                        )
                    }
                }
            }
    }

    return buildTree(
        parentNodeId = null,
        sectionPath = emptyList(),
        ancestors = emptySet(),
    )
}

private fun resolveFooterNodes(
    pageId: String,
    contributions: List<PageContributionBundle>,
    localizationBundles: Map<String, PageLocalizationBundle>,
    context: PageContext,
): List<ResolvedPageNode.Widget> {
    return contributions.flatMap { contribution -> contribution.nodes }
        .filter { registration ->
            registration.pageId == pageId &&
                context.target in registration.supportedTargets &&
                registration.placement == PageNodePlacement.FOOTER &&
                registration.displayPolicy.visibleWhen(context)
        }
        .filterIsInstance<PageWidgetRegistration>()
        .groupBy { registration ->
            registrationPathKey(
                pageId = pageId,
                sectionPath = listOf("footer"),
                nodeId = registration.nodeId,
            )
        }
        .mapValues { (_, registrations) ->
            registrations.maxWithOrNull(compareBy<PageWidgetRegistration>(
                { -priorityFor(it.sourceId, context.extensionPriorityEntries) },
            ))!!
        }
        .values
        .sortedWith(compareBy<PageWidgetRegistration>({ it.orderHint }, { it.nodeId }))
        .map { registration ->
            ResolvedPageNode.Widget(
                nodeId = registration.nodeId,
                orderHint = registration.orderHint,
                footerLayout = registration.footerLayout ?: PageFooterLayoutRegistration(),
                widget = registration.widget.toResolved(
                    sourceId = registration.sourceId,
                    localizationBundles = localizationBundles,
                    context = context,
                ),
            )
        }
}

private fun registrationPathKey(
    pageId: String,
    sectionPath: List<String>,
    nodeId: String,
): String {
    return buildList {
        add(pageId)
        addAll(sectionPath)
        add(nodeId)
    }.joinToString("/")
}

private fun PageWidgetRegistrationModel.toResolved(
    sourceId: String,
    localizationBundles: Map<String, PageLocalizationBundle>,
    context: PageContext,
): ResolvedPageWidget {
    return when (this) {
        is PageWidgetRegistrationModel.SummaryCard -> ResolvedPageWidget.SummaryCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            subtitle = resolveText(subtitle, sourceId, localizationBundles, context),
            tone = tone,
            onClick = onClick,
        )

        is PageWidgetRegistrationModel.ValueCard -> ResolvedPageWidget.ValueCard(
            rows = rows.map { row ->
                ResolvedPageValueItem(
                    label = resolveText(row.label, sourceId, localizationBundles, context),
                    value = resolveText(row.value, sourceId, localizationBundles, context),
                )
            },
        )

        is PageWidgetRegistrationModel.DetailCard -> ResolvedPageWidget.DetailCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            subtitle = subtitle?.let { value ->
                resolveText(value, sourceId, localizationBundles, context)
            },
            rows = rows.map { row ->
                ResolvedPageValueItem(
                    label = resolveText(row.label, sourceId, localizationBundles, context),
                    value = resolveText(row.value, sourceId, localizationBundles, context),
                )
            },
            actions = actions.map { action ->
                ResolvedPageAction(
                    id = action.id,
                    label = resolveText(action.label, sourceId, localizationBundles, context),
                    compactLabel = action.compactLabel?.let { value ->
                        resolveText(value, sourceId, localizationBundles, context)
                    },
                    style = action.style,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
            },
            tone = tone,
            enabled = enabled,
            onClick = onClick,
        )

        is PageWidgetRegistrationModel.ProgressCard -> ResolvedPageWidget.ProgressCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            subtitle = subtitle?.let { value ->
                resolveText(value, sourceId, localizationBundles, context)
            },
            progress = ResolvedPageProgress(
                fraction = progress.fraction?.coerceIn(0f, 1f),
                label = progress.label?.let { value ->
                    resolveText(value, sourceId, localizationBundles, context)
                },
                supportingText = progress.supportingText?.let { value ->
                    resolveText(value, sourceId, localizationBundles, context)
                },
            ),
            tone = tone,
        )

        is PageWidgetRegistrationModel.ChoiceCard -> ResolvedPageWidget.ChoiceCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            subtitle = subtitle?.let { value ->
                resolveText(value, sourceId, localizationBundles, context)
            },
            options = options.map { option ->
                ResolvedPageChoiceOption(
                    id = option.id,
                    label = resolveText(option.label, sourceId, localizationBundles, context),
                    selected = option.selected,
                    enabled = option.enabled,
                    onClick = option.onClick,
                )
            },
            actions = actions.map { action ->
                ResolvedPageAction(
                    id = action.id,
                    label = resolveText(action.label, sourceId, localizationBundles, context),
                    compactLabel = action.compactLabel?.let { value ->
                        resolveText(value, sourceId, localizationBundles, context)
                    },
                    style = action.style,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
            },
        )

        is PageWidgetRegistrationModel.ToggleCard -> ResolvedPageWidget.ToggleCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            subtitle = subtitle?.let { value ->
                resolveText(value, sourceId, localizationBundles, context)
            },
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )

        is PageWidgetRegistrationModel.ButtonStack -> ResolvedPageWidget.ButtonStack(
            actions = actions.map { action ->
                ResolvedPageAction(
                    id = action.id,
                    label = resolveText(action.label, sourceId, localizationBundles, context),
                    compactLabel = action.compactLabel?.let { value ->
                        resolveText(value, sourceId, localizationBundles, context)
                    },
                    style = action.style,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
            },
        )

        is PageWidgetRegistrationModel.LaunchBar -> ResolvedPageWidget.LaunchBar(
            title = title?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            subtitle = subtitle?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            primaryAction = ResolvedPageAction(
                id = primaryAction.id,
                label = resolveText(primaryAction.label, sourceId, localizationBundles, context),
                compactLabel = primaryAction.compactLabel?.let { value ->
                    resolveText(value, sourceId, localizationBundles, context)
                },
                style = primaryAction.style,
                enabled = primaryAction.enabled,
                onClick = primaryAction.onClick,
            ),
            secondaryActions = secondaryActions.map { action ->
                ResolvedPageAction(
                    id = action.id,
                    label = resolveText(action.label, sourceId, localizationBundles, context),
                    compactLabel = action.compactLabel?.let { value ->
                        resolveText(value, sourceId, localizationBundles, context)
                    },
                    style = action.style,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
            },
            tone = tone,
        )

        is PageWidgetRegistrationModel.AutoRefresh -> ResolvedPageWidget.AutoRefresh(
            intervalMillis = intervalMillis,
            onRefresh = onRefresh,
        )

        is PageWidgetRegistrationModel.TextInputCard -> ResolvedPageWidget.TextInputCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            value = resolveText(value, sourceId, localizationBundles, context),
            placeholder = placeholder?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            supportingText = supportingText?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            enabled = enabled,
            singleLine = singleLine,
            password = password,
            onValueChange = onValueChange,
        )

        is PageWidgetRegistrationModel.DirectoryInputCard -> ResolvedPageWidget.DirectoryInputCard(
            title = resolveText(title, sourceId, localizationBundles, context),
            value = resolveText(value, sourceId, localizationBundles, context),
            placeholder = placeholder?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            supportingText = supportingText?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            enabled = enabled,
            pickButtonLabel = pickButtonLabel?.let { ref ->
                resolveText(ref, sourceId, localizationBundles, context)
            },
            onValueChange = onValueChange,
            onPickDirectory = onPickDirectory,
        )
    }
}

private fun resolveText(
    ref: PageTextRef,
    sourceId: String,
    localizationBundles: Map<String, PageLocalizationBundle>,
    context: PageContext,
): String {
    return when (ref) {
        is PageTextRef.Direct -> ref.value
        is PageTextRef.BundleKey -> localizationBundles[sourceId]?.resolve(context.strings.language, ref.key) ?: ref.key
    }
}

private fun priorityFor(
    sourceId: String,
    entries: List<ExtensionPriorityEntry>,
): Int {
    return entries.firstOrNull { entry -> entry.descriptor.extension.identityId == sourceId }?.priority ?: Int.MAX_VALUE
}
