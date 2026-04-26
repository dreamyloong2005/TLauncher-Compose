package com.dreamyloong.tlauncher.core.template

import com.dreamyloong.tlauncher.core.model.Template
import com.dreamyloong.tlauncher.core.model.TemplateDescriptor
import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.model.PlatformTarget

interface TemplateRegistry {
    fun allTemplates(): List<TemplateDescriptor>

    fun resolveTemplate(
        templatePackageId: ExtensionIdentityId,
        target: PlatformTarget,
    ): Template? {
        return allTemplates()
            .firstOrNull { descriptor -> descriptor.packageId == templatePackageId }
            ?.resolve(target)
    }
}

interface CompatibilityEvaluator {
    fun compatibleTemplates(
        templates: List<TemplateDescriptor>,
        target: PlatformTarget,
    ): List<Template>
}

class DefaultCompatibilityEvaluator : CompatibilityEvaluator {
    override fun compatibleTemplates(
        templates: List<TemplateDescriptor>,
        target: PlatformTarget,
    ): List<Template> {
        return templates.mapNotNull { template -> template.resolve(target) }
    }
}
