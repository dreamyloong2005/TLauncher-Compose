package com.example.tlauncher

import com.dreamyloong.tlauncher.sdk.extension.ExtensionCapability
import com.dreamyloong.tlauncher.sdk.extension.ExtensionCompatibility
import com.dreamyloong.tlauncher.sdk.extension.ExtensionContext
import com.dreamyloong.tlauncher.sdk.extension.ExtensionEntrypoint
import com.dreamyloong.tlauncher.sdk.extension.ExtensionFeature
import com.dreamyloong.tlauncher.sdk.extension.LauncherExtension
import com.dreamyloong.tlauncher.sdk.model.ExtensionKind
import com.dreamyloong.tlauncher.sdk.model.ExtensionManifest
import com.dreamyloong.tlauncher.sdk.model.PlatformTarget

object ExamplePluginEntrypoint : ExtensionEntrypoint {
    override fun createExtension(): LauncherExtension = ExamplePluginExtension()
}

private class ExamplePluginExtension : LauncherExtension {
    override val extension: ExtensionManifest = ExtensionManifest(
        id = "plugin.example.hello.windows",
        kind = ExtensionKind.PLUGIN,
        supportedTargets = setOf(PlatformTarget.WINDOWS),
        capabilities = setOf(
            ExtensionCapability.PROVIDE_PLUGIN_PAGE_CONTRIBUTIONS,
        ),
        compatibility = ExtensionCompatibility(),
    )
    override val displayName: String = "Example Plugin"
    override val version: String = "1.0.0"
    override val apiVersion: String = "1.0.0"
    override val entrypoint: String = ExamplePluginEntrypoint::class.qualifiedName.orEmpty()

    override fun createFeatures(context: ExtensionContext): List<ExtensionFeature> {
        return emptyList()
    }
}
