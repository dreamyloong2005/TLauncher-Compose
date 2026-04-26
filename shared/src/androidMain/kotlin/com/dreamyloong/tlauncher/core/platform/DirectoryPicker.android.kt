package com.dreamyloong.tlauncher.core.platform

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDirectoryPickerState(): DirectoryPickerState {
    val context = LocalContext.current
    val pendingCallbackState = remember {
        mutableStateOf<((String?) -> Unit)?>(null)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
        }
        pendingCallbackState.value?.invoke(uri?.toLauncherPath())
        pendingCallbackState.value = null
    }

    return DirectoryPickerState(
        isSupported = true,
        pickDirectory = { initialPath, onPicked ->
            pendingCallbackState.value = onPicked
            val initialUri: Uri? = initialPath?.toDocumentTreeUri()
            launcher.launch(initialUri)
        },
    )
}

private fun Uri.toLauncherPath(): String {
    val documentId = DocumentsContract.getTreeDocumentId(this)
    return documentId.toStoragePath() ?: toString()
}

private fun String.toDocumentTreeUri(): Uri? {
    val normalized = replace('\\', '/').removeSuffix("/")
    if (!normalized.startsWith("/storage/emulated/0")) {
        return null
    }
    val relativePath = normalized.removePrefix("/storage/emulated/0").trimStart('/')
    val documentId = if (relativePath.isBlank()) {
        "primary:"
    } else {
        "primary:$relativePath"
    }
    return DocumentsContract.buildTreeDocumentUri(
        "com.android.externalstorage.documents",
        documentId,
    )
}

private fun String.toStoragePath(): String? {
    return when {
        startsWith("primary:") -> {
            val relativePath = removePrefix("primary:").trimStart('/')
            if (relativePath.isBlank()) {
                "/storage/emulated/0"
            } else {
                "/storage/emulated/0/$relativePath"
            }
        }

        else -> null
    }
}
