package com.dreamyloong.tlauncher.core.template

import com.dreamyloong.tlauncher.core.model.ExtensionIdentityId
import com.dreamyloong.tlauncher.core.page.PageContext
import com.dreamyloong.tlauncher.core.page.PageContributionBundle

interface TemplatePageContributionProvider {
    fun providePageContributions(
        context: PageContext,
    ): List<PageContributionBundle>
}

interface TemplatePageContributionProviderRegistry {
    fun providerFor(templatePackageId: ExtensionIdentityId): TemplatePageContributionProvider?
}
