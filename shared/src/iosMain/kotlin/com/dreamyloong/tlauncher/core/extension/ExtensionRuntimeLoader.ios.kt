package com.dreamyloong.tlauncher.core.extension

import com.dreamyloong.tlauncher.core.model.PlatformTarget

actual object ExtensionRuntimeLoader {
    actual fun load(
        parsedPackage: ParsedExtensionPackage,
        source: ExtensionPackageSource,
        target: PlatformTarget,
    ): LoadedExtensionRuntime {
        throw ExtensionRuntimeLoadException("Dynamic .textension runtime loading is not supported on iOS yet.")
    }
}
