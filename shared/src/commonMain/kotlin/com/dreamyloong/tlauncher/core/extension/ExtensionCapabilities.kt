package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionKind

enum class ExtensionCapability {
    DEFINE_TEMPLATE_METADATA,
    DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS,
    PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS,
    PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS,

    DEFINE_THEME_METADATA,
    DEFINE_THEME_TOKENS,
    DEFINE_THEME_SCENE,

    PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
    PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
    MUTATE_PAGE_TREE,
    INTERCEPT_ACTIONS,
    INTERCEPT_GAME_LAUNCH,
    INTERCEPT_TEMPLATE_LAUNCH_PREPARATION,
    READ_GAME_LIBRARY,
    WRITE_GAME_LIBRARY,
    OPEN_EXTERNAL_URLS,
    NETWORK_ACCESS,
    FILESYSTEM_READ,
    FILESYSTEM_WRITE,
}

object ExtensionCapabilityPolicy {
    private val templateCapabilities = setOf(
        ExtensionCapability.DEFINE_TEMPLATE_METADATA,
        ExtensionCapability.DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS,
        ExtensionCapability.PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS,
        ExtensionCapability.PROVIDE_TEMPLATE_SETTINGS_CONTRIBUTIONS,
    )

    private val themeCapabilities = setOf(
        ExtensionCapability.DEFINE_THEME_METADATA,
        ExtensionCapability.DEFINE_THEME_TOKENS,
        ExtensionCapability.DEFINE_THEME_SCENE,
    )

    private val pluginCapabilities = setOf(
        ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
        ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
        ExtensionCapability.MUTATE_PAGE_TREE,
        ExtensionCapability.INTERCEPT_ACTIONS,
        ExtensionCapability.INTERCEPT_GAME_LAUNCH,
        ExtensionCapability.INTERCEPT_TEMPLATE_LAUNCH_PREPARATION,
        ExtensionCapability.READ_GAME_LIBRARY,
        ExtensionCapability.WRITE_GAME_LIBRARY,
        ExtensionCapability.OPEN_EXTERNAL_URLS,
        ExtensionCapability.NETWORK_ACCESS,
        ExtensionCapability.FILESYSTEM_READ,
        ExtensionCapability.FILESYSTEM_WRITE,
    )

    fun allowedFor(kind: ExtensionKind): Set<ExtensionCapability> {
        return when (kind) {
            ExtensionKind.TEMPLATE -> templateCapabilities
            ExtensionKind.THEME -> themeCapabilities
            ExtensionKind.PLUGIN -> pluginCapabilities
        }
    }

    fun defaultFor(kind: ExtensionKind): Set<ExtensionCapability> {
        return when (kind) {
            ExtensionKind.TEMPLATE -> templateCapabilities
            ExtensionKind.THEME -> themeCapabilities
            ExtensionKind.PLUGIN -> setOf(
                ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
                ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS,
            )
        }
    }

    fun invalidFor(
        kind: ExtensionKind,
        declared: Set<ExtensionCapability>,
    ): Set<ExtensionCapability> {
        return declared - allowedFor(kind)
    }

    fun isValid(
        kind: ExtensionKind,
        declared: Set<ExtensionCapability>,
    ): Boolean {
        return invalidFor(kind, declared).isEmpty()
    }
}
