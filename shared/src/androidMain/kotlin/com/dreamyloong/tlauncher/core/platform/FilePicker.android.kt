package com.dreamyloong.tlauncher.core.platform

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePickerState(): FilePickerState {
    val context = LocalContext.current
    val pendingCallbackState = remember {
        mutableStateOf<((PickedFile?) -> Unit)?>(null)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val callback = pendingCallbackState.value
        pendingCallbackState.value = null
        if (uri == null || callback == null) {
            callback?.invoke(null)
            return@rememberLauncherForActivityResult
        }

        val picked = runCatching {
            val fileName = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else {
                    null
                }
            }?.takeIf { it.isNotBlank() } ?: "picked_${System.currentTimeMillis()}"
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            } ?: return@runCatching null
            PickedFile(
                name = fileName,
                bytes = bytes,
            )
        }.getOrNull()

        callback(picked)
    }

    return remember(launcher) {
        FilePickerState(
            isSupported = true,
            pickFile = { acceptedMimeTypes, onPicked ->
                pendingCallbackState.value = onPicked
                launcher.launch(acceptedMimeTypes.ifEmpty { listOf("*/*") }.toTypedArray())
            },
        )
    }
}
