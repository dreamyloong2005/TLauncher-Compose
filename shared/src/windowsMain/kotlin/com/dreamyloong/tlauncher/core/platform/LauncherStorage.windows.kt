package com.dreamyloong.tlauncher.core.platform

import okio.FileSystem
import java.io.File

actual fun launcherStorageDirectoryPath(): String {
    val baseDirectory = System.getenv("APPDATA")
        ?.takeIf { it.isNotBlank() }
        ?: File(System.getProperty("user.home"), ".tlauncher").absolutePath
    return File(baseDirectory, "TLauncher").absolutePath
}

actual fun launcherAppFilesDirectoryPath(): String = launcherStorageDirectoryPath()

actual fun launcherPackageName(): String? = null

actual fun launcherFileSystem(): FileSystem = FileSystem.SYSTEM
