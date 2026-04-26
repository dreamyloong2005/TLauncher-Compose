package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource

data class ExtensionPackagePickerState(
    val isSupported: Boolean,
    val pickPackage: (onPicked: (ExtensionPackageSource?) -> Unit) -> Unit,
)

@Composable
expect fun rememberExtensionPackagePickerState(): ExtensionPackagePickerState
