package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.extension.ZipExtensionPackageSource

@Composable
actual fun rememberExtensionPackagePickerState(): ExtensionPackagePickerState {
    return ExtensionPackagePickerState(
        isSupported = true,
        pickPackage = { onPicked ->
            onPicked(
                WindowsNativeFileDialog.pickExtensionPackage()?.let { selectedFile ->
                    ZipExtensionPackageSource(selectedFile)
                },
            )
        },
    )
}
