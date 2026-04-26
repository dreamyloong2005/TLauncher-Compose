package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

data class DirectoryPickerState(
    val isSupported: Boolean,
    val pickDirectory: (initialPath: String?, onPicked: (String?) -> Unit) -> Unit,
)

@Composable
expect fun rememberDirectoryPickerState(): DirectoryPickerState
