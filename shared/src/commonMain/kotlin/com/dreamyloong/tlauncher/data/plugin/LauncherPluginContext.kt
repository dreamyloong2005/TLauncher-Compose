package com.dreamyloong.tlauncher.data.plugin

import com.dreamyloong.tlauncher.core.extension.ExtensionContext
import com.dreamyloong.tlauncher.core.extension.ExtensionHostPaths
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageResources
import com.dreamyloong.tlauncher.core.extension.EmptyExtensionPackageResources
import com.dreamyloong.tlauncher.core.extension.EmptyExtensionStateStore
import com.dreamyloong.tlauncher.core.extension.ExtensionSdkContract
import com.dreamyloong.tlauncher.core.extension.HostGrant
import com.dreamyloong.tlauncher.core.extension.ExtensionStateStore
import com.dreamyloong.tlauncher.sdk.host.EmptyExtensionHostServices
import com.dreamyloong.tlauncher.sdk.host.ExtensionHostServices

data class LauncherExtensionContext(
    override val apiVersion: String = ExtensionSdkContract.SDK_VERSION,
    override val hostGrants: Set<HostGrant> = emptySet(),
    override val packageResources: ExtensionPackageResources = EmptyExtensionPackageResources,
    override val stateStore: ExtensionStateStore = EmptyExtensionStateStore,
    override val hostPaths: ExtensionHostPaths = ExtensionHostPaths(),
    override val hostServices: ExtensionHostServices = EmptyExtensionHostServices,
) : ExtensionContext {
    override fun withHostGrants(grants: Set<HostGrant>): ExtensionContext {
        return copy(hostGrants = grants)
    }

    override fun withPackageResources(resources: ExtensionPackageResources): ExtensionContext {
        return copy(packageResources = resources)
    }

    override fun withStateStore(store: ExtensionStateStore): ExtensionContext {
        return copy(stateStore = store)
    }

    override fun withHostPaths(paths: ExtensionHostPaths): ExtensionContext {
        return copy(hostPaths = paths)
    }

    override fun withHostServices(services: ExtensionHostServices): ExtensionContext {
        return copy(hostServices = services)
    }
}
