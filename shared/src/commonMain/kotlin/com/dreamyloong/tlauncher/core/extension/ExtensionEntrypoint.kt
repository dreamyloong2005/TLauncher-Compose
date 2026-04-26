package com.dreamyloong.tlauncher.core.extension

/**
 * Stable SDK entrypoint contract for third-party extension runtime artifacts.
 *
 * A target-specific runtime artifact referenced by a `.textension` package
 * should expose a class that implements this interface. The host-side dynamic
 * loader can then instantiate that class and ask it to create one
 * [LauncherExtension] instance.
 */
interface ExtensionEntrypoint {
    fun createExtension(): LauncherExtension
}
