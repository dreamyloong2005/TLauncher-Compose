package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberExtensionPackagePickerState(): ExtensionPackagePickerState {
    return ExtensionPackagePickerState(
        isSupported = false,
        pickPackage = { onPicked -> onPicked(null) },
    )
}
