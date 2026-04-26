package com.dreamyloong.tlauncher.data.plugin

import com.dreamyloong.tlauncher.core.extension.ExtensionContext
import com.dreamyloong.tlauncher.core.extension.ExtensionFeaturePolicy
import com.dreamyloong.tlauncher.core.extension.ExtensionRegistry
import com.dreamyloong.tlauncher.core.extension.ExtensionStateStore
import com.dreamyloong.tlauncher.core.extension.EmptyExtensionStateStore
import com.dreamyloong.tlauncher.core.extension.LauncherExtension
import com.dreamyloong.tlauncher.core.extension.RegisteredExtensionFeature

class RuntimeExtensionRegistry(
    private val extensionContext: ExtensionContext,
    private val installedExtensions: List<LauncherExtension> = emptyList(),
    private val extensionStateStoreFactory: (String) -> ExtensionStateStore = { EmptyExtensionStateStore },
) : ExtensionRegistry {
    private val extensionFailuresByIdentity = linkedMapOf<String, LinkedHashMap<String, String>>()
    private var cachedInstalledExtensionSnapshot: List<LauncherExtension> = emptyList()
    private var cachedRegisteredFeatures: List<RegisteredExtensionFeature> = emptyList()

    init {
        installedExtensions.forEach(ExtensionFeaturePolicy::validateExtension)
    }

    override fun installedExtensions(): List<LauncherExtension> = installedExtensions

    override fun extensionFailureSnapshot(): Map<String, String> {
        return extensionFailuresByIdentity.mapValues { (_, stageMessages) ->
            stageMessages.values.joinToString("\n")
        }
    }

    override fun reportExtensionFailure(
        owner: LauncherExtension,
        stage: String,
        throwable: Throwable,
    ) {
        val message = buildFailureMessage(stage, throwable)
        val stageMessages = extensionFailuresByIdentity.getOrPut(owner.extension.identityId) {
            linkedMapOf()
        }
        stageMessages[stage] = message
    }

    override fun clearExtensionFailure(
        owner: LauncherExtension,
        stage: String,
    ) {
        val stageMessages = extensionFailuresByIdentity[owner.extension.identityId] ?: return
        stageMessages.remove(stage)
        if (stageMessages.isEmpty()) {
            extensionFailuresByIdentity.remove(owner.extension.identityId)
        }
    }

    fun clearExtensionFailures(identityId: String) {
        extensionFailuresByIdentity.remove(identityId)
    }

    override fun registeredFeatures(): List<RegisteredExtensionFeature> {
        val installedSnapshot = installedExtensions.toList()
        if (installedSnapshot.matchesCachedSnapshot()) {
            return cachedRegisteredFeatures
        }

        val resolvedFeatures = installedSnapshot.flatMap { extension ->
            runCatching {
                clearExtensionFailure(extension, STAGE_FEATURE_INITIALIZATION)
                ExtensionFeaturePolicy.validateExtension(extension)
                val scopedContext = extensionContext
                    .withHostGrants(extension.hostGrants)
                    .withPackageResources(extension.packageResources)
                    .withStateStore(extensionStateStoreFactory(extension.extension.identityId))
                extension.createFeatures(scopedContext).map { feature ->
                    ExtensionFeaturePolicy.validateFeature(extension, feature)
                    RegisteredExtensionFeature(
                        owner = extension,
                        feature = feature,
                    )
                }
            }.getOrElse { error ->
                reportExtensionFailure(extension, STAGE_FEATURE_INITIALIZATION, error)
                emptyList()
            }
        }
        cachedInstalledExtensionSnapshot = installedSnapshot
        cachedRegisteredFeatures = resolvedFeatures
        return resolvedFeatures
    }

    private fun List<LauncherExtension>.matchesCachedSnapshot(): Boolean {
        if (size != cachedInstalledExtensionSnapshot.size) {
            return false
        }
        return indices.all { index -> this[index] === cachedInstalledExtensionSnapshot[index] }
    }

    private fun buildFailureMessage(
        stage: String,
        throwable: Throwable,
    ): String {
        val detail = throwable.message?.trim().takeUnless { it.isNullOrEmpty() }
            ?: throwable::class.simpleName
            ?: "Unknown error"
        return "${stageLabel(stage)}: $detail"
    }

    private fun stageLabel(stage: String): String {
        return when {
            stage == STAGE_FEATURE_INITIALIZATION -> "Feature initialization failed"
            stage == STAGE_TEMPLATE_PACKAGES -> "Template metadata failed"
            stage == STAGE_THEME_PACKAGES -> "Theme metadata failed"
            stage.startsWith(STAGE_PAGE_CONTRIBUTIONS_PREFIX) -> {
                val pageId = stage.removePrefix(STAGE_PAGE_CONTRIBUTIONS_PREFIX)
                "Page contributions failed on $pageId"
            }

            stage.startsWith(STAGE_PAGE_MUTATOR_PREFIX) -> {
                val pageId = stage.removePrefix(STAGE_PAGE_MUTATOR_PREFIX)
                "Page mutation failed on $pageId"
            }

            else -> "Extension execution failed"
        }
    }

    companion object {
        const val STAGE_FEATURE_INITIALIZATION = "feature_initialization"
        const val STAGE_TEMPLATE_PACKAGES = "template_packages"
        const val STAGE_THEME_PACKAGES = "theme_packages"
        const val STAGE_PAGE_CONTRIBUTIONS_PREFIX = "page_contributions:"
        const val STAGE_PAGE_MUTATOR_PREFIX = "page_mutator:"
    }
}
