package com.dreamyloong.tlauncher.core.platform

import android.app.Activity
import android.content.Context
import okio.FileSystem
import java.lang.ref.WeakReference

internal object AndroidLauncherStorageContextHolder {
    var applicationContext: Context? = null
    var currentActivity: WeakReference<Activity>? = null
}

fun initializeAndroidLauncherStorage(context: Context) {
    AndroidLauncherStorageContextHolder.applicationContext = context.applicationContext
}

fun initializeAndroidLauncherActivity(activity: Activity) {
    AndroidLauncherStorageContextHolder.currentActivity = WeakReference(activity)
}

fun clearAndroidLauncherActivity(activity: Activity) {
    if (AndroidLauncherStorageContextHolder.currentActivity?.get() == activity) {
        AndroidLauncherStorageContextHolder.currentActivity = null
    }
}

actual fun launcherStorageDirectoryPath(): String {
    val context = requireAndroidLauncherContext()
    return context.filesDir.resolve("launcher-state").absolutePath
}

actual fun launcherAppFilesDirectoryPath(): String {
    return requireAndroidLauncherContext().filesDir.absolutePath
}

actual fun launcherPackageName(): String? {
    return requireAndroidLauncherContext().packageName
}

actual fun launcherFileSystem(): FileSystem = FileSystem.SYSTEM

internal fun requireAndroidLauncherContext(): Context {
    return AndroidLauncherStorageContextHolder.applicationContext
        ?: error("Android launcher storage has not been initialized.")
}
