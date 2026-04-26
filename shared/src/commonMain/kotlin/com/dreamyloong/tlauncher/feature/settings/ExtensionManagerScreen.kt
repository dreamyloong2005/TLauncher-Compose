package com.dreamyloong.tlauncher.feature.settings

import com.dreamyloong.tlauncher.core.extension.HostPermissionKey
import com.dreamyloong.tlauncher.core.extension.HostGrantState
import com.dreamyloong.tlauncher.core.i18n.AppStrings
import com.dreamyloong.tlauncher.core.i18n.SupportedLanguage
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
import com.dreamyloong.tlauncher.core.model.ExtensionPackageScanProblem
import com.dreamyloong.tlauncher.core.model.ExtensionPermissionReview
import com.dreamyloong.tlauncher.core.model.ExtensionPriorityEntry
import com.dreamyloong.tlauncher.core.model.PlatformTarget

class ExtensionManagerPage(
    private val entries: List<ExtensionPriorityEntry>,
    private val permissionReviews: List<ExtensionPermissionReview>,
    private val scanProblems: List<ExtensionPackageScanProblem>,
    private val currentTarget: PlatformTarget,
    private val installedPackageDirectory: String?,
    private val onLoadExtension: (() -> Unit)?,
    private val onDeleteExtensionPackage: ((String) -> Unit)?,
    private val loadStatusMessage: String?,
    private val onTogglePermissionGrant: (ExtensionPermissionReview, HostPermissionKey, Boolean) -> Unit,
    private val onOpenExtensionDetail: (String) -> Unit,
    private val onBack: () -> Unit,
    private val onIncreasePriority: (String) -> Unit,
    private val onDecreasePriority: (String) -> Unit,
) : LauncherPage(PageIds.EXTENSION_MANAGER) {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        val movableLastIndex = entries.indexOfLast { entry -> !entry.descriptor.priorityPinnedToBottom }
        val loadSubtitle = loadStatusMessage
            ?: if (onLoadExtension != null) {
                strings.extensionManagerLoadPackageSubtitle()
            } else {
                strings.extensionManagerLoadUnsupported()
            }
        return PageContributionBundle(
            sourceId = "core.page.extension_manager",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.extension_manager",
                title = PageTextRef.Direct(strings.extensionManagerTitle),
                subtitle = PageTextRef.Direct(strings.extensionManagerSubtitle),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "extension_manager_tools",
                        pageId = pageId,
                        sourceId = "core.page.extension_manager",
                        orderHint = -1,
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "extension_manager_entries",
                        pageId = pageId,
                        sourceId = "core.page.extension_manager",
                        orderHint = 1,
                    ),
                )
                if (permissionReviews.isNotEmpty()) {
                    add(
                        PageSectionRegistration(
                            nodeId = "extension_manager_permission_reviews",
                            pageId = pageId,
                            sourceId = "core.page.extension_manager",
                            orderHint = 0,
                            title = PageTextRef.Direct(strings.extensionPermissionReviewTitle()),
                            subtitle = PageTextRef.Direct(strings.extensionPermissionReviewSubtitle()),
                        ),
                    )
                }
                if (scanProblems.isNotEmpty()) {
                    add(
                        PageSectionRegistration(
                            nodeId = "extension_manager_scan_problems",
                            pageId = pageId,
                            sourceId = "core.page.extension_manager",
                            orderHint = -2,
                            title = PageTextRef.Direct(strings.extensionManagerScanProblemsTitle()),
                            subtitle = PageTextRef.Direct(strings.extensionManagerScanProblemsSubtitle()),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "extension_manager_loader",
                        pageId = pageId,
                        parentNodeId = "extension_manager_tools",
                        sourceId = "core.page.extension_manager",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.extensionManagerLoadPackageTitle()),
                            subtitle = PageTextRef.Direct(loadSubtitle),
                            actions = listOfNotNull(
                                onLoadExtension?.let { loadAction ->
                                    PageActionRegistration(
                                        id = "load_textension_extension",
                                        label = PageTextRef.Direct(strings.extensionManagerLoadPackageAction()),
                                        style = PageActionStyle.FILLED_TONAL,
                                        onClick = loadAction,
                                    )
                                },
                            ),
                        ),
                    ),
                )
                add(
                    PageWidgetRegistration(
                        nodeId = "extension_manager_install_notes",
                        pageId = pageId,
                        parentNodeId = "extension_manager_tools",
                        sourceId = "core.page.extension_manager",
                        orderHint = 1,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.extensionManagerInstalledPackagesTitle()),
                            subtitle = PageTextRef.Direct(
                                strings.extensionManagerInstalledPackagesSubtitle(installedPackageDirectory),
                            ),
                        ),
                    ),
                )
                scanProblems.forEachIndexed { index, problem ->
                    val deleteSupported = onDeleteExtensionPackage != null
                    add(
                        PageWidgetRegistration(
                            nodeId = "extension_manager_scan_problem_$index",
                            pageId = pageId,
                            parentNodeId = "extension_manager_scan_problems",
                            sourceId = "core.page.extension_manager",
                            orderHint = index,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(
                                    strings.extensionManagerScanProblemTitle(problem.sourceName),
                                ),
                                subtitle = PageTextRef.Direct(problem.message),
                                tone = PageWidgetTone.DANGER,
                                actions = listOfNotNull(
                                    if (deleteSupported) {
                                        PageActionRegistration(
                                            id = "delete_scan_problem_${problem.sourceName}",
                                            label = PageTextRef.Direct(strings.extensionManagerDeletePackageAction()),
                                            style = PageActionStyle.OUTLINED,
                                            onClick = { onDeleteExtensionPackage?.invoke(problem.sourceName) },
                                        )
                                    } else {
                                        null
                                    },
                                ),
                            ),
                        ),
                    )
                }
                permissionReviews.forEachIndexed { index, review ->
                    val requestedPermissions = review.extension.permissionKeys
                    val grantedPermissions = review.currentGrants
                    val hasDeniedGrant = grantedPermissions.any { grant -> !grant.isGranted }
                    val hasGrantedAllPermissions = requestedPermissions.isNotEmpty() &&
                        requestedPermissions.all { permissionKey ->
                            grantedPermissions.any { grant -> grant.permissionKey == permissionKey && grant.isGranted }
                        }
                    add(
                        PageSectionRegistration(
                            nodeId = "permission_review_${review.extension.identityId}_section",
                            pageId = pageId,
                            parentNodeId = "extension_manager_permission_reviews",
                            sourceId = "core.page.extension_manager",
                            orderHint = index,
                            title = PageTextRef.Direct(review.displayName),
                            subtitle = PageTextRef.Direct(
                                if (hasDeniedGrant) {
                                    strings.extensionPermissionDeniedStatus()
                                } else if (hasGrantedAllPermissions) {
                                    strings.extensionPermissionGrantedStatus()
                                } else {
                                    strings.extensionPermissionPendingStatus()
                                },
                            ),
                        ),
                    )
                    requestedPermissions.forEachIndexed { permissionIndex, permissionKey ->
                        val currentGrant = grantedPermissions.firstOrNull { grant ->
                            grant.permissionKey == permissionKey
                        }
                        add(
                            PageWidgetRegistration(
                                nodeId = "permission_toggle_${review.extension.identityId}_${permissionKey.name.lowercase()}",
                                pageId = pageId,
                                parentNodeId = "permission_review_${review.extension.identityId}_section",
                                sourceId = "core.page.extension_manager",
                                orderHint = permissionIndex,
                                widget = PageWidgetRegistrationModel.ToggleCard(
                                    title = PageTextRef.Direct(strings.hostPermissionName(permissionKey)),
                                    subtitle = PageTextRef.Direct(
                                        buildString {
                                            append(strings.hostGrantStateName(currentGrant?.state ?: HostGrantState.DENIED))
                                            append('\n')
                                            append(strings.extensionPermissionToggleReason(currentGrant?.reason))
                                        },
                                    ),
                                    checked = currentGrant?.isGranted == true,
                                    onCheckedChange = { granted ->
                                        onTogglePermissionGrant(review, permissionKey, granted)
                                    },
                                ),
                            ),
                        )
                    }
                    add(
                        PageWidgetRegistration(
                            nodeId = "permission_review_${review.extension.identityId}",
                            pageId = pageId,
                            parentNodeId = "permission_review_${review.extension.identityId}_section",
                            sourceId = "core.page.extension_manager",
                            orderHint = requestedPermissions.size,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(strings.extensionGrantedPermissionsLabel()),
                                subtitle = PageTextRef.Direct(
                                    buildString {
                                        append(strings.extensionGrantedPermissionsLabel())
                                        append(": ")
                                        append(strings.hostGrantSummary(grantedPermissions))
                                    },
                                ),
                            ),
                        ),
                    )
                }
                entries.forEachIndexed { index, entry ->
                    val descriptor = entry.descriptor
                    val extension = descriptor.extension
                    val moveUpEnabled =
                        !descriptor.priorityPinnedToBottom &&
                            index > 0 &&
                            !entries[index - 1].descriptor.priorityPinnedToBottom
                    val moveDownEnabled =
                        !descriptor.priorityPinnedToBottom &&
                            index >= 0 &&
                            index < movableLastIndex &&
                            !entries[index + 1].descriptor.priorityPinnedToBottom
                    val statusSummary = strings.extensionRuntimeStatusSummary(
                        kind = extension.kind,
                        currentTarget = currentTarget,
                        supportedTargets = extension.supportedTargets,
                        userEnabled = descriptor.userEnabled,
                        compatibility = descriptor.compatibility,
                        runtimeLoaded = descriptor.runtimeLoaded,
                        requestedPermissions = extension.permissionKeys,
                        grantedPermissions = descriptor.hostGrants,
                        runtimeLoadError = descriptor.runtimeLoadError,
                    )
                    val runtimeLoadError = descriptor.runtimeLoadError.trimmedOrNull()
                    add(
                        PageWidgetRegistration(
                            nodeId = "extension_${extension.identityId}",
                            pageId = pageId,
                            parentNodeId = "extension_manager_entries",
                            sourceId = "core.page.extension_manager",
                            orderHint = index,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(
                                    buildString {
                                        append(index + 1)
                                        append(". ")
                                        append(descriptor.displayName)
                                        if (runtimeLoadError != null) {
                                            append(" · ")
                                            append(extensionRuntimeLoadProblemBadge(strings))
                                        }
                                    },
                                ),
                                subtitle = PageTextRef.Direct(
                                    buildString {
                                        if (runtimeLoadError != null) {
                                            append(extensionRuntimeLoadProblemCardHint(strings))
                                            append('\n')
                                        }
                                        append(strings.extensionKindName(extension.kind))
                                        append('\n')
                                        append(statusSummary)
                                        append(" · ")
                                        append("P")
                                        append(entry.priority + 1)
                                    },
                                ),
                                rows = runtimeLoadError?.let { error ->
                                    listOf(row(extensionRuntimeErrorLabel(strings), error))
                                }.orEmpty(),
                                enabled = true,
                                tone = extensionCardTone(descriptor),
                                onClick = { onOpenExtensionDetail(extension.identityId) },
                                actions = listOf(
                                    PageActionRegistration(
                                        id = "priority_up_${extension.identityId}",
                                        label = PageTextRef.Direct(strings.extensionPriorityIncrease),
                                        style = PageActionStyle.OUTLINED,
                                        enabled = moveUpEnabled,
                                        onClick = { onIncreasePriority(extension.identityId) },
                                    ),
                                    PageActionRegistration(
                                        id = "priority_down_${extension.identityId}",
                                        label = PageTextRef.Direct(strings.extensionPriorityDecrease),
                                        style = PageActionStyle.OUTLINED,
                                        enabled = moveDownEnabled,
                                        onClick = { onDecreasePriority(extension.identityId) },
                                    ),
                                ),
                            ),
                        ),
                    )
                }
            },
        )
    }
}

class ExtensionDetailPage(
    private val entry: ExtensionPriorityEntry,
    private val currentTarget: PlatformTarget,
    private val onToggleExtensionEnabled: (String, Boolean) -> Unit,
    private val onDeleteExtensionPackage: ((String) -> Unit)?,
    private val onBack: () -> Unit,
) : LauncherPage("extension_detail_${entry.descriptor.extension.identityId}") {
    override fun buildBaseContribution(context: PageContext): PageContributionBundle {
        val strings = context.strings
        val descriptor = entry.descriptor
        val extension = descriptor.extension
        val statusSummary = strings.extensionRuntimeStatusSummary(
            kind = extension.kind,
            currentTarget = currentTarget,
            supportedTargets = extension.supportedTargets,
            userEnabled = descriptor.userEnabled,
            compatibility = descriptor.compatibility,
            runtimeLoaded = descriptor.runtimeLoaded,
            requestedPermissions = extension.permissionKeys,
            grantedPermissions = descriptor.hostGrants,
            runtimeLoadError = descriptor.runtimeLoadError,
        )
        val runtimeLoadError = descriptor.runtimeLoadError.trimmedOrNull()
        val sourceName = descriptor.sourceName
        val deleteSupported = onDeleteExtensionPackage != null && !sourceName.isNullOrBlank()
        return PageContributionBundle(
            sourceId = "core.page.extension_detail",
            page = PageRegistration(
                id = pageId,
                sourceId = "core.page.extension_detail",
                title = PageTextRef.Direct(descriptor.displayName),
                subtitle = PageTextRef.Direct(statusSummary),
                actionLabel = PageTextRef.Direct(strings.commonBack),
                action = onBack,
            ),
            nodes = buildList {
                add(
                    PageSectionRegistration(
                        nodeId = "extension_detail_controls",
                        pageId = pageId,
                        sourceId = "core.page.extension_detail",
                        orderHint = 0,
                    ),
                )
                add(
                    PageSectionRegistration(
                        nodeId = "extension_detail_info",
                        pageId = pageId,
                        sourceId = "core.page.extension_detail",
                        orderHint = 1,
                        title = PageTextRef.Direct(strings.extensionDetailsTitle()),
                    ),
                )
                if (runtimeLoadError != null) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "extension_detail_runtime_error",
                            pageId = pageId,
                            parentNodeId = "extension_detail_controls",
                            sourceId = "core.page.extension_detail",
                            orderHint = -1,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(extensionRuntimeLoadProblemTitle(strings)),
                                subtitle = PageTextRef.Direct(
                                    extensionRuntimeLoadProblemDetailMessage(strings, runtimeLoadError),
                                ),
                                tone = PageWidgetTone.DANGER,
                            ),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "extension_detail_enabled",
                        pageId = pageId,
                        parentNodeId = "extension_detail_controls",
                        sourceId = "core.page.extension_detail",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.ToggleCard(
                            title = PageTextRef.Direct(strings.extensionInstalledToggleTitle()),
                            subtitle = PageTextRef.Direct(strings.extensionInstalledToggleSubtitle()),
                            checked = descriptor.userEnabled,
                            enabled = true,
                            onCheckedChange = { enabled ->
                                onToggleExtensionEnabled(extension.identityId, enabled)
                            },
                        ),
                    ),
                )
                if (deleteSupported) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "extension_detail_delete",
                            pageId = pageId,
                            parentNodeId = "extension_detail_controls",
                            sourceId = "core.page.extension_detail",
                            orderHint = 1,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(strings.extensionManagerDeletePackageAction()),
                                subtitle = PageTextRef.Direct(strings.extensionManagerInstalledPackageManagedNote()),
                                tone = PageWidgetTone.DANGER,
                                actions = listOf(
                                    PageActionRegistration(
                                        id = "delete_package_${extension.identityId}",
                                        label = PageTextRef.Direct(strings.extensionManagerDeletePackageAction()),
                                        style = PageActionStyle.OUTLINED,
                                        onClick = {
                                            sourceName?.let { packageSourceName ->
                                                onDeleteExtensionPackage?.invoke(packageSourceName)
                                            }
                                        },
                                    ),
                                ),
                            ),
                        ),
                    )
                }
                add(
                    PageWidgetRegistration(
                        nodeId = "extension_detail_rows",
                        pageId = pageId,
                        parentNodeId = "extension_detail_info",
                        sourceId = "core.page.extension_detail",
                        orderHint = 0,
                        widget = PageWidgetRegistrationModel.DetailCard(
                            title = PageTextRef.Direct(strings.extensionDetailsTitle()),
                            rows = extensionDetailRows(
                                strings = strings,
                                entry = entry,
                                statusSummary = statusSummary,
                            ),
                            tone = extensionCardTone(descriptor),
                        ),
                    ),
                )
                if (extension.permissionKeys.isNotEmpty()) {
                    add(
                        PageWidgetRegistration(
                            nodeId = "extension_detail_permission_note",
                            pageId = pageId,
                            parentNodeId = "extension_detail_info",
                            sourceId = "core.page.extension_detail",
                            orderHint = 1,
                            widget = PageWidgetRegistrationModel.DetailCard(
                                title = PageTextRef.Direct(strings.extensionRequestedPermissionsLabel()),
                                subtitle = PageTextRef.Direct(strings.extensionPermissionBehaviorNote()),
                            ),
                        ),
                    )
                }
            },
        )
    }
}

private fun extensionCardTone(descriptor: com.dreamyloong.tlauncher.core.model.ExtensionDescriptor): PageWidgetTone {
    return when {
        !descriptor.compatibility.isCompatible || !descriptor.runtimeLoadError.isNullOrBlank() ->
            PageWidgetTone.DANGER

        descriptor.runtimeLoaded -> PageWidgetTone.ACCENT
        else -> PageWidgetTone.DEFAULT
    }
}

private fun extensionDetailRows(
    strings: AppStrings,
    entry: ExtensionPriorityEntry,
    statusSummary: String,
): List<PageValueItemRegistration> {
    val descriptor = entry.descriptor
    val extension = descriptor.extension
    val requestedPermissions = extension.permissionKeys
    val grantedPermissions = descriptor.hostGrants
    return buildList {
        add(row(strings.commonStatus, statusSummary))
        add(row(extensionKindLabel(strings), strings.extensionKindName(extension.kind)))
        add(row(strings.commonPlatformLabel, extension.supportedTargets.joinToString { target -> strings.platformName(target) }))
        add(row(strings.extensionCompatibilityLabel(), strings.extensionCompatibilitySummary(descriptor.compatibility)))
        add(row(strings.commonSource, strings.extensionSourceSummary(descriptor.sourceName)))
        add(row("ID", extension.registrationId))
        if (extension.identityId != extension.registrationId) {
            add(row("Identity", extension.identityId))
        }
        add(row(extensionPriorityLabel(strings), "P${entry.priority + 1}"))
        add(row(strings.extensionPackageVersionLabel(), descriptor.packageVersion ?: strings.commonNone))
        add(row(strings.extensionSdkApiVersionLabel(), descriptor.apiVersion ?: strings.commonNone))
        add(row(strings.extensionDescriptionLabel(), descriptor.packageDescription ?: strings.extensionDescriptionFallback()))
        if (requestedPermissions.isNotEmpty()) {
            add(
                row(
                    strings.extensionRequestedPermissionsLabel(),
                    requestedPermissions.joinToString { permissionKey ->
                        strings.hostPermissionName(permissionKey)
                    },
                ),
            )
        }
        if (grantedPermissions.isNotEmpty()) {
            add(row(strings.extensionGrantedPermissionsLabel(), strings.hostGrantSummary(grantedPermissions)))
        }
        if (!descriptor.runtimeLoadError.isNullOrBlank()) {
            add(row(extensionRuntimeErrorLabel(strings), descriptor.runtimeLoadError))
        }
    }
}

private fun extensionKindLabel(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "类型"
        SupportedLanguage.EN_US -> "Kind"
    }
}

private fun extensionPriorityLabel(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "优先级"
        SupportedLanguage.EN_US -> "Priority"
    }
}

private fun extensionRuntimeErrorLabel(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "扩展错误"
        SupportedLanguage.EN_US -> "Extension Error"
    }
}

private fun extensionRuntimeLoadProblemBadge(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "加载失败"
        SupportedLanguage.EN_US -> "Load Failed"
    }
}

private fun extensionRuntimeLoadProblemCardHint(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "这个扩展在加载或执行时失败了，相关能力暂时不会生效。"
        SupportedLanguage.EN_US -> "This extension failed while loading or running, so its features are inactive for now."
    }
}

private fun extensionRuntimeLoadProblemTitle(strings: AppStrings): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "扩展加载失败"
        SupportedLanguage.EN_US -> "Extension Load Failed"
    }
}

private fun extensionRuntimeLoadProblemDetailMessage(
    strings: AppStrings,
    message: String,
): String {
    return when (strings.language) {
        SupportedLanguage.ZH_CN -> "扩展在加载或执行时失败了，模板、主题或插件能力不会生效。\n错误：$message"
        SupportedLanguage.EN_US -> "The extension failed while loading or running, so template, theme, or plugin features will not be active.\nError: $message"
    }
}

private fun row(
    label: String,
    value: String,
): PageValueItemRegistration {
    return PageValueItemRegistration(
        label = PageTextRef.Direct(label),
        value = PageTextRef.Direct(value),
    )
}

private fun String?.trimmedOrNull(): String? {
    return this?.trim()?.takeIf { value -> value.isNotEmpty() }
}
