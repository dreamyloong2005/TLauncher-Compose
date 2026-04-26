package com.dreamyloong.tlauncher.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePickerState(): FilePickerState {
    return FilePickerState(
        isSupported = true,
        pickFile = { acceptedMimeTypes, onPicked ->
            val file = WindowsNativeFileDialog.pickFile(
                title = "Choose File",
                filters = filtersForMimeTypes(acceptedMimeTypes),
            )
            onPicked(
                file?.let { selectedFile ->
                    PickedFile(
                        name = selectedFile.name,
                        bytes = selectedFile.readBytes(),
                    )
                },
            )
        },
    )
}

private fun filtersForMimeTypes(acceptedMimeTypes: List<String>): List<WindowsFileDialogFilter> {
    val extensions = acceptedMimeTypes
        .flatMap(::extensionsForMimeType)
        .distinct()
    if (extensions.isEmpty()) {
        return emptyList()
    }
    val supportedFilter = WindowsFileDialogFilter(
        name = buildString {
            append("Supported Files (")
            append(extensions.joinToString(", ") { extension -> "*.$extension" })
            append(')')
        },
        spec = extensions.joinToString(";") { extension -> "*.$extension" },
    )
    return if (acceptedMimeTypes.any { mimeType -> mimeType.trim() == "*/*" } || extensions.size > 1) {
        listOf(supportedFilter, WindowsFileDialogFilter("All Files (*.*)", "*.*"))
    } else {
        listOf(supportedFilter)
    }
}

private fun extensionsForMimeType(mimeType: String): List<String> {
    return when (mimeType.trim().lowercase()) {
        "application/json", "text/json" -> listOf("json")
        "application/zip", "application/x-zip-compressed" -> listOf("zip")
        "application/x-msdownload" -> listOf("dll")
        else -> emptyList()
    }
}
