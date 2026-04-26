package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerState(): FilePickerState {
    return FilePickerState(
        isSupported = false,
        pickFile = { _, onPicked -> onPicked(null) },
    )
}
