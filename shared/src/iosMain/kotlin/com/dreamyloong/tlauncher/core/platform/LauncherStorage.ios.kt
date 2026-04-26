package com.dreamyloong.tlauncher.core.platform

import okio.FileSystem
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun launcherStorageDirectoryPath(): String {
    val supportDirectory = NSSearchPathForDirectoriesInDomains(
        directory = NSApplicationSupportDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).filterIsInstance<String>().firstOrNull()
    val baseDirectory = supportDirectory ?: NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).filterIsInstance<String>().first()
    return "$baseDirectory/TLauncher"
}

actual fun launcherAppFilesDirectoryPath(): String = launcherStorageDirectoryPath()

actual fun launcherPackageName(): String? = null

actual fun launcherFileSystem(): FileSystem = FileSystem.SYSTEM
