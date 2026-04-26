package com.dreamyloong.tlauncher.core.platform

import platform.Foundation.NSFileManager

actual fun launcherFileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}
