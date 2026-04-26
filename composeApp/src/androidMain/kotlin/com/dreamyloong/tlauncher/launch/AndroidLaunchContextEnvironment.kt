package com.dreamyloong.tlauncher.launch

import android.content.Context
import android.system.Os
import android.util.Log
import com.dreamyloong.tlauncher.core.platform.GameLaunchRequest
import java.io.File

private const val TAG = "AndroidLaunchContext"
private const val ENV_LAUNCH_CONTEXT_PATH = "TLAUNCHER_LAUNCH_CONTEXT_PATH"
private const val ENV_LAUNCH_CONTEXT_JSON = "TLAUNCHER_LAUNCH_CONTEXT_JSON"
private const val ENV_LAUNCHER_PACKAGE_NAME = "TLAUNCHER_PACKAGE_NAME"
private const val DEFAULT_LAUNCH_CONTEXT_FILE_NAME = "tlauncher-launch-context.json"

internal fun syncAndroidLaunchContextEnvironment(
    context: Context,
    request: GameLaunchRequest?,
) {
    when (request) {
        is GameLaunchRequest.AndroidRuntime -> {
            syncAndroidLaunchContextEnvironment(
                context = context,
                payload = AndroidGameLaunchPayload.fromRequest(request),
            )
        }

        null -> clearAndroidLaunchContextEnvironment(context)
    }
}

internal fun syncAndroidLaunchContextEnvironment(
    context: Context,
    payload: AndroidGameLaunchPayload,
) {
    val launchContextFile = launchContextFile(context, payload.launchContextFileName)
    launchContextFile.parentFile?.mkdirs()
    launchContextFile.writeText(payload.launchContextJson)
    setEnv(ENV_LAUNCHER_PACKAGE_NAME, context.packageName)
    setEnv(ENV_LAUNCH_CONTEXT_JSON, payload.launchContextJson)
    setEnv(ENV_LAUNCH_CONTEXT_PATH, launchContextFile.absolutePath)
    Log.i(TAG, "Synced Android launch context at ${launchContextFile.absolutePath}")
}

internal fun clearAndroidLaunchContextEnvironment(context: Context) {
    launchContextFile(context, DEFAULT_LAUNCH_CONTEXT_FILE_NAME).delete()
    unsetEnv(ENV_LAUNCH_CONTEXT_JSON)
    unsetEnv(ENV_LAUNCH_CONTEXT_PATH)
    setEnv(ENV_LAUNCHER_PACKAGE_NAME, context.packageName)
    Log.i(TAG, "Cleared Android launch context")
}

private fun launchContextFile(
    context: Context,
    fileName: String,
): File {
    val safeFileName = fileName
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .takeIf { it.isNotBlank() }
        ?: DEFAULT_LAUNCH_CONTEXT_FILE_NAME
    return context.filesDir.resolve(safeFileName)
}

private fun setEnv(
    name: String,
    value: String,
) {
    runCatching { Os.setenv(name, value, true) }
        .onFailure { throwable -> Log.w(TAG, "Failed to set env $name", throwable) }
}

private fun unsetEnv(name: String) {
    runCatching { Os.unsetenv(name) }
        .onFailure { throwable -> Log.w(TAG, "Failed to unset env $name", throwable) }
}
