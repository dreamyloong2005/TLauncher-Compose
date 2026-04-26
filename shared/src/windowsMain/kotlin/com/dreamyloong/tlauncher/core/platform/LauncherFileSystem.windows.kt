package com.dreamyloong.tlauncher.core.platform

import java.io.File

actual fun launcherFileExists(path: String): Boolean {
    return File(path).isFile
}
