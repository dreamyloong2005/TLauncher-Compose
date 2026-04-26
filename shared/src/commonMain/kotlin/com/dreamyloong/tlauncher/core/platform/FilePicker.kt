package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
)

data class FilePickerState(
    val isSupported: Boolean,
    val pickFile: (acceptedMimeTypes: List<String>, onPicked: (PickedFile?) -> Unit) -> Unit,
)

@Composable
expect fun rememberFilePickerState(): FilePickerState
