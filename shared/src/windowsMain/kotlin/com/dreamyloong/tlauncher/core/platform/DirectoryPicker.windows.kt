package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberDirectoryPickerState(): DirectoryPickerState {
    return DirectoryPickerState(
        isSupported = true,
        pickDirectory = { initialPath, onPicked ->
            onPicked(WindowsNativeFileDialog.pickDirectory(initialPath))
        },
    )
}
