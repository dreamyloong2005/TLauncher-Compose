package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberDirectoryPickerState(): DirectoryPickerState {
    return DirectoryPickerState(
        isSupported = false,
        pickDirectory = { _, onPicked -> onPicked(null) },
    )
}
