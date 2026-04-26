package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.ExtensionKind
import com.dreamyloong.tlauncher.core.plugin.ActionInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.LaunchInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.PageContributionProviderExtension
import com.dreamyloong.tlauncher.core.plugin.PageTreeMutatorExtension
import com.dreamyloong.tlauncher.core.plugin.SettingsSectionMutatorExtension
import com.dreamyloong.tlauncher.core.plugin.SettingsSectionProviderExtension
import com.dreamyloong.tlauncher.core.plugin.TemplateLaunchPreparationInterceptorExtension
import com.dreamyloong.tlauncher.core.plugin.TemplateProviderExtension
import com.dreamyloong.tlauncher.core.plugin.ThemeProviderExtension

data class ExtensionFeaturePolicyRule(
    val featureLabel: String,
    val allowedKinds: Set<ExtensionKind>,
    val requiredCapabilities: Set<ExtensionCapability>,
    val supported: Boolean = true,
)

class ExtensionRegistrationException(
    message: String,
) : IllegalArgumentException(message)

object ExtensionFeaturePolicy {
    fun validateExtension(extension: LauncherExtension) {
        val manifest = extension.extension
        if (!manifest.hasValidCapabilityDeclaration()) {
            throw ExtensionRegistrationException(
                "Extension ${manifest.registrationId} declares invalid capabilities: ${manifest.invalidCapabilities().joinToString()}",
            )
        }
        if (!manifest.hasValidPermissionDeclaration()) {
            val invalidPermissions = manifest.invalidPermissionKeys()
            val missingPermissions = manifest.missingPermissionKeysForCapabilities()
            val details = buildList {
                if (invalidPermissions.isNotEmpty()) {
                    add("invalid permissions: ${invalidPermissions.joinToString()}")
                }
                if (missingPermissions.isNotEmpty()) {
                    add("missing required permissions: ${missingPermissions.joinToString()}")
                }
            }.joinToString("; ")
            throw ExtensionRegistrationException(
                "Extension ${manifest.registrationId} declares invalid host permissions: $details",
            )
        }
        if (extension.displayName.isBlank()) {
            throw ExtensionRegistrationException("Extension ${manifest.registrationId} must have a non-blank displayName.")
        }
        if (extension.version.isBlank()) {
            throw ExtensionRegistrationException("Extension ${manifest.registrationId} must have a non-blank version.")
        }
        if (extension.apiVersion.isBlank()) {
            throw ExtensionRegistrationException("Extension ${manifest.registrationId} must have a non-blank apiVersion.")
        }
        if (extension.entrypoint.isBlank()) {
            throw ExtensionRegistrationException("Extension ${manifest.registrationId} must have a non-blank entrypoint.")
        }
    }

    fun validateFeature(
        owner: LauncherExtension,
        feature: ExtensionFeature,
    ) {
        val manifest = owner.extension
        val rule = ruleFor(
            ownerKind = manifest.kind,
            feature = feature,
        )
        if (!rule.supported) {
            throw ExtensionRegistrationException(
                "Feature ${rule.featureLabel} is not supported for SDK registration in ${manifest.registrationId}.",
            )
        }
        if (manifest.kind !in rule.allowedKinds) {
            throw ExtensionRegistrationException(
                "Feature ${rule.featureLabel} is not allowed for ${manifest.kind.name.lowercase()} extension ${manifest.registrationId}.",
            )
        }
        val missingCapabilities = rule.requiredCapabilities - manifest.capabilities
        if (missingCapabilities.isNotEmpty()) {
            throw ExtensionRegistrationException(
                "Extension ${manifest.registrationId} is missing required capabilities for ${rule.featureLabel}: ${missingCapabilities.joinToString()}",
            )
        }
    }

    private fun ruleFor(
        ownerKind: ExtensionKind,
        feature: ExtensionFeature,
    ): ExtensionFeaturePolicyRule {
        return when (feature) {
            is PageContributionProviderExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "PageContributionProviderExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN, ExtensionKind.TEMPLATE),
                requiredCapabilities = when (ownerKind) {
                    ExtensionKind.TEMPLATE -> setOf(ExtensionCapability.PROVIDE_TEMPLATE_PAGE_CONTRIBUTIONS)
                    ExtensionKind.PLUGIN -> setOf(ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS)
                    ExtensionKind.THEME -> emptySet()
                },
            )

            is SettingsSectionProviderExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "SettingsSectionProviderExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS),
            )

            is SettingsSectionMutatorExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "SettingsSectionMutatorExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.PROVIDE_PLUGIN_SETTINGS_CONTRIBUTIONS),
            )

            is PageTreeMutatorExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "PageTreeMutatorExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.MUTATE_PAGE_TREE),
            )

            is ActionInterceptorExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "ActionInterceptorExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.INTERCEPT_ACTIONS),
            )

            is LaunchInterceptorExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "LaunchInterceptorExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.INTERCEPT_GAME_LAUNCH),
            )

            is TemplateLaunchPreparationInterceptorExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "TemplateLaunchPreparationInterceptorExtension",
                allowedKinds = setOf(ExtensionKind.PLUGIN),
                requiredCapabilities = setOf(ExtensionCapability.INTERCEPT_TEMPLATE_LAUNCH_PREPARATION),
            )

            is TemplateProviderExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "TemplateProviderExtension",
                allowedKinds = setOf(ExtensionKind.TEMPLATE),
                requiredCapabilities = setOf(
                    ExtensionCapability.DEFINE_TEMPLATE_METADATA,
                    ExtensionCapability.DEFINE_TEMPLATE_RUNTIME_REQUIREMENTS,
                ),
            )

            is ThemeProviderExtension -> ExtensionFeaturePolicyRule(
                featureLabel = "ThemeProviderExtension",
                allowedKinds = setOf(ExtensionKind.THEME),
                requiredCapabilities = setOf(
                    ExtensionCapability.DEFINE_THEME_METADATA,
                    ExtensionCapability.DEFINE_THEME_TOKENS,
                    ExtensionCapability.DEFINE_THEME_SCENE,
                ),
            )

            else -> ExtensionFeaturePolicyRule(
                featureLabel = feature::class.simpleName ?: "UnknownFeature",
                allowedKinds = emptySet(),
                requiredCapabilities = emptySet(),
                supported = false,
            )
        }
    }
}
