package com.dreamyloong.tlauncher.core.platform

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dreamyloong.tlauncher.core.extension.ExtensionPackageSource
import com.dreamyloong.tlauncher.core.extension.TExtensionPackageFormat
import com.dreamyloong.tlauncher.core.extension.ZipExtensionPackageSource

@Composable
actual fun rememberExtensionPackagePickerState(): ExtensionPackagePickerState {
    val context = LocalContext.current
    val importDirectory = remember(context) {
        context.cacheDir.resolve("extension-imports").apply {
            mkdirs()
        }
    }
    val pendingCallbackState = remember {
        mutableStateOf<((ExtensionPackageSource?) -> Unit)?>(null)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val callback = pendingCallbackState.value
        pendingCallbackState.value = null
        if (uri == null || callback == null) {
            callback?.invoke(null)
            return@rememberLauncherForActivityResult
        }

        val source = runCatching {
            val displayName = context.contentResolver.query(
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
            }.orEmpty()
                .trim()
                .ifBlank { "extension_${System.currentTimeMillis()}${TExtensionPackageFormat.FILE_EXTENSION}" }
                .let(::ensureTExtensionFileName)

            val destination = importDirectory.resolve(displayName).apply {
                parentFile?.mkdirs()
                if (exists()) {
                    delete()
                }
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Unable to open selected extension package.")
            ZipExtensionPackageSource(destination)
        }.getOrNull()

        callback(source)
    }

    return ExtensionPackagePickerState(
        isSupported = true,
        pickPackage = { onPicked ->
            pendingCallbackState.value = onPicked
            launcher.launch(arrayOf("*/*"))
        },
    )
}

private fun ensureTExtensionFileName(fileName: String): String {
    val trimmed = fileName.trim()
    return if (TExtensionPackageFormat.isTExtensionFileName(trimmed)) {
        trimmed
    } else {
        trimmed + TExtensionPackageFormat.FILE_EXTENSION
    }
}
